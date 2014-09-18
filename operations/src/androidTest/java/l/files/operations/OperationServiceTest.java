package l.files.operations;

import android.content.ComponentName;
import android.content.Intent;

import com.google.common.base.Function;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import de.greenrobot.event.EventBus;
import l.files.common.testing.FileBaseTest;
import l.files.eventbus.Subscribe;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.truth.Truth.ASSERT;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.common.testing.util.concurrent.CountDownSubject.countDown;
import static l.files.operations.OperationService.ACTION_CANCEL;
import static l.files.operations.OperationService.EXTRA_TASK_ID;
import static l.files.operations.OperationService.copy;
import static l.files.operations.OperationService.delete;
import static l.files.operations.OperationService.move;
import static l.files.operations.OperationService.newCancelIntent;
import static l.files.operations.TaskKind.COPY;
import static l.files.operations.TaskKind.DELETE;
import static l.files.operations.TaskKind.MOVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class OperationServiceTest extends FileBaseTest {

  public void testCancelIntent() throws Exception {
    Intent intent = newCancelIntent(getContext(), 101);
    ASSERT.that(intent.getAction()).is(ACTION_CANCEL);
    ASSERT.that(intent.getIntExtra(EXTRA_TASK_ID, -1)).is(101);
    ASSERT.that(intent.getComponent())
        .is(new ComponentName(getContext(), OperationService.class));
  }

  public void testCancelTaskNotFound() throws Exception {
    OperationService service = new OperationService();
    service.onCreate();
    service.bus = mock(EventBus.class);
    service.onStartCommand(newCancelIntent(getContext(), 1011), 0, 0);
    verify(service.bus).post(TaskNotFound.create(1011));
  }

  public void testMovesFile() throws Exception {
    File src = tmp().createFile("a");
    File dst = tmp().createDir("dst");
    CountDownListener listener = register(new CountDownListener(MOVE));
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
    CountDownListener listener = register(new CountDownListener(COPY));
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
    CountDownListener listener = register(new CountDownListener(DELETE));
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
    CountDownListener listener = register(new CountDownListener(DELETE, 2));
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

  private Set<Integer> getTaskIds(List<? extends TaskState> values) {
    return new HashSet<>(transform(values, new Function<TaskState, Integer>() {
          @Override public Integer apply(TaskState state) {
            return state.task().id();
          }
        }
    ));
  }

  public void testTaskStartTimeIsCorrect() throws Exception {
    String path1 = tmp().createFile("a").getPath();
    String path2 = tmp().createFile("b").getPath();
    CountDownListener listener = register(new CountDownListener(DELETE));
    try {

      long start = currentTimeMillis();
      {
        delete(getContext(), path1, path2);
        listener.await();
      }
      long end = currentTimeMillis();

      Set<Long> times = getTaskStartTimes(listener.getValues());
      assertThat(times.iterator().next()).isGreaterThanOrEqualTo(start);
      assertThat(times.iterator().next()).isLessThanOrEqualTo(end);

    } finally {
      unregister(listener);
    }
  }

  private Set<Long> getTaskStartTimes(List<? extends TaskState> values) {
    return new HashSet<>(transform(values, new Function<TaskState, Long>() {
          @Override public Long apply(TaskState value) {
            return value.time().time();
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
    private final TaskKind kind;
    private final List<TaskState> values;

    private CountDownListener(TaskKind kind) {
      this(kind, 1);
    }

    private CountDownListener(TaskKind kind, int countDowns) {
      this.latch = new CountDownLatch(countDowns);
      this.values = new ArrayList<>(countDowns);
      this.kind = kind;
    }

    @Subscribe public void onEvent(TaskState state) {
      values.add(state);
      ASSERT.that(state.task().kind()).is(kind);
      if (state.isFinished()) {
        latch.countDown();
      }
    }

    public void await() throws InterruptedException {
      ASSERT.about(countDown()).that(latch).await(1, SECONDS);
    }

    public List<TaskState> getValues() {
      return values;
    }
  }
}
