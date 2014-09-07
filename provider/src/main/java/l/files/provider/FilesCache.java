package l.files.provider;

import android.content.Context;
import android.net.Uri;
import android.os.CancellationSignal;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import l.files.io.file.FileInfo;
import l.files.io.file.Path;
import l.files.io.file.WatchEvent;
import l.files.io.file.WatchService;
import l.files.logging.Logger;

import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static java.lang.Boolean.parseBoolean;
import static l.files.provider.FilesContract.PARAM_SHOW_HIDDEN;
import static l.files.provider.FilesContract.buildFileUri;
import static l.files.provider.FilesContract.getFileLocation;

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
  private final WatchService service;

  // TODO One optimization may be to use a batch per directory
  // and a discrete executor for each, so that updates for a directory is not
  // blocking another directory
  private final EventBatch batch;

  private final Cache<Path, ValueMap> cache =
      CacheBuilder.newBuilder()
          .weakValues()
          .removalListener(this)
          .build();

  FilesCache(Context context, WatchService service) {
    this.context = context;
    this.service = service;
    this.batch = new EventBatch(context.getContentResolver(), this);
  }

  public FileInfo[] get(Uri uri, CancellationSignal signal) {
    ValueMap map = getCache(uri);
    return showHidden(uri) ? map.values() : map.values(NOT_HIDDEN);
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
      logger.debug("Cache load: %s", path);
      loadInto(values, path);
    }
    return values;
  }

  private void loadInto(ValueMap map, Path path) {
    service.unregister(path, this);
    try {
      service.register(path, this);
    } catch (IOException e) {
      // No longer exist etc
      logger.warn(e);
      return;
    }

    String[] names = path.toFile().list();
    if (names == null) {
      return;
    }
    for (String name : names) {
      try {
        Path child = path.child(name);
        map.put(child, FileInfo.get(child.toString()));
      } catch (IOException e) {
        logger.warn(e);
      }
    }
  }

  @Override public void onEvent(WatchEvent event) {
    Path parent = event.path().parent();
    String location = getFileLocation(parent.toFile());
    Uri uri = buildFileUri(context, location);
    batch.batch(event, uri);
  }

  @Override public void process(WatchEvent event) {
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
      data.put(path, FileInfo.get(path.toString()));
    } catch (IOException e) {
      logger.warn(e);
      remove(path);
    }
  }

  private void remove(Path path) {
    Path parent = path.parent();
    ValueMap data = cache.getIfPresent(parent);
    if (data == null) {
      service.unregister(parent, this);
    } else {
      data.remove(path);
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
    private final ConcurrentMap<Path, FileInfo> map;

    ValueMap() {
      map = new ConcurrentHashMap<>();
    }

    FileInfo put(Path key, FileInfo value) {
      return map.put(key, value);
    }

    FileInfo remove(Path key) {
      return map.remove(key);
    }

    FileInfo[] values() {
      return values(Predicates.<FileInfo>alwaysTrue());
    }

    FileInfo[] values(Predicate<FileInfo> filter) {
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
