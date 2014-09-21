package l.files.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;
import android.os.CancellationSignal;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import l.files.io.file.FileInfo;
import l.files.io.file.Path;
import l.files.io.file.WatchEvent;
import l.files.io.file.WatchService;
import l.files.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static java.lang.Boolean.parseBoolean;
import static l.files.provider.FilesContract.PARAM_SHOW_HIDDEN;
import static l.files.provider.FilesContract.buildFileUri;
import static l.files.provider.FilesContract.getFileId;

final class FilesCache implements
    WatchEvent.Listener,
    EventBatch.Processor,
    RemovalListener<Path, FilesCache.ValueMap> {

  private static final Logger logger = Logger.get(FilesCache.class);


  static final Predicate<FileInfo> HIDDEN = new Predicate<FileInfo>() {
    @Override
    public boolean apply(FileInfo input) {
      return input.isHidden();
    }
  };

  static final Predicate<FileInfo> NOT_HIDDEN = not(HIDDEN);


  private final Context context;
  private final ContentResolver resolver;
  private final WatchService service;

  private final Map<Path, EventBatch> batches;

  private final Cache<Path, ValueMap> cache =
      CacheBuilder.newBuilder()
          .weakValues()
          .removalListener(this)
          .build();

  FilesCache(Context context, WatchService service) {
    this(context, service, context.getContentResolver());
  }

  FilesCache(Context context, WatchService service, ContentResolver resolver) {
    this.context = checkNotNull(context, "context");
    this.resolver = checkNotNull(resolver, "resolver");
    this.service = checkNotNull(service, "service");
    this.batches = new HashMap<>();
  }

  // TODO implement CancellationSignal
  public Cursor get(Uri uri, String[] columns, String sortOrder, CancellationSignal signal) {
    final ValueMap map = getCache(uri);
    final FileInfo[] files = showHidden(uri) ? map.values() : map.values(NOT_HIDDEN);
    return new CursorWrapper(newFileCursor(uri, columns, sortOrder, files)) {
      // Hold a reference to keep the cache alive as long as the cursor is used
      @SuppressWarnings("UnusedDeclaration")
      private final Object cache = map;
    };
  }

  @VisibleForTesting Object getFromCache(Path path) {
    return cache.getIfPresent(path);
  }

  private Cursor newFileCursor(Uri uri, String[] projection, String sortOrder, FileInfo[] files) {
    sort(files, sortOrder);
    Cursor c = new FileCursor(files, projection);
    c.setNotificationUri(resolver, uri);
    return c;
  }

  private void sort(FileInfo[] data, String sortOrder) {
    if (sortOrder != null) {
      Arrays.sort(data, SortBy.valueOf(sortOrder));
    }
  }

  private ValueMap getCache(Uri uri) {
    File file = new File(URI.create(getFileId(uri)));
    Path path = Path.from(file);
    if (!service.isWatchable(path)) {
      return loadInto(new ValueMap(), path);
    }

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
      logger.debug("Cache load: %s", path);
      service.unregister(path, this);
      try {
        service.register(path, this);
        loadInto(values, path);
      } catch (IOException e) {
        logger.debug(e, "Failed to read %s", path);
      }
    }
    return values;
  }

  private ValueMap loadInto(ValueMap map, Path path) {
    String[] names = path.toFile().list();
    if (names == null) {
      return map;
    }
    for (String name : names) {
      try {
        Path child = path.child(name);
        map.put(child, FileInfo.read(child.toString()));
      } catch (IOException e) {
        logger.debug(e, "Failed to read %s", path);
      }
    }
    return map;
  }

  @Override public void onEvent(WatchEvent event) {
    Path parent = event.path().parent();
    String location = getFileId(parent.toFile());
    Uri uri = buildFileUri(context, location);
    EventBatch batch;
    synchronized (this) {
      batch = batches.get(parent);
      if (batch == null) {
        batch = new EventBatch(resolver, uri, this);
        batches.put(parent, batch);
      }
    }
    batch.batch(event);
  }

  @Override public boolean process(WatchEvent event) {
    switch (event.kind()) {
      case CREATE:
      case MODIFY:
        return addOrUpdate(event.path());
      case DELETE:
        return remove(event.path());
      default:
        throw new AssertionError(event);
    }
  }

  private boolean addOrUpdate(Path path) {
    Path parent = path.parent();
    ValueMap data = cache.getIfPresent(parent);
    if (data == null) {
      service.unregister(parent, this);
      return false;
    }

    try {
      FileInfo newInfo = FileInfo.read(path.toString());
      FileInfo oldInfo = data.put(path, newInfo);
      return !Objects.equal(newInfo, oldInfo);
    } catch (IOException e) {
      logger.debug(e, "Failed to read %s", path);
      return remove(path);
    }
  }

  private boolean remove(Path path) {
    Path parent = path.parent();
    ValueMap data = cache.getIfPresent(parent);
    if (data == null) {
      service.unregister(parent, this);
      return false;
    } else {
      return data.remove(path) != null;
    }
  }

  @Override
  public void onRemoval(RemovalNotification<Path, ValueMap> notification) {
    Path path = notification.getKey();
    service.unregister(path, this);
    logger.debug("Cache remove: %s", path);
  }

  private boolean showHidden(Uri uri) {
    return parseBoolean(uri.getQueryParameter(PARAM_SHOW_HIDDEN));
  }

  static final class ValueMap {
    private final Map<Path, FileInfo> map;

    ValueMap() {
      map = new HashMap<>();
    }

    synchronized FileInfo put(Path key, FileInfo value) {
      return map.put(key, value);
    }

    synchronized FileInfo remove(Path key) {
      return map.remove(key);
    }

    FileInfo[] values() {
      return values(Predicates.<FileInfo>alwaysTrue());
    }

    synchronized FileInfo[] values(Predicate<FileInfo> filter) {
      List<FileInfo> list = newArrayListWithCapacity(map.size());
      for (FileInfo data : map.values()) {
        if (filter.apply(data)) {
          list.add(data);
        }
      }
      return list.toArray(new FileInfo[list.size()]);
    }
  }
}
