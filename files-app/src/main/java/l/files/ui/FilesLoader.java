package l.files.ui;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Handler;
import android.os.OperationCanceledException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import l.files.fs.DirectoryStream;
import l.files.fs.FileStatus;
import l.files.fs.FileSystem;
import l.files.fs.NoSuchFileException;
import l.files.fs.Path;
import l.files.fs.PathEntry;
import l.files.fs.WatchEvent;
import l.files.fs.WatchService;
import l.files.logging.Logger;

import static android.os.Looper.getMainLooper;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Loader to load files of a directory and will continue to monitor the
 * directory for changes and notifies observer when that happens.
 */
public final class FilesLoader extends AsyncTaskLoader<List<FileStatus>> {

  private static final Logger logger = Logger.get(FilesLoader.class);
  private static final Handler handler = new Handler(getMainLooper());

  private final WatchService service;
  private final ConcurrentMap<Path, FileStatus> data;
  private final EventListener listener;
  private final Runnable deliverResult;

  private final Path path;
  private final Comparator<FileStatus> comparator;
  private final boolean showHidden;

  /**
   * @param path       the path to load files from
   * @param comparator the comparator for sorting results
   * @param showHidden whether to show hidden files
   */
  public FilesLoader(Context context, Path path,
                     Comparator<FileStatus> comparator, boolean showHidden) {
    super(context);

    this.path = checkNotNull(path, "path");
    this.comparator = checkNotNull(comparator, "comparator");
    this.showHidden = showHidden;
    this.data = new ConcurrentHashMap<>();
    this.listener = new EventListener();
    this.deliverResult = new DeliverResultRunnable();
    this.service = path.resource().watcher();
  }

  @Override protected void onStartLoading() {
    super.onStartLoading();
    if (data.isEmpty()) {
      forceLoad();
    } else {
      deliverResult(buildResult());
    }
  }

  @Override public List<FileStatus> loadInBackground() {
    data.clear();
    service.register(path, listener);
    try (DirectoryStream stream = path.resource().newDirectoryStream()) {
      for (PathEntry entry : stream) {
        checkCancelled();
        addData(entry.path());
      }
    } catch (IOException e) {
      throw new RuntimeException(e); // TODO
    }
    return buildResult();
  }

  private List<FileStatus> buildResult() {
    List<FileStatus> result = new ArrayList<>(data.size());
    if (showHidden) {
      result.addAll(data.values());
    } else {
      for (FileStatus status : data.values()) {
        if (!status.isHidden()) {
          result.add(status);
        }
      }
    }
    Collections.sort(result, comparator);
    return result;
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
   * Adds the new status of the given path to the data map. Returns true if the
   * data map is changed.
   */
  private boolean addData(Path path) {
    try {
      FileStatus newStat = path.resource().stat();
      FileStatus oldStat = data.put(path, newStat);
      return !Objects.equals(newStat, oldStat);
    } catch (NoSuchFileException e) {
      logger.debug(e);
    } catch (IOException e) {
      // TODO
    }
    return false;
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
      deliverResult(buildResult());
    }
  }
}
