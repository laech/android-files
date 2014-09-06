package l.files.operations;

import android.os.Handler;

import java.util.concurrent.CountDownLatch;

import de.greenrobot.event.EventBus;
import l.files.common.testing.BaseTest;
import l.files.eventbus.Subscribe;
import l.files.operations.info.TaskInfo;

import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.operations.info.TaskInfo.TaskStatus;
import static l.files.operations.info.TaskInfo.TaskStatus.FINISHED;
import static l.files.operations.info.TaskInfo.TaskStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;

public class TaskTest extends BaseTest {

  private EventBus bus;
  private Handler handler;

  @Override protected void setUp() throws Exception {
    super.setUp();
    bus = new EventBus();
    handler = new Handler();
  }

  public void testDoInBackgroundReturnsEmptyFailuresOnSuccess() {
    Task task = create(0, bus, handler);
    task.run();
    assertThat(task.getFailures()).isNotNull().isEmpty();
  }

  public void testEmptyFailuresOnError() throws Exception {
    class TestException extends RuntimeException {}
    Task task = new Task(0, bus, handler) {
      @Override protected void doTask() {
        throw new TestException();
      }
    };
    try {
      task.run();
    } catch (TestException e) {
      // Ignored
    }
    assertThat(task.getFailures()).isNotNull().isEmpty();
  }

  public void testNotifiesOnStart() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    final TaskStatus[] status = {null};
    bus.register(new Object() {
      @Subscribe public void onEvent(TaskInfo info) {
        if (status[0] == null) {
          status[0] = info.getTaskStatus();
        }
        latch.countDown();
      }
    });
    create(0, bus, handler).run();
    latch.await(1, SECONDS);
    assertThat(status).containsExactly(PENDING);
  }

  public void testNotifiesOnEnd() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    final TaskStatus[] status = {null};
    bus.register(new Object() {
      @Subscribe public void onEvent(TaskInfo info) {
        if (info.getTaskStatus() == FINISHED) {
          status[0] = info.getTaskStatus();
          latch.countDown();
        }
      }
    });
    create(0, bus, handler).run();
    latch.await(1, SECONDS);
    assertThat(status).containsExactly(FINISHED);
  }

  protected Task create(int id, EventBus bus, Handler handler) {
    return new Task(id, bus, handler) {
      @Override protected void doTask() {}
    };
  }

}
