package l.files.provider;

import android.content.ContentResolver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;

final class Processor implements Runnable {

  private static final int BATCH_DELAY_MILLIS = 200;

  private final Handler handler = new Handler(Looper.getMainLooper()) {
    @Override public void handleMessage(Message msg) {
      super.handleMessage(msg);
      // TODO use another executor
      AsyncTask.SERIAL_EXECUTOR.execute(Processor.this);
    }
  };

  private Set<Uri> notifications;
  private List<Runnable> operations;

  private final SQLiteOpenHelper helper;
  private final ContentResolver resolver;

  Processor(SQLiteOpenHelper helper, ContentResolver resolver) {
    this.helper = checkNotNull(helper, "helper");
    this.resolver = checkNotNull(resolver, "resolver");
    init();
  }

  private void init() {
    synchronized (this) {
      this.operations = newLinkedList();
      this.notifications = newHashSet();
    }
  }

  /**
   * Posts an operation to be executed, notifies the given uri afterward.
   */
  public void post(Runnable operation, Uri... notificationUris) {
    synchronized (this) {
//      operations.remove(operation); // TODO necessary?
      operations.add(operation);
      notifications.addAll(asList(notificationUris));
    }
    // TODO handle too frequent events causing size of queue to increase infinitely and never executes
    handler.removeMessages(0);
    handler.sendEmptyMessageDelayed(0, BATCH_DELAY_MILLIS);
  }

  @Override public void run() {
    Collection<Runnable> operations;
    Collection<Uri> notifications;
    synchronized (Processor.this) {
      operations = Processor.this.operations;
      notifications = Processor.this.notifications;
      init();
    }

    SQLiteDatabase db = helper.getWritableDatabase();
    db.beginTransaction();
    try {
      Iterator<Runnable> it = operations.iterator();
      int count = 0;
      while (it.hasNext()) {
        it.next().run();
        it.remove();
        if (++count % 500 == 0) {
          db.yieldIfContendedSafely();
        }
      }
      for (Runnable operation : operations) {
        operation.run();
      }
      db.setTransactionSuccessful();
    } finally {
      db.endTransaction();
    }

    for (Uri uri : notifications) {
      resolver.notifyChange(uri, null);
    }
  }
}
