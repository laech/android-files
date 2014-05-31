package l.files.operations;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;

import com.google.common.base.Function;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import l.files.common.testing.FileBaseTest;

import static com.google.common.collect.Collections2.transform;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.common.testing.matchers.FileMatchers.exists;
import static l.files.operations.Progress.Delete.getDeletedItemCount;
import static l.files.operations.Progress.Delete.getTotalItemCount;
import static l.files.operations.Progress.STATUS_FINISHED;
import static l.files.operations.Progress.getTaskId;
import static l.files.operations.Progress.getTaskStartTime;
import static l.files.operations.Progress.getTaskStatus;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;

public final class OperationServiceTest extends FileBaseTest {

  private Handler handler;

  @Override protected void setUp() throws Exception {
    super.setUp();
    handler = new Handler(Looper.getMainLooper());
  }

  public void testDeletesFiles() throws Exception {
    File file1 = tmp().createFile("a");
    File file2 = tmp().createFile("b/c");

    final CountDownLatch latch = new CountDownLatch(1);
    BroadcastReceiver listener = new BroadcastReceiver() {
      @Override public void onReceive(Context context, Intent intent) {
        if (getTaskStatus(intent) == STATUS_FINISHED) {
          assertEquals(getTotalItemCount(intent), getDeletedItemCount(intent));
          latch.countDown();
        }
      }
    };

    register(listener, Progress.Delete.ACTION);
    try {

      OperationService.delete(getContext(), file1.getPath(), file2.getPath());
      assertThat(latch.await(1, SECONDS), is(true));
      assertThat(file1, not(exists()));
      assertThat(file2, not(exists()));

    } finally {
      unregister(listener);
    }
  }

  public void testTaskIdIsUnique() throws Exception {
    final List<Intent> intents = new ArrayList<>();
    final CountDownLatch latch = new CountDownLatch(2);
    BroadcastReceiver receiver = new BroadcastReceiver() {
      @Override public void onReceive(Context context, Intent intent) {
        intents.add(intent);
        if (getTaskStatus(intent) == STATUS_FINISHED) {
          latch.countDown();
        }
      }
    };

    register(receiver, Progress.Delete.ACTION);
    try {

      OperationService.delete(getContext(), tmp().createFile("a").getPath());
      OperationService.delete(getContext(), tmp().createFile("2").getPath());
      assertThat(latch.await(1, SECONDS), is(true));
      assertThat(intents.size(), greaterThan(1));
      assertThat(getTaskIds(intents), hasSize(2));

    } finally {
      unregister(receiver);
    }
  }

  private Set<Integer> getTaskIds(List<Intent> intents) {
    return new HashSet<>(transform(intents,
        new Function<Intent, Integer>() {
          @Override public Integer apply(Intent intent) {
            return getTaskId(intent);
          }
        }
    ));
  }

  public void testTaskStartTimeIsCorrect() throws Exception {
    String path1 = tmp().createFile("a").getPath();
    String path2 = tmp().createFile("b").getPath();

    final List<Intent> intents = new ArrayList<>();
    final CountDownLatch latch = new CountDownLatch(1);
    BroadcastReceiver receiver = new BroadcastReceiver() {
      @Override public void onReceive(Context context, Intent intent) {
        intents.add(intent);
        if (getTaskStatus(intent) == STATUS_FINISHED) {
          latch.countDown();
        }
      }
    };

    register(receiver, Progress.Delete.ACTION);
    try {

      long start = currentTimeMillis();
      {
        OperationService.delete(getContext(), path1, path2);
        assertThat(latch.await(1, SECONDS), is(true));
      }
      long end = currentTimeMillis();

      Set<Long> times = getTaskStartTimes(intents);
      assertThat(times.size(), is(1));
      assertThat(times.iterator().next(), greaterThanOrEqualTo(start));
      assertThat(times.iterator().next(), lessThanOrEqualTo(end));

    } finally {
      unregister(receiver);
    }
  }

  private Set<Long> getTaskStartTimes(List<Intent> intents) {
    return new HashSet<>(transform(intents,
        new Function<Intent, Long>() {
          @Override public Long apply(Intent intent) {
            return getTaskStartTime(intent);
          }
        }
    ));
  }

  private void register(final BroadcastReceiver receiver, final String action)
      throws InterruptedException {
    runOnMainThread(new Runnable() {
      @Override public void run() {
        getContext().registerReceiver(receiver, new IntentFilter(action),
            Permissions.SEND_PROGRESS, null);
      }
    });
  }

  private void unregister(final BroadcastReceiver receiver)
      throws InterruptedException {
    runOnMainThread(new Runnable() {
      @Override public void run() {
        getContext().unregisterReceiver(receiver);
      }
    });
  }

  private void runOnMainThread(final Runnable fn) throws InterruptedException {
    final CountDownLatch latch = new CountDownLatch(1);
    handler.post(new Runnable() {
      @Override public void run() {
        fn.run();
        latch.countDown();
      }
    });
    assertTrue(latch.await(1, SECONDS));
  }
}

