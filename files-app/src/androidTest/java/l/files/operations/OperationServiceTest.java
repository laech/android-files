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
import l.files.fs.local.LocalPath;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.singleton;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.common.testing.Tests.assertExists;
import static l.files.common.testing.Tests.assertNotExists;
import static l.files.operations.OperationService.ACTION_CANCEL;
import static l.files.operations.OperationService.EXTRA_TASK_ID;
import static l.files.operations.OperationService.copy;
import static l.files.operations.OperationService.delete;
import static l.files.operations.OperationService.move;
import static l.files.operations.OperationService.newCancelIntent;
import static l.files.operations.TaskKind.COPY;
import static l.files.operations.TaskKind.DELETE;
import static l.files.operations.TaskKind.MOVE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class OperationServiceTest extends FileBaseTest {

  public void testCancelIntent() throws Exception {
    Intent intent = newCancelIntent(getContext(), 101);
    assertEquals(ACTION_CANCEL, intent.getAction());
    assertEquals(101, intent.getIntExtra(EXTRA_TASK_ID, -1));
    assertEquals(new ComponentName(getContext(), OperationService.class),
        intent.getComponent());
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

      move(getContext(), singleton(LocalPath.of(src)), LocalPath.of(dst));
      listener.await();
      assertNotExists(src);
      assertExists(new File(dst, src.getName()));

    } finally {
      unregister(listener);
    }
  }

  public void testCopiesFile() throws Exception {
    File src = tmp().createFile("a");
    File dst = tmp().createDir("dst");
    CountDownListener listener = register(new CountDownListener(COPY));
    try {

      copy(getContext(), singleton(LocalPath.of(src)), LocalPath.of(dst));
      listener.await();
      assertExists(src);
      assertExists(new File(dst, src.getName()));

    } finally {
      unregister(listener);
    }
  }

  public void testDeletesFiles() throws Exception {
    File a = tmp().createFile("a");
    File b = tmp().createFile("b/c");
    CountDownListener listener = register(new CountDownListener(DELETE));
    try {

      delete(getContext(), newHashSet(LocalPath.of(a), LocalPath.of(b)));
      listener.await();
      assertNotExists(a);
      assertNotExists(b);

    } finally {
      unregister(listener);
    }
  }

  public void testTaskIdIsUnique() throws Exception {
    CountDownListener listener = register(new CountDownListener(DELETE, 2));
    try {

      delete(getContext(), singleton(LocalPath.of(tmp().createFile("a"))));
      delete(getContext(), singleton(LocalPath.of(tmp().createFile("2"))));
      listener.await();
      assertTrue(listener.getValues().size() > 1);
      assertEquals(2, getTaskIds(listener.getValues()).size());

    } finally {
      unregister(listener);
    }
  }

  private Set<Integer> getTaskIds(List<? extends TaskState> values) {
    return new HashSet<>(transform(values, new Function<TaskState, Integer>() {
          @Override public Integer apply(TaskState state) {
            return state.getTask().getId();
          }
        }
    ));
  }

  public void testTaskStartTimeIsCorrect() throws Exception {
    File file1 = tmp().createFile("a");
    File file2 = tmp().createFile("b");
    CountDownListener listener = register(new CountDownListener(DELETE));
    try {

      long start = currentTimeMillis();
      {
        delete(getContext(), newHashSet(LocalPath.of(file1), LocalPath.of(file2)));
        listener.await();
      }
      long end = currentTimeMillis();

      Set<Long> times = getTaskStartTimes(listener.getValues());
      assertTrue(times.iterator().next() >= start);
      assertTrue(times.iterator().next() <= end);

    } finally {
      unregister(listener);
    }
  }

  private Set<Long> getTaskStartTimes(List<? extends TaskState> values) {
    return new HashSet<>(transform(values, new Function<TaskState, Long>() {
          @Override public Long apply(TaskState value) {
            return value.getTime().getTime();
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
      assertEquals(kind, state.getTask().getKind());
      if (state.isFinished()) {
        latch.countDown();
      }
    }

    public void await() throws InterruptedException {
      assertTrue(latch.await(1, SECONDS));
    }

    public List<TaskState> getValues() {
      return values;
    }
  }
}
