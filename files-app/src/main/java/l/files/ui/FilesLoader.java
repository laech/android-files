package l.files.ui;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.OperationCanceledException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import l.files.fs.Path;
import l.files.fs.Resource;
import l.files.fs.ResourceStatus;
import l.files.fs.ResourceStream;
import l.files.fs.WatchEvent;
import l.files.fs.WatchService;
import l.files.logging.Logger;

import static android.os.Looper.getMainLooper;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.emptyList;

public final class FilesLoader extends AsyncTaskLoader<List<FileListItem>> {

  private static final Logger logger = Logger.get(FilesLoader.class);
  private static final Handler handler = new Handler(getMainLooper());

  private final WatchService service;
  private final ConcurrentMap<Path, FileListItem.File> data;
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
    this.service = path.getResource().getWatcher();
  }

  @Override protected void onStartLoading() {
    super.onStartLoading();
    if (data.isEmpty()) {
      forceLoad();
    } else {
      deliverResult(buildResult());
    }
  }

  @Override public List<FileListItem> loadInBackground() {
    data.clear();
    try {
      service.register(path, listener);
    } catch (IOException e) {
      // TODO
      return emptyList();
    }
    // TODO this will error on dirs like /proc/xxx
    try (ResourceStream<? extends Resource> stream = path.getResource().openResourceStream()) {
      Iterator<? extends Resource> it = stream.iterator();
      while (it.hasNext()) {
        checkCancelled();
        addData(it.next().getPath());
      }
    } catch (IOException e) {
      throw new RuntimeException(e); // TODO
    }
    return buildResult();
  }

  private List<FileListItem> buildResult() {
    List<FileListItem.File> files = new ArrayList<>(data.size());
    if (showHidden) {
      files.addAll(data.values());
    } else {
      for (FileListItem.File item : data.values()) {
        if (!item.getPath().getIsHidden()) {
          files.add(item);
        }
      }
    }
    Collections.sort(files, sort.newComparator(Locale.getDefault()));

    List<FileListItem> result = new ArrayList<>(files.size() + 6);

    Categorizer categorizer = sort.newCategorizer();
    Resources res = getContext().getResources();
    String preCategory = null;
    for (int i = 0; i < files.size(); i++) {
      FileListItem.File stat = files.get(i);
      String category = categorizer.get(res, stat);
      if (i == 0) {
        if (category != null) {
          result.add(new FileListItem.Header(category));
        }
      } else {
        if (!Objects.equals(preCategory, category)) {
          result.add(new FileListItem.Header(category));
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

      Resource resource = path.getResource();
      ResourceStatus stat = resource.readStatus(false);
      ResourceStatus targetStat = readTargetStatus(stat);
      FileListItem.File newStat = new FileListItem.File(path, stat, targetStat);
      FileListItem.File oldStat = data.put(path, newStat);
      return !Objects.equals(newStat, oldStat);

    } catch (FileNotFoundException e) {
      return data.remove(path) != null;

    } catch (IOException e) {
      data.put(path, new FileListItem.File(path, null, null));
      return true;
    }
  }

  private ResourceStatus readTargetStatus(ResourceStatus status) {
    if (status.getIsSymbolicLink()) {
      try {
        return status.getResource().readStatus(true);
      } catch (IOException e) {
        logger.debug(e);
      }
    }
    return status;
  }

  final class EventListener implements WatchEvent.Listener {
    @Override public void onEvent(WatchEvent event) {
      switch (event.getKind()) {
        case CREATE:
        case MODIFY:
          if (!path.equals(event.getPath()) && addData(event.getPath())) {
            redeliverResult();
          }
          break;
        case DELETE:
          if (data.remove(event.getPath()) != null) {
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
