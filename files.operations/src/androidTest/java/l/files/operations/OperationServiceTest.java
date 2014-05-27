package l.files.operations;

import android.os.Handler;
import android.os.Looper;

import com.google.common.base.Function;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

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
import static l.files.operations.Progress.State.FINISHED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;

public final class OperationServiceTest extends FileBaseTest {

  private Bus bus;
  private Handler handler;

  @Override protected void setUp() throws Exception {
    super.setUp();
    handler = new Handler(Looper.getMainLooper());
    bus = EventBus.get();
  }

  public void testDeletesFiles() throws Exception {
    File file1 = tmp().createFile("a");
    File file2 = tmp().createFile("b/c");

    final CountDownLatch latch = new CountDownLatch(1);
    Object listener = new Object() {
      @Subscribe public void on(DeleteProgress progress) {
        if (progress.state() == FINISHED) {
          assertEquals(progress.totalItemCount(), progress.deletedItemCount());
          latch.countDown();
        }
      }
    };

    register(listener);
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
    final List<DeleteProgress> progresses = new ArrayList<>();
    final CountDownLatch latch = new CountDownLatch(2);
    Object listener = new Object() {
      @Subscribe public synchronized void on(DeleteProgress progress) {
        progresses.add(progress);
        if (progress.state() == FINISHED) {
          latch.countDown();
        }
      }
    };

    register(listener);
    try {

      OperationService.delete(getContext(), tmp().createFile("a").getPath());
      OperationService.delete(getContext(), tmp().createFile("2").getPath());
      assertThat(latch.await(1, SECONDS), is(true));
      assertThat(progresses.size(), greaterThan(1));
      assertThat(getTaskIds(progresses), hasSize(2));

    } finally {
      unregister(listener);
    }
  }

  private Set<Integer> getTaskIds(List<DeleteProgress> progresses) {
    return new HashSet<>(transform(progresses,
        new Function<DeleteProgress, Integer>() {
          @Override public Integer apply(DeleteProgress progress) {
            return progress.taskId();
          }
        }
    ));
  }

  public void testTaskStartTimeIsCorrect() throws Exception {
    String path1 = tmp().createFile("a").getPath();
    String path2 = tmp().createFile("b").getPath();

    final List<DeleteProgress> progresses = new ArrayList<>();
    final CountDownLatch latch = new CountDownLatch(1);
    Object listener = new Object() {
      @Subscribe public synchronized void on(DeleteProgress progress) {
        progresses.add(progress);
        if (progress.state() == FINISHED) {
          latch.countDown();
        }
      }
    };

    register(listener);
    try {

      long start = currentTimeMillis();
      {
        OperationService.delete(getContext(), path1, path2);
        assertThat(latch.await(1, SECONDS), is(true));
      }
      long end = currentTimeMillis();

      Set<Long> times = getTaskStartTimes(progresses);
      assertThat(times.size(), is(1));
      assertThat(times.iterator().next(), greaterThanOrEqualTo(start));
      assertThat(times.iterator().next(), lessThanOrEqualTo(end));

    } finally {
      unregister(listener);
    }
  }

  private Set<Long> getTaskStartTimes(List<DeleteProgress> progresses) {
    return new HashSet<>(transform(progresses,
        new Function<DeleteProgress, Long>() {
          @Override public Long apply(DeleteProgress progress) {
            return progress.taskStartTime();
          }
        }
    ));
  }

  private void register(final Object listener) throws InterruptedException {
    runOnMainThread(new Runnable() {
      @Override public void run() {
        bus.register(listener);
      }
    });
  }

  private void unregister(final Object listener) throws InterruptedException {
    runOnMainThread(new Runnable() {
      @Override public void run() {
        bus.unregister(listener);
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

