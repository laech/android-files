package l.files.provider;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.CancellationSignal;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import l.files.fse.WatchEvent;
import l.files.fse.WatchService;
import l.files.io.Path;
import l.files.logging.Logger;
import l.files.os.ErrnoException;

import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static java.lang.Boolean.parseBoolean;
import static l.files.provider.FileData.NOT_HIDDEN;
import static l.files.provider.FilesContract.FileInfo;
import static l.files.provider.FilesContract.PARAM_SHOW_HIDDEN;
import static l.files.provider.FilesContract.buildFileUri;
import static l.files.provider.FilesContract.getFileLocation;

final class FilesQuerier implements WatchEvent.Listener, RemovalListener<Path, FilesQuerier.ValueMap> {

  private static final Logger logger = Logger.get(FilesQuerier.class);

  private static final String[] DEFAULT_PROJECTION = {
      FileInfo.LOCATION,
      FileInfo.NAME,
      FileInfo.SIZE,
      FileInfo.READABLE,
      FileInfo.WRITABLE,
      FileInfo.MIME,
      FileInfo.MODIFIED,
      FileInfo.HIDDEN,
  };

  private final Context context;
  private final WatchService service;

  private final Cache<Path, ValueMap> cache =
      CacheBuilder.newBuilder()
          .weakValues()
          .removalListener(this)
          .build();

  FilesQuerier(Context context, WatchService service) {
    this.context = context;
    this.service = service;
  }

  public Cursor query(Uri uri, String[] projection, String order, CancellationSignal signal) {

    if (projection == null) {
      projection = DEFAULT_PROJECTION;
    }

    ValueMap map = getCache(uri);
    FileData[] data = showHidden(uri) ? map.values() : map.values(NOT_HIDDEN);
    sort(data, order);
    return cursor(uri, projection, data, map);
  }

  private ValueMap getCache(Uri uri) {
    File file = new File(URI.create(getFileLocation(uri)));
    Path path = Path.from(file);
    boolean create = false;
    ValueMap values;
    synchronized (this) {
      values = cache.getIfPresent(path);
      if (values == null) {
        create = true;
        values = new ValueMap();
        // Put the new map in the cache first, register the event listener,
        // then process the children, this allows the processing of children and
        // receiving of event happen at the same time to use the map.
        cache.put(path, values);
      }
    }
    if (create) {
      loadInto(values, path);
    }
    return values;
  }

  private void loadInto(ValueMap map, Path path) {
    service.unregister(path, FilesQuerier.this);
    service.register(path, FilesQuerier.this);

    String[] names = path.toFile().list();
    if (names == null) {
      return;
    }
    for (String name : names) {
      try {
        Path child = path.child(name);
        map.put(child, FileData.stat(child));
      } catch (ErrnoException e) {
        logger.warn(e);
      }
    }
  }

  private Cursor cursor(Uri uri, String[] projection, FileData[] data, Object ref) {
    FileCursor cursor = new FileCursor(data, projection, ref);
    cursor.setNotificationUri(context.getContentResolver(), uri);
    return cursor;
  }

  private void sort(FileData[] data, String sortOrder) {
    if (sortOrder != null) {
      Arrays.sort(data, SortBy.valueOf(sortOrder));
    }
  }

  @Override public void onEvent(WatchEvent event) {
    // TODO batch
    switch (event.kind()) {
      case CREATE:
      case MODIFY:
        addOrUpdate(event.path());
        break;
      case DELETE:
        remove(event.path());
        break;
    }
  }

  private void addOrUpdate(Path path) {
    Path parent = path.parent();
    ValueMap data = cache.getIfPresent(parent);
    if (data == null) {
      service.unregister(parent, this);
      return;
    }

    try {
      data.put(path, FileData.stat(path));
      notifyContentChanged(parent);
    } catch (ErrnoException e) {
      remove(path);
    }
  }

  private void remove(Path path) {
    Path parent = path.parent();
    ValueMap data = cache.getIfPresent(parent);
    if (data == null) {
      service.unregister(parent, this);
    } else if (data.remove(path) != null) {
      notifyContentChanged(parent);
    }
  }

  private void notifyContentChanged(Path path) {
    String location = getFileLocation(path.toFile());
    Uri uri = buildFileUri(context, location);
    context.getContentResolver().notifyChange(uri, null);
  }

  @Override
  public void onRemoval(RemovalNotification<Path, ValueMap> notification) {
    Path path = notification.getKey();
    service.unregister(path, this);
  }

  private boolean showHidden(Uri uri) {
    return parseBoolean(uri.getQueryParameter(PARAM_SHOW_HIDDEN));
  }

  static final class ValueMap {
    private final ConcurrentMap<Path, FileData> map;

    ValueMap() {
      map = new ConcurrentHashMap<>();
    }

    FileData put(Path key, FileData value) {
      return map.put(key, value);
    }

    FileData remove(Path key) {
      return map.remove(key);
    }

    FileData[] values() {
      return values(Predicates.<FileData>alwaysTrue());
    }

    FileData[] values(Predicate<FileData> filter) {
      List<FileData> list = newArrayListWithCapacity(map.size());
      for (FileData data : map.values()) {
        if (filter.apply(data)) {
          list.add(data);
        }
      }
      return list.toArray(new FileData[list.size()]);
    }
  }
}
