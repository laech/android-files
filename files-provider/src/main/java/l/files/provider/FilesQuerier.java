package l.files.provider;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.CancellationSignal;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import l.files.fse.FileEventListener;
import l.files.fse.FileEventService;
import l.files.fse.PathStat;
import l.files.logging.Logger;
import l.files.os.OsException;

import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static java.lang.Boolean.parseBoolean;
import static l.files.os.Stat.stat;
import static l.files.provider.FilesContract.FileInfo;
import static l.files.provider.FilesContract.PARAM_SHOW_HIDDEN;
import static l.files.provider.FilesContract.buildFileUri;
import static l.files.provider.FilesContract.getFileLocation;

final class FilesQuerier extends CacheLoader<String, Map<String, FileData>>
    implements FileEventListener, RemovalListener<String, Map<String, FileData>> {

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
  private final FileEventService service;

  private final LoadingCache<String, Map<String, FileData>> cache =
      CacheBuilder.newBuilder()
          .weakValues()
          .removalListener(this)
          .build(this);

  FilesQuerier(Context context, FileEventService service) {
    this.context = context;
    this.service = service;
    this.service.register(this); // TODO
  }

  public Cursor query(
      Uri uri, String[] projection, String order, CancellationSignal signal) {

    if (projection == null) {
      projection = DEFAULT_PROJECTION;
    }
    boolean showHidden = getShowHidden(uri);

    Map<String, FileData> map = getCache(uri);
    FileData[] data = showHidden ? array(map) : filterHidden(map);
    sort(order, data);
    return cursor(uri, projection, data, map);
  }

  @Override public void onFileAdded(int event, String parent, String child) {
    addOrUpdate(parent, child);
  }

  @Override public void onFileChanged(int event, String parent, String child) {
    addOrUpdate(parent, child);
  }

  @Override public void onFileRemoved(int event, String parent, String child) {
    remove(parent, child);
  }

  @Override public void onRemoval(
      @SuppressWarnings("NullableProblems")
      RemovalNotification<String, Map<String, FileData>> notification) {
    logger.debug("Removal cache %s", notification.getKey());
    service.unmonitor(new File(notification.getKey()));
  }

  @Override public Map<String, FileData> load(
      @SuppressWarnings("NullableProblems") String key) throws Exception {
    logger.debug("Load cache %s", key);
    return loadCache(new File(key));
  }

  private boolean getShowHidden(Uri uri) {
    return parseBoolean(uri.getQueryParameter(PARAM_SHOW_HIDDEN));
  }

  private Map<String, FileData> getCache(Uri uri) {
    File parent = new File(URI.create(getFileLocation(uri)));
    try {
      return cache.get(parent.getPath());
    } catch (ExecutionException e) {
      throw new AssertionError(e);
    }
  }

  private Cursor cursor(Uri uri, String[] projection, FileData[] data, Object ref) {
    FileCursor cursor = new FileCursor(data, projection, ref);
    cursor.setNotificationUri(context.getContentResolver(), uri);
    return cursor;
  }

  private void sort(String sortOrder, FileData[] data) {
    if (sortOrder != null) {
      Arrays.sort(data, SortBy.valueOf(sortOrder));
    }
  }

  private FileData[] array(Map<String, FileData> map) {
    return map.values().toArray(new FileData[map.size()]);
  }

  private FileData[] filterHidden(Map<String, FileData> cache) {
    List<FileData> list = newArrayListWithCapacity(cache.size());
    for (FileData node : cache.values()) {
      if (node.hidden == 0) {
        list.add(node);
      }
    }
    return list.toArray(new FileData[list.size()]);
  }

  private void addOrUpdate(String parent, String child) {
    Map<String, FileData> data = cache.getIfPresent(parent);
    if (data == null) {
      service.unmonitor(new File(parent));
      return;
    }

    try {

      String path = parent + "/" + child;
      FileData file = FileData.from(stat(path), path, child);
      data.put(child, file);
      notifyContentChanged(parent);

    } catch (OsException e) {
      remove(parent, child);
    }
  }

  private void remove(String parent, String child) {
    Map<String, FileData> data = cache.getIfPresent(parent);
    if (data == null) {
      service.unmonitor(new File(parent));
      return;
    }
    if (data.remove(child) != null) {
      notifyContentChanged(parent);
    }
  }

  private void notifyContentChanged(String parent) {
    String location = getFileLocation(new File(parent));
    Uri uri = buildFileUri(context, location);
    context.getContentResolver().notifyChange(uri, null);
  }

  private Map<String, FileData> loadCache(File file) {
    service.unmonitor(file);
    List<PathStat> stats = service.monitor2(file).get();
    return cacheMap(stats);
  }

  private Map<String, FileData> cacheMap(Collection<PathStat> stats) {
    Map<String, FileData> m = newHashMapWithExpectedSize(stats.size());
    for (PathStat stat : stats) {
      String path = stat.path();
      String name = FilenameUtils.getName(path);
      m.put(name, FileData.from(stat.stat(), path, name));
    }
    return m;
  }
}
