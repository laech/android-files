package l.files.ui;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.OperationCanceledException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import l.files.fs.DirectoryStream;
import l.files.fs.FileStatus;
import l.files.fs.FileSystemException;
import l.files.fs.NoSuchFileException;
import l.files.fs.Path;
import l.files.fs.PathEntry;
import l.files.fs.WatchEvent;
import l.files.fs.WatchService;
import l.files.logging.Logger;

import static android.os.Looper.getMainLooper;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.emptyList;

/**
 * Loader to load files of a directory and will continue to monitor the
 * directory for changes and notifies observer when that happens.
 */
public final class FilesLoader extends AsyncTaskLoader<List<Object>> {

  private static final Logger logger = Logger.get(FilesLoader.class);
  private static final Handler handler = new Handler(getMainLooper());

  private final WatchService service;
  private final ConcurrentMap<Path, FileStatus> data;
  private final EventListener listener;
  private final Runnable deliverResult;

  private final Path path;
  private final FileSort sort;
  private final boolean showHidden;

  /**
   * @param path       the path to load files from
   * @param sort       the comparator for sorting results
   * @param showHidden whether to show hidden files
   */
  public FilesLoader(Context context, Path path,
                     FileSort sort, boolean showHidden) {
    super(context);

    this.path = checkNotNull(path, "path");
    this.sort = checkNotNull(sort, "sort");
    this.showHidden = showHidden;
    this.data = new ConcurrentHashMap<>();
    this.listener = new EventListener();
    this.deliverResult = new DeliverResultRunnable();
    this.service = path.getResource().watcher();
  }

  @Override protected void onStartLoading() {
    super.onStartLoading();
    if (data.isEmpty()) {
      forceLoad();
    } else {
      deliverResult(buildResult());
    }
  }

  @Override public List<Object> loadInBackground() {
    data.clear();
    try {
      service.register(path, listener);
    } catch (FileSystemException e) {
      // TODO
      return emptyList();
    }
    // TODO this will error on dirs like /proc/xxx
    try (DirectoryStream stream = path.getResource().newDirectoryStream()) {
      for (PathEntry entry : stream) {
        checkCancelled();
        addData(entry.path());
      }
    } catch (IOException e) {
      throw new RuntimeException(e); // TODO
    }
    return buildResult();
  }

  private List<Object> buildResult() {
    List<FileStatus> files = new ArrayList<>(data.size());
    if (showHidden) {
      files.addAll(data.values());
    } else {
      for (FileStatus status : data.values()) {
        if (!status.isHidden()) {
          files.add(status);
        }
      }
    }
    Collections.sort(files, sort.newComparator(Locale.getDefault()));

    List<Object> result = new ArrayList<>(files.size() + 6);

    Categorizer categorizer = sort.newCategorizer();
    Resources res = getContext().getResources();
    String preCategory = null;
    for (int i = 0; i < files.size(); i++) {
      FileStatus stat = files.get(i);
      String category = categorizer.get(res, stat);
      if (i == 0) {
        if (category != null) {
          result.add(category);
        }
      } else {
        if (!Objects.equals(preCategory, category)) {
          result.add(category);
        }
      }
      result.add(stat);
      preCategory = category;
    }

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
      FileStatus newStat = path.getResource().stat();
      FileStatus oldStat = data.put(path, newStat);
      return !Objects.equals(newStat, oldStat);
    } catch (NoSuchFileException e) {
      logger.debug(e);
    } catch (IOException | FileSystemException e) {
      logger.error(e, "Failed to stat %s", path);
      // TODO use wrapper class for error paths e.g. /storage/ext_sd/.android_secure
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
