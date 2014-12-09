package l.files.provider;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Handler;
import android.os.OperationCanceledException;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import l.files.fs.DirectoryEntry;
import l.files.fs.DirectoryStream;
import l.files.fs.FileStatus;
import l.files.fs.Files;
import l.files.fs.NoSuchFileException;
import l.files.fs.Path;
import l.files.fs.WatchEvent;
import l.files.fs.WatchService;
import l.files.logging.Logger;

import static android.os.Looper.getMainLooper;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;

/**
 * Loader to load files of a directory and will continue to monitor the
 * directory for changes and notifies observer when that happens.
 */
public final class FilesLoader extends AsyncTaskLoader<List<FileStatus>> {

  private static final Logger logger = Logger.get(FilesLoader.class);
  private static final Handler handler = new Handler(getMainLooper());

  private final Path path;
  private final Comparator<FileStatus> comparator;
  private final ConcurrentMap<Path, FileStatus> data;
  private final EventListener listener;
  private final WatchService service;
  private final Runnable deliverResult;

  /**
   * @param path       the path to load files from
   * @param comparator the comparator for sorting results
   * @param service    the service to monitor changes file changes
   */
  public FilesLoader(
      Context context,
      Path path,
      Comparator<FileStatus> comparator,
      WatchService service) {
    super(context);
    this.path = checkNotNull(path);
    this.comparator = checkNotNull(comparator);
    this.service = checkNotNull(service);
    this.data = new ConcurrentHashMap<>();
    this.listener = new EventListener();
    this.deliverResult = new DeliverResultRunnable();
  }

  @Override protected void onStartLoading() {
    super.onStartLoading();
    if (data.isEmpty()) {
      forceLoad();
    } else {
      deliverResult(sortData());
    }
  }

  @Override public List<FileStatus> loadInBackground() {
    data.clear();
    service.register(path, listener);
    try (DirectoryStream stream = Files.openDirectory(path)) {
      for (DirectoryEntry entry : stream) {
        checkCancelled();
        addData(entry.path());
      }
    }
    return sortData();
  }

  private List<FileStatus> sortData() {
    FileStatus[] stats = data.values().toArray(new FileStatus[data.size()]);
    Arrays.sort(stats, comparator);
    return asList(stats);
  }

  @Override protected void onReset() {
    super.onReset();
    service.unregister(path, listener);
    data.clear();
  }

  @Override protected void finalize() throws Throwable {
    super.finalize();
    if (!isReset()) {
      service.unregister(path, listener);
      logger.error("WatchService has not been unregistered");
    }
  }

  private void checkCancelled() {
    if (isLoadInBackgroundCanceled()) {
      throw new OperationCanceledException();
    }
  }

  /**
   * Adds the new status of the given path to the data map.
   * Returns true if the data map is changed.
   */
  private boolean addData(Path path) {
    try {
      FileStatus newStat = stat(path);
      FileStatus oldStat = data.put(path, newStat);
      return !Objects.equals(newStat, oldStat);
    } catch (NoSuchFileException e) {
      logger.debug(e);
    }
    return false;
  }

  private FileStatus stat(Path path) {
    return Files.stat(path, false);
  }

  final class EventListener implements WatchEvent.Listener {
    @Override public void onEvent(WatchEvent event) {
      switch (event.kind()) {
        case CREATE:
        case MODIFY:
          if (!path.equals(event.path()) && addData(event.path())) {
            redeliverResult();
          }
          break;
        case DELETE:
          if (data.remove(event.path()) != null) {
            redeliverResult();
          }
          break;
        default:
          throw new AssertionError(event);
      }
    }
  }

  private void redeliverResult() {
    handler.removeCallbacks(deliverResult);
    handler.postDelayed(deliverResult, 1000);
  }

  final class DeliverResultRunnable implements Runnable {
    @Override public void run() {
      deliverResult(sortData());
    }
  }
}
