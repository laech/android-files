package l.files.operations;

import com.google.common.base.Function;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import l.files.common.testing.FileBaseTest;
import l.files.eventbus.Subscribe;

import static com.google.common.collect.Collections2.transform;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.operations.OperationService.copy;
import static l.files.operations.OperationService.delete;
import static l.files.operations.OperationService.move;
import static l.files.operations.TaskInfo.TaskStatus.FINISHED;
import static org.assertj.core.api.Assertions.assertThat;

public final class OperationServiceTest extends FileBaseTest {

  public void testMovesFile() throws Exception {
    File src = tmp().createFile("a");
    File dst = tmp().createDir("dst");
    CountDownListener listener = register(new CountDownListener(MoveTaskInfo.class));
    try {

      move(getContext(), asList(src.getPath()), dst.getPath());
      listener.await();
      assertThat(src).doesNotExist();
      assertThat(new File(dst, src.getName())).exists();

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
      assertThat(src).exists();
      assertThat(new File(dst, src.getName())).exists();

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
      assertThat(a).doesNotExist();
      assertThat(b).doesNotExist();

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
      assertThat(listener.getValues().size()).isGreaterThan(1);
      assertThat(getTaskIds(listener.getValues())).hasSize(2);

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
      assertThat(times).hasSize(1);
      assertThat(times.iterator().next()).isGreaterThanOrEqualTo(start);
      assertThat(times.iterator().next()).isLessThanOrEqualTo(end);

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

    @Subscribe public void onEvent(ProgressInfo value) {
      values.add(value);
      assertThat(value).isInstanceOf(clazz);
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
