package l.files.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import l.files.io.file.WatchEvent;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.concurrent.TimeUnit.SECONDS;

final class EventBatch implements Runnable {

  private static final int BATCH_DELAY_MILLIS = 200;

  private final Executor executor =
      new ThreadPoolExecutor(0, 1, 1L, SECONDS, new LinkedBlockingQueue<Runnable>());

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
  private LinkedHashSet<WatchEvent> events;
  private Set<Uri> notifications;

  private final Processor processor;
  private final ContentResolver resolver;

  EventBatch(ContentResolver resolver, Processor processor) {
    this.resolver = checkNotNull(resolver, "resolver");
    this.processor = checkNotNull(processor, "processor");
    init();
  }

  private void init() {
    synchronized (this) {
      this.events = newLinkedHashSet();
      this.notifications = newHashSet();
    }
  }

  public void batch(WatchEvent event, Uri notificationUri) {
    synchronized (this) {
      events.remove(event);
      events.add(event);
      notifications.add(notificationUri);
    }
    // TODO this has the potential to never execute if events are flooding in non stop
    handler.removeMessages(0);
    handler.sendEmptyMessageDelayed(0, BATCH_DELAY_MILLIS);
  }

  @Override public void run() {
    Collection<WatchEvent> events;
    Collection<Uri> notifications;
    synchronized (EventBatch.this) {
      events = EventBatch.this.events;
      notifications = EventBatch.this.notifications;
      init();
    }

    for (WatchEvent event : events) {
      processor.process(event);
    }

    for (Uri uri : notifications) {
      resolver.notifyChange(uri, null);
    }
  }

  static interface Processor {
    void process(WatchEvent event);
  }
}
