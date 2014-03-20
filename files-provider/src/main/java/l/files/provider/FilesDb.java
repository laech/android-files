package l.files.provider;

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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;

import l.files.common.logging.Logger;
import l.files.os.OsException;
import l.files.os.Stat;
import l.files.provider.event.LoadFinished;
import l.files.provider.event.LoadProgress;
import l.files.provider.event.LoadStarted;

import static android.database.DatabaseUtils.appendSelectionArgs;
import static android.database.DatabaseUtils.concatenateWhere;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.synchronizedSet;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static l.files.common.database.DataTypes.intToBoolean;
import static l.files.common.event.Events.bus;
import static l.files.common.event.Events.post;
import static l.files.provider.FilesContract.FileInfo;
import static l.files.provider.FilesContract.FileInfo.LOCATION;
import static l.files.provider.FilesContract.FileInfo.PARENT_LOCATION;
import static l.files.provider.FilesContract.getFileLocation;

final class FilesDb extends SQLiteOpenHelper {

  private static final Logger logger = Logger.get(FilesDb.class);

  private static final String TABLE_FILES = "file";
  private static final String DB_NAME = "file.db";
  private static final int DB_VERSION = 1;

  private static final Handler handler = new Handler(Looper.getMainLooper());

  private final Set<String> queried = synchronizedSet(new HashSet<String>());

  // TODO use another executor
  @VisibleForTesting
  static volatile Executor executor = AsyncTask.THREAD_POOL_EXECUTOR;

  private final FilesDbSync manager;
  private final Context context;

  public FilesDb(Context context) {
    this(context, DB_NAME);
  }

  FilesDb(Context context, String name) {
    super(context, name, null, DB_VERSION);
    this.context = context;
    this.manager = new FilesDbSync(context, this);
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
      }
    } finally {
      statement.close();
    }
  }

  public static void deleteChildren(SQLiteDatabase db, String parentLocation) {
    db.delete(TABLE_FILES, PARENT_LOCATION + "=?", new String[]{parentLocation});
  }

  public static void deleteChild(SQLiteDatabase db, String childLocation) {
    db.delete(TABLE_FILES, LOCATION + "=?", new String[]{childLocation});
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

    // Start before doing anything else to speed loading time
    if (queried.add(location) || !manager.isStarted(parent)) {
      executor.execute(new Runnable() {
        @Override public void run() {
          updateAndMonitor(uri, parent);
        }
      });
    }

    Cursor cursor = query(columns, selection, selectionArgs, orderBy);
    cursor.setNotificationUri(context.getContentResolver(), uri);
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
  private void updateAndMonitor(final Uri uri, final File parent) {
    manager.start(parent);

    // TODO symlinks to directories shouldn't be included
    Set<String> dirs = update(uri, parent);
    synchronized (this) {
      for (String childPath : dirs) {
        manager.start(new File(childPath));
      }
    }

    context.getContentResolver().notifyChange(uri, null);
  }

  /**
   * @return the paths of the children that are directories
   */
  private Set<String> update(Uri uri, File parent) {
    post(bus(), new LoadStarted(uri), handler);

    Set<String> dirs = newHashSet();
    String parentLocation = getFileLocation(parent);
    FileData[] entries = loadChildren(uri, parent, dirs);

    SQLiteDatabase db = getWritableDatabase();
    db.beginTransaction();
    try {
      replaceChildren(db, parentLocation, entries);
      db.setTransactionSuccessful();
    } finally {
      db.endTransaction();
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
      String name = names[i];
      String path = parent.getAbsolutePath() + "/" + name;
      try { // TODO
        entries[i] = FileData.from(Stat.stat(path), path, name);
      } catch (OsException e) {
        entries[i] = FileData.from(new File(parent, name));
        logger.warn(e);
      }
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
}
