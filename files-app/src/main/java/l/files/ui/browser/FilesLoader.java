package l.files.ui.browser;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.OperationCanceledException;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import l.files.fs.Event;
import l.files.fs.Observer;
import l.files.fs.Resource;
import l.files.fs.Stat;
import l.files.fs.Visitor;
import l.files.logging.Logger;
import l.files.ui.browser.FileListItem.File;

import static android.os.Looper.getMainLooper;
import static java.lang.System.nanoTime;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Visitor.Result.CONTINUE;

public final class FilesLoader extends AsyncTaskLoader<FilesLoader.Result> {

  private static final Logger log = Logger.get(FilesLoader.class);
  private static final Handler handler = new Handler(getMainLooper());

  private final ConcurrentMap<String, File> data;
  private final Resource root;
  private final Collator collator;

  private volatile FileSort sort;
  private volatile boolean showHidden;
  private volatile boolean forceReload;

  private volatile boolean observing;
  private volatile Closeable observable;

  private final ScheduledExecutorService executor;

  private final Set<String> childrenPendingUpdates;

  private final Runnable childrenPendingUpdatesRun = new Runnable() {
    long lastUpdateNanoTime = nanoTime();

    @Override public void run() {
      /*
       * Don't update if last update was less than a second ago,
       * this avoid updating too frequently due to resources in the
       * current directory being changed frequently by other processes,
       * but since user triggered operation are usually seconds apart,
       * those actions will still be updated instantly.
       */
      long now = nanoTime();
      if (!forceReload && now - lastUpdateNanoTime < SECONDS.toNanos(1)) {
        return;
      }

      String[] children;
      synchronized (FilesLoader.this) {
        if (!forceReload && childrenPendingUpdates.isEmpty()) {
          return;
        }
        children = new String[childrenPendingUpdates.size()];
        childrenPendingUpdates.toArray(children);
        childrenPendingUpdates.clear();
      }

      lastUpdateNanoTime = now;

      boolean changed = false;
      for (String child : children) {
        changed |= update(child);
      }

      if (changed || forceReload) {
        final Result result = buildResult();
        handler.post(new Runnable() {
          @Override public void run() {
            deliverResult(result);
          }
        });
      }

      forceReload = false;
    }
  };

  private final Observer listener = new Observer() {
    @Override public void onEvent(Event event, String child) {
      if (child != null) {
        synchronized (FilesLoader.this) {
          childrenPendingUpdates.add(child);
        }
      }
    }
  };

  private final AtomicInteger approximateChildTotal = new AtomicInteger(0);

  public FilesLoader(
      Context context,
      Resource root,
      FileSort sort,
      Collator collator,
      boolean showHidden) {
    super(context);

    this.root = requireNonNull(root, "root");
    this.sort = requireNonNull(sort, "sort");
    this.collator = requireNonNull(collator, "collator");
    this.showHidden = showHidden;
    this.data = new ConcurrentHashMap<>();
    this.childrenPendingUpdates = new HashSet<>();
    this.executor = newSingleThreadScheduledExecutor();
  }

  public int approximateChildTotal() {
    return approximateChildTotal.get();
  }

  public int approximateChildLoaded() {
    return data.size();
  }

  public void setSort(FileSort sort) {
    this.sort = requireNonNull(sort, "sort");
    this.forceReload = true;
  }

  public void setShowHidden(boolean showHidden) {
    this.showHidden = showHidden;
    this.forceReload = true;
  }

  @Override protected void onStartLoading() {
    super.onStartLoading();
    if (data.isEmpty()) {
      log.debug("forceLoad");
      forceLoad();
    } else {
      forceReload = true;
      log.debug("forceReload");
    }
  }

