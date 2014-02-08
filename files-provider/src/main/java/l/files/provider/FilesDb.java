package l.files.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;

import java.io.File;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;

import l.files.common.logging.Logger;
import l.files.provider.event.LoadFinished;
import l.files.provider.event.LoadProgress;
import l.files.provider.event.LoadStarted;

import static android.database.DatabaseUtils.appendSelectionArgs;
import static android.database.DatabaseUtils.concatenateWhere;
import static android.os.FileObserver.ATTRIB;
import static android.os.FileObserver.CREATE;
import static android.os.FileObserver.DELETE;
import static android.os.FileObserver.DELETE_SELF;
import static android.os.FileObserver.MODIFY;
import static android.os.FileObserver.MOVED_FROM;
import static android.os.FileObserver.MOVED_TO;
import static android.os.FileObserver.MOVE_SELF;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Map.Entry;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static l.files.common.database.DataTypes.intToBoolean;
import static l.files.common.event.Events.bus;
import static l.files.common.event.Events.post;
import static l.files.provider.FilesContract.FileInfo;
import static l.files.provider.FilesContract.FileInfo.LOCATION;
import static l.files.provider.FilesContract.FileInfo.PARENT_LOCATION;
import static l.files.provider.FilesContract.getFileLocation;

@VisibleForTesting
public final class FilesDb extends SQLiteOpenHelper implements
    StopSelfListener.Callback,
    UpdateSelfListener.Callback,
    UpdateChildrenListener.Callback {

  private static final Logger logger = Logger.get(FilesDb.class);

  private static final String TABLE_FILES = "file";
  private static final String DB_NAME = "file.db";
  private static final int DB_VERSION = 1;
  private static final int MODIFICATION_MASK = ATTRIB
      | CREATE
      | DELETE
      | DELETE_SELF
      | MODIFY
      | MOVE_SELF
      | MOVED_FROM
      | MOVED_TO;

  private static final Handler handler = new Handler(Looper.getMainLooper());

  /**
   * The main directories that are currently being monitored (i.e. the ones that
   * have been called {@link #updateAndMonitor(Uri, File)}). The keys are {@link
   * FileInfo#LOCATION}s.
   */
  private static final ConcurrentMap<String, File> monitored = new ConcurrentHashMap<>();

  /**
   * The map of {@link FileInfo#LOCATION} to file observers.
   * <p/>
   * {@link android.os.FileObserver} class has global shared state, there could
   * only be one observer per file, regardless of the event mask. Stopping an
   * observer or garbage collecting an observer will cause it to be stopped, and
   * because the states are shared globally, all observer instances watching on
   * the same file node will stop receiving events.
   */
  private static final Map<String, DirWatcher> observers = newHashMap();

  @VisibleForTesting
  public static Executor executor = AsyncTask.THREAD_POOL_EXECUTOR;

  private final ContentResolver resolver;
  private final Uri authority;

  public FilesDb(Context context, Uri authority) {
    super(context, DB_NAME, null, DB_VERSION);
    this.authority = checkNotNull(authority, "authority");
    this.resolver = context.getContentResolver();
  }

  public static void replaceChildren(
      SQLiteDatabase db, String parentLocation, FileData... entries) {
    deleteChildren(db, parentLocation);
    insertChildren(db, parentLocation, entries);
  }

  public static void insertChildren(
      SQLiteDatabase db, String parentLocation, FileData... entries) {

    SQLiteStatement statement = db.compileStatement("insert into " +
        TABLE_FILES + " (" +
        FileInfo.LOCATION + ", " +
        FileInfo.PARENT_LOCATION + ", " +
        FileInfo.NAME + ", " +
        FileInfo.MIME + ", " +
        FileInfo.SIZE + ", " +
        FileInfo.MODIFIED + ", " +
        FileInfo.HIDDEN + ", " +
        FileInfo.IS_DIRECTORY + ", " +
        FileInfo.READABLE + ", " +
        FileInfo.WRITABLE +
        ") values(?,?,?,?,?,?,?,?,?,?)");

    //noinspection TryFinallyCanBeTryWithResources
    try {
      for (int i = 0; i < entries.length; i++) {
        FileData entry = entries[i];
        statement.clearBindings();
        statement.bindString(1, entry.location);
        statement.bindString(2, parentLocation);
        statement.bindString(3, entry.name);
        statement.bindString(4, entry.mime);
        statement.bindLong(5, entry.length);
        statement.bindLong(6, entry.lastModified);
        statement.bindLong(7, entry.hidden);
        statement.bindLong(8, entry.directory);
        statement.bindLong(9, entry.canRead);
        statement.bindLong(10, entry.canWrite);
        statement.executeInsert();
        if (i % 1000 == 0) {
          db.yieldIfContendedSafely();
        }
        logger.debug("Insert %s", entry.location);
      }
    } finally {
      statement.close();
    }
  }

  public static void deleteChildren(SQLiteDatabase db, String parentLocation) {
    db.delete(TABLE_FILES, PARENT_LOCATION + "=?", new String[]{parentLocation});
    logger.debug("Delete children of %s", parentLocation);
  }

  public static void deleteChild(SQLiteDatabase db, String childLocation) {
    db.delete(TABLE_FILES, LOCATION + "=?", new String[]{childLocation});
    logger.debug("Delete %s", childLocation);
  }

  @Override public void onCreate(SQLiteDatabase db) {
    db.execSQL("create table " + TABLE_FILES + " (" +
        FileInfo.LOCATION + " text not null primary key on conflict replace, " +
        FileInfo.NAME + " text not null collate localized, " +
        FileInfo.SIZE + " integer not null, " +
        FileInfo.MIME + " text not null, " +
        FileInfo.HIDDEN + " integer not null, " +
        FileInfo.MODIFIED + " integer not null, " +
        FileInfo.READABLE + " integer not null, " +
        FileInfo.WRITABLE + " integer not null, " +
        FileInfo.IS_DIRECTORY + " integer not null, " +
        FileInfo.PARENT_LOCATION + " text not null)");

    db.execSQL("create index idx_parent on " +
        TABLE_FILES + "(" + FileInfo.PARENT_LOCATION + ")");
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("drop table if exists " + TABLE_FILES);
    onCreate(db);
  }

  /*
   * On first query of a directory, it's content will be loaded to the database,
   * then it will be tracked by a {@link FileObserver} implementation, so that
   * changes (such as writing to a file causing the last modified date to be
   * updated) will be captured and the database record will be updated
   * accordingly.
   *
   * Adding and deleting files/directories under child directories are not fired
   * but they still need to be tracked because such actions will cause the child
   * directory's last modified date to be changed. Tracking is done by
   * registering another observer for each child directory currently present in
   * the monitored parent directory, and any new directory added to the parent
   * while the parent is being monitored.
   */
  public Cursor query(
      final Uri uri,
      String[] columns,
      String selection,
      String[] selectionArgs,
      String orderBy) {


    final File parent = new File(URI.create(getFileLocation(uri)));
    String location = getFileLocation(parent);
    selection = concatenateWhere(selection, FileInfo.PARENT_LOCATION + "=?");
    selectionArgs = appendSelectionArgs(selectionArgs, new String[]{location});
    Cursor cursor = query(columns, selection, selectionArgs, orderBy);


    if (cursor.getCount() == 0) {
      // If cursor is empty and the directory is not currently being monitored,
      // update it before returning
      if (updateAndMonitor(uri, parent)) {
        cursor.close();
        cursor = query(columns, selection, selectionArgs, orderBy);
      }
    } else if (!monitored.containsKey(location)) {
      // If there is already data for the directory, return the cursor, then
      // update it in the background.
      executor.execute(new Runnable() {
        @Override public void run() {
          updateAndMonitor(uri, parent);
        }
      });
    }

    cursor.setNotificationUri(resolver, uri);
    return cursor;
  }

  private Cursor query(
      String[] columns, String where, String[] whereArgs, String orderBy) {
    return getReadableDatabase().query(
        TABLE_FILES, columns, where, whereArgs, null, null, orderBy);
  }

  /**
   * Updates the database record of the given parent, and starts monitoring
   * changes in the parent and update the database accordingly. Has no affect if
   * the given parent is already being monitored. Returns true if directory will
   * be updated and monitored, false if it's already being monitored.
   */
  private boolean updateAndMonitor(final Uri uri, final File parent) {
    String parentLocation = getFileLocation(parent);
    synchronized (FilesDb.class) {
      if (monitored.put(parentLocation, parent) != null) {
        return false;
      }
      DirWatcher parentObserver = observers.get(parentLocation);
      if (parentObserver == null) {
        parentObserver = newObserver(parent);
        parentObserver.startWatching();
        observers.put(parentLocation, parentObserver);
      }
    }

    // TODO symlinks to directories shouldn't be included
    Set<String> dirs = update(uri, parent);
    synchronized (FilesDb.class) {
      for (String childPath : dirs) {
        File child = new File(childPath);
        String childLocation = getFileLocation(child);
        DirWatcher childObserver = observers.get(childLocation);
        if (childObserver == null) {
          childObserver = newObserver(child);
          childObserver.startWatching();
          observers.put(childLocation, childObserver);
        }
      }
    }

    resolver.notifyChange(uri, null);
    return true;
  }

  private DirWatcher newObserver(File dir) {
    Processor processor = new Processor(this, resolver);
    DirWatcher observer = new DirWatcher(dir, MODIFICATION_MASK);
    observer.setListeners(
        new StopSelfListener(observer, this),
        new UpdateSelfListener(authority, dir, processor, this, this),
        new UpdateChildrenListener(authority, dir, processor, this, this));
    return observer;
  }

  /**
   * @return the paths of the children that are directories
   */
  private Set<String> update(Uri uri, File parent) {
    post(bus(), new LoadStarted(uri), handler);

    Set<String> dirs = newHashSet();
    String parentLocation = getFileLocation(parent);
    FileData[] entries = loadChildren(uri, parent, dirs);

    logger.debug("Begin transaction");
    SQLiteDatabase db = getWritableDatabase();
    db.beginTransaction();
    try {
      replaceChildren(db, parentLocation, entries);
      db.setTransactionSuccessful();
    } finally {
      db.endTransaction();
      logger.debug("End transaction");
    }

    post(bus(), new LoadFinished(uri), handler);

    return dirs;
  }

  /*
   * Calling methods on java.io.File (e.g. lastModified, length, etc) is
   * relatively expensive (except getName as it doesn't require a call to the
   * OS). On a large directory, this process can take minutes (Galaxy Nexus,
   * /storage/emulated/0/DCIM/.thumbnails with ~20,000 files took 1 to 4 minutes
   * to load, where File.listFiles took around 2 ~ 3 seconds to return).
   *
   * It doesn't seem to make a difference in time whether only one property
   * method is called or all properties are called. Ideally such calls to the
   * properties can be avoided until needed, but they are needed upfront because
   * of sorting, such as sorting by last modified date.
   *
   * So take the loading out of the transaction.
   */
  private FileData[] loadChildren(Uri uri, File parent, Set<String> dirs) {
    String[] names = parent.list();
    if (names == null) {
      names = new String[0];
    }

    Stopwatch watch = Stopwatch.createStarted();
    FileData[] entries = new FileData[names.length];
    for (int i = 0; i < entries.length; i++) {
      entries[i] = FileData.from(new File(parent, names[i]));
      postLoadProgressIfOkay(watch, uri, i + 1, names.length);
      if (intToBoolean(entries[i].directory)) {
        dirs.add(entries[i].path);
      }
    }
    postLoadProgressIfOkay(watch, uri, names.length, names.length);
    return entries;
  }

  private void postLoadProgressIfOkay(
      Stopwatch watch, Uri uri, int progress, int max) {
    if (watch.elapsed(MILLISECONDS) >= 500) {
      post(bus(), new LoadProgress(uri, progress, max), handler);
      watch.reset().start();
    }
  }

  @Override public void onObserverStopped(DirWatcher observer) {
    String location = getFileLocation(observer.getDirectory());
    synchronized (this) {
      observers.remove(location);
      monitored.remove(location);
      String prefix = location.endsWith("/") ? location : location + "/";
      removeMonitored(prefix);
      removeObservers(prefix);
    }
  }

  private void removeMonitored(String locationPrefix) {
    Iterator<Entry<String, File>> it = monitored.entrySet().iterator();
    while (it.hasNext()) {
      if (it.next().getKey().startsWith(locationPrefix)) {
        it.remove();
      }
    }
  }

  private void removeObservers(String locationPrefix) {
    Iterator<Entry<String, DirWatcher>> it = observers.entrySet().iterator();
    while (it.hasNext()) {
      Entry<String, DirWatcher> entry = it.next();
      if (entry.getKey().startsWith(locationPrefix)) {
        entry.getValue().stopWatching();
        it.remove();
      }
    }
  }

  @Override public void onChildAdded(File child, String childLocation) {
    if (!child.isDirectory()) {
      return;
    }
    synchronized (FilesDb.class) {
      DirWatcher childObserver = observers.get(childLocation);
      if (childObserver == null) {
        childObserver = newObserver(child);
        childObserver.startWatching();
        observers.put(childLocation, childObserver);
      }
    }
  }

  @Override public void onChildRemoved(File child, String childLocation) {
    synchronized (FilesDb.class) {
      DirWatcher childObserver = observers.remove(childLocation);
      if (childObserver != null) {
        childObserver.stopWatching();
      }
    }
  }

  @Override public boolean onPreUpdateSelf(String parentLocation) {
    synchronized (FilesDb.class) {
      return monitored.containsKey(parentLocation);
    }
  }
}
