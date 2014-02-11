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
import java.util.List;
import java.util.Set;

import l.files.common.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Sets.newHashSet;

final class Processor implements Runnable {

  private static final Logger logger = Logger.get(Processor.class);

  private static final int BATCH_DELAY_MILLIS = 200;

  private final Handler handler = new Handler(Looper.getMainLooper()) {
    @Override public void handleMessage(Message msg) {
      super.handleMessage(msg);
      // TODO use another executor
      AsyncTask.SERIAL_EXECUTOR.execute(Processor.this);
    }
  };

  private final Set<Uri> notifications;
  private final List<Runnable> operations;
  private final SQLiteOpenHelper helper;
  private final ContentResolver resolver;

  Processor(SQLiteOpenHelper helper, ContentResolver resolver) {
    this.helper = checkNotNull(helper, "helper");
    this.resolver = checkNotNull(resolver, "resolver");
    this.operations = newLinkedList();
    this.notifications = newHashSet();
  }

  /**
   * Posts an operation to be executed, notifies the given uri afterward.
   */
  public void post(Runnable operation, Uri notification) {
    synchronized (this) {
      operations.remove(operation); // TODO necessary?
      operations.add(operation);
      notifications.add(notification);
    }
    handler.removeMessages(0);
    handler.sendEmptyMessageDelayed(0, BATCH_DELAY_MILLIS);
  }

  @Override public void run() {
    Collection<Runnable> operations;
    Collection<Uri> notifications;
    synchronized (Processor.this) {
      operations = newArrayList(Processor.this.operations);
      notifications = newArrayList(Processor.this.notifications);
      Processor.this.operations.clear();
      Processor.this.notifications.clear();
    }

    logger.debug("Begin transaction.");
    SQLiteDatabase db = helper.getWritableDatabase();
    db.beginTransaction();
    try {
      for (Runnable operation : operations) {
        operation.run();
      }
      db.setTransactionSuccessful();
    } finally {
      db.endTransaction();
      logger.debug("End transaction.");
    }

    for (Uri uri : notifications) {
      resolver.notifyChange(uri, null);
      logger.debug("Notify change: %s", uri);
    }
  }
}