  @Override public Result loadInBackground() {
    data.clear();

    boolean observe = false;
    synchronized (this) {
      if (!observing) {
        observing = true;
        observe = true;
      }
    }

    List<Resource> children;
    try {
      if (observe) {
        children = observe();
        executor.scheduleWithFixedDelay(
            childrenPendingUpdatesRun, 80, 80, MILLISECONDS);
      } else {
        children = visit();
      }
    } catch (IOException e) {
      log.debug(e);
      return Result.of(e);
    }

    update(children);
    return buildResult();
  }

  private List<Resource> observe() throws IOException {
    log.verbose("observe start");
    List<Resource> children = new ArrayList<>();
    observable = root.observe(FOLLOW, listener, collectInto(children));
    log.verbose("observe end");
    return children;
  }

  private List<Resource> visit() throws IOException {
    log.verbose("visit start");
    List<Resource> children = new ArrayList<>();
    root.list(FOLLOW, collectInto(children));
    log.verbose("visit end");
    return children;
  }

  private Visitor collectInto(final List<Resource> children) {
    return new Visitor() {
      @Override
      public Result accept(Resource resource) throws IOException {
        checkCancel();
        approximateChildTotal.incrementAndGet();
        children.add(resource);
        return CONTINUE;
      }
    };
  }

  private void update(List<Resource> children) {
    log.verbose("update start");
    for (Resource child : children) {
      checkCancel();
      update(child);
    }
    log.verbose("update end");
  }

  private void checkCancel() {
    if (isLoadInBackgroundCanceled()) {
      throw new OperationCanceledException();
    }
  }

  private Result buildResult() {
    log.verbose("buildResult start");
    List<File> files = new ArrayList<>(data.size());
    if (showHidden) {
      files.addAll(data.values());
    } else {
      for (File item : data.values()) {
        if (!item.resource().hidden()) {
          files.add(item);
        }
      }
    }
    Resources res = getContext().getResources();
    List<FileListItem> result = sort.sort(files, res);
    log.verbose("buildResult end");
    return Result.of(result);
  }

  @Override protected void onReset() {
    super.onReset();

    Closeable closeable = null;
    synchronized (this) {
      if (observing) {
        closeable = observable;
        observable = null;
        observing = false;
      }
    }

    if (closeable != null) {
      executor.shutdownNow();
      try {
        closeable.close();
      } catch (IOException e) {
        log.warn(e);
      }
    }

    data.clear();
  }

  @Override protected void finalize() throws Throwable {
    super.finalize();

    Closeable closeable = null;
    synchronized (this) {
      if (observing) {
        closeable = observable;
        observable = null;
        observing = false;
      }
    }
    if (closeable != null) {
      log.error("Has not been unregistered");
      executor.shutdownNow();
      closeable.close();
    }
  }

  /**
   * Adds the new status of the given path to the data map. Returns true if
   * the data map is changed.
   */
  private boolean update(String child) {
    return update(root.resolve(child));
  }

  private boolean update(Resource resource) {
    try {
      Stat stat = resource.stat(NOFOLLOW);
      Stat targetStat = readTargetStatus(resource, stat);
      File newStat = File.create(resource, stat, targetStat, collator);
      File oldStat = data.put(resource.name().toString(), newStat);
      return !Objects.equals(newStat, oldStat);
    } catch (FileNotFoundException e) {
      return data.remove(resource.name().toString()) != null;
    } catch (IOException e) {
      data.put(
          resource.name().toString(),
          File.create(resource, null, null, collator));
      return true;
    }
  }

  private Stat readTargetStatus(Resource resource, Stat stat) {
    if (stat.isSymbolicLink()) {
      try {
        return resource.stat(FOLLOW);
      } catch (IOException e) {
        log.debug(e);
      }
    }
    return stat;
  }

  @AutoValue
  static abstract class Result {
    Result() {
    }

    abstract List<FileListItem> items();

    @Nullable abstract IOException exception();

    private static Result of(IOException exception) {
      return new AutoValue_FilesLoader_Result(
          Collections.<FileListItem>emptyList(), exception);
    }

    private static Result of(List<FileListItem> result) {
      return new AutoValue_FilesLoader_Result(result, null);
    }
  }

}
