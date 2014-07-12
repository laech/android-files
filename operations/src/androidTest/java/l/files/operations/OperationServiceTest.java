package l.files.operations;

import com.google.common.base.Function;
import com.google.common.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import l.files.common.testing.FileBaseTest;
import l.files.operations.info.CopyTaskInfo;
import l.files.operations.info.DeleteTaskInfo;
import l.files.operations.info.MoveTaskInfo;
import l.files.operations.info.ProgressInfo;
import l.files.operations.info.TaskInfo;

import static com.google.common.collect.Collections2.transform;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.operations.OperationService.copy;
import static l.files.operations.OperationService.delete;
import static l.files.operations.OperationService.move;
import static l.files.operations.info.TaskInfo.TaskStatus.FINISHED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

public final class OperationServiceTest extends FileBaseTest {

  public void testMovesFile() throws Exception {
    File src = tmp().createFile("a");
    File dst = tmp().createDir("dst");
    CountDownListener listener = register(new CountDownListener(MoveTaskInfo.class));
    try {

      move(getContext(), asList(src.getPath()), dst.getPath());
      listener.await();
      assertThat(src.exists(), is(false));
      assertThat(new File(dst, src.getName()).exists(), is(true));

    } finally {
      unregister(listener);
    }
  }

  public void testCopiesFile() throws Exception {
    File src = tmp().createFile("a");
    File dst = tmp().createDir("dst");
    CountDownListener listener = register(new CountDownListener(CopyTaskInfo.class));
    try {

      copy(getContext(), asList(src.getPath()), dst.getPath());
      listener.await();
      assertThat(src.exists(), is(true));
      assertThat(new File(dst, src.getName()).exists(), is(true));

    } finally {
      unregister(listener);
    }
  }

  public void testDeletesFiles() throws Exception {
    File a = tmp().createFile("a");
    File b = tmp().createFile("b/c");
    CountDownListener listener = register(new CountDownListener(DeleteTaskInfo.class));
    try {

      delete(getContext(), a.getPath(), b.getPath());
      listener.await();
      assertThat(a.exists(), is(false));
      assertThat(b.exists(), is(false));

    } finally {
      unregister(listener);
    }
  }

  public void testTaskIdIsUnique() throws Exception {
    CountDownListener listener = register(new CountDownListener(DeleteTaskInfo.class, 2));
    try {

      delete(getContext(), tmp().createFile("a").getPath());
      delete(getContext(), tmp().createFile("2").getPath());
      listener.await();
      assertThat(listener.getValues().size(), greaterThan(1));
      assertThat(getTaskIds(listener.getValues()), hasSize(2));

    } finally {
      unregister(listener);
    }
  }

  private Set<Integer> getTaskIds(List<? extends TaskInfo> values) {
    return new HashSet<>(transform(values, new Function<TaskInfo, Integer>() {
          @Override public Integer apply(TaskInfo task) {
            return task.getTaskId();
          }
        }
    ));
  }

  public void testTaskStartTimeIsCorrect() throws Exception {
    String path1 = tmp().createFile("a").getPath();
    String path2 = tmp().createFile("b").getPath();
    CountDownListener listener = register(new CountDownListener(DeleteTaskInfo.class));
    try {

      long start = currentTimeMillis();
      {
        delete(getContext(), path1, path2);
        listener.await();
      }
      long end = currentTimeMillis();

      Set<Long> times = getTaskStartTimes(listener.getValues());
      assertThat(times.size(), is(1));
      assertThat(times.iterator().next(), greaterThanOrEqualTo(start));
      assertThat(times.iterator().next(), lessThanOrEqualTo(end));

    } finally {
      unregister(listener);
    }
  }

  private Set<Long> getTaskStartTimes(List<? extends TaskInfo> values) {
    return new HashSet<>(transform(values, new Function<TaskInfo, Long>() {
          @Override public Long apply(TaskInfo value) {
            return value.getTaskStartTime();
          }
        }
    ));
  }

  private <T> T register(T listener) {
    Events.get().register(listener);
    return listener;
  }

  private void unregister(Object listener) {
    Events.get().unregister(listener);
  }

  private static class CountDownListener {
    private final CountDownLatch latch;
    private final Class<ProgressInfo> clazz;
    private final List<ProgressInfo> values;

    private CountDownListener(Class<? extends ProgressInfo> clazz) {
      this(clazz, 1);
    }

    @SuppressWarnings("unchecked")
    private CountDownListener(Class<? extends ProgressInfo> clazz, int countDowns) {
      this.latch = new CountDownLatch(countDowns);
      this.values = new ArrayList<>(countDowns);
      this.clazz = (Class<ProgressInfo>) clazz;
    }

    @Subscribe public void on(ProgressInfo value) {
      values.add(value);
      assertThat(value, instanceOf(clazz));
      if (value.getTaskStatus() == FINISHED) {
        assertEquals(value.getTotalItemCount(), value.getProcessedItemCount());
        assertEquals(value.getTotalByteCount(), value.getProcessedByteCount());
        latch.countDown();
      }
    }

    public void await() throws InterruptedException {
      assertTrue(latch.await(1, SECONDS));
    }

    public List<ProgressInfo> getValues() {
      return values;
    }
  }
}
