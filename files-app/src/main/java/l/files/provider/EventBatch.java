package l.files.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import l.files.fs.local.WatchEvent;
import l.files.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.concurrent.TimeUnit.SECONDS;

final class EventBatch implements Runnable {

  private static final Logger logger = Logger.get(EventBatch.class);

  private static final int BATCH_DELAY_MILLIS = 200;

  private static final Executor executor = new ThreadPoolExecutor(
      0, 1, 1L, SECONDS, new LinkedBlockingQueue<Runnable>());

  private final Handler handler = new Handler(Looper.getMainLooper()) {
    @Override public void handleMessage(Message msg) {
      super.handleMessage(msg);
      executor.execute(EventBatch.this);
    }
  };

  /**
   * Pending events to be processed, only the latest version of the events will
   * be kept, for example, if batch is called with events in the following
   * sequence:
   * <p/>
   * {@code a, b, a}
   * <p/>
   * then the final values in the map will be
   * <p/>
   * {@code b, a}
   * <p/>
   * the first event {@code a} has been removed because a newer event {@code a}
   * is added. Hence this is declared as a LinkedHashSet as the order is
   * important.
   */
  private final LinkedHashSet<WatchEvent> events;
  private final ContentResolver resolver;
  private final Uri notificationUri;
  private final Processor processor;

  EventBatch(ContentResolver resolver, Uri notificationUri, Processor processor) {
    this.resolver = checkNotNull(resolver, "resolver");
    this.notificationUri = checkNotNull(notificationUri, "notificationUri");
    this.processor = checkNotNull(processor, "processor");
    this.events = new LinkedHashSet<>();
  }

  public void batch(WatchEvent event) {
    synchronized (this) {
      events.remove(event);
      events.add(event);
    }
    handler.removeMessages(0);
    handler.sendEmptyMessageDelayed(0, BATCH_DELAY_MILLIS);
  }

  @Override public void run() {
    Collection<WatchEvent> events;
    synchronized (EventBatch.this) {
      events = new ArrayList<>(EventBatch.this.events);
      this.events.clear();
    }

    boolean changed = false;
    for (WatchEvent event : events) {
      changed |= processor.process(event);
    }
    if (changed) {
      resolver.notifyChange(notificationUri, null);
      logger.debug("Notifying %s", notificationUri);
    }
  }

  static interface Processor {
    /**
     * Returns true if notification should be sent for the given event.
     */
    boolean process(WatchEvent event);
  }
}
