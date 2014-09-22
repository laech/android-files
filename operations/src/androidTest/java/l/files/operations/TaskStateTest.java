package l.files.operations;

import java.io.IOException;
import java.util.List;

import l.files.common.testing.BaseTest;

import static java.util.Arrays.asList;
import static l.files.operations.TaskKind.COPY;

public final class TaskStateTest extends BaseTest {

  private Time time;
  private TaskId task;
  private Target target;
  private TaskState.Pending pending;

  @Override protected void setUp() throws Exception {
    super.setUp();
    this.time = Time.create(10, 11);
    this.task = TaskId.create(1, COPY);
    this.target = Target.create("src", "dst");
    this.pending = TaskState.pending(task, target, time);
  }

  public void testCreatePending() throws Exception {
    assertEquals(task, pending.task());
    assertEquals(time, pending.time());
    assertEquals(target, pending.target());
  }

  public void testPendingToRunning() throws Exception {
    Progress items = Progress.create(10101, 1);
    Progress bytes = Progress.create(10100, 0);
    Time time = Time.create(1010, 11);
    TaskState.Running state = pending.running(time, items, bytes);
    assertEquals(task, state.task());
    assertEquals(time, state.time());
    assertEquals(items, state.items());
    assertEquals(bytes, state.bytes());
    assertEquals(target, state.target());
  }

  public void testPendingToRunningWithoutProgress() throws Exception {
    Time time = Time.create(10101, 100);
    TaskState.Running state = pending.running(time);
    assertEquals(task, state.task());
    assertEquals(time, state.time());
    assertEquals(target, state.target());
    assertEquals(Progress.none(), state.items());
    assertEquals(Progress.none(), state.bytes());

  }

  public void testRunningToRunningDoesNotChangeTime() throws Exception {
    Time time = Time.create(102, 2);
    Progress items = Progress.create(4, 2);
    Progress bytes = Progress.create(9, 2);
    TaskState.Running state = pending.running(time).running(items, bytes);
    assertEquals(task, state.task());
    assertEquals(time, state.time());
    assertEquals(items, state.items());
    assertEquals(bytes, state.bytes());
    assertEquals(target, state.target());
  }

  public void testRunningToSuccess() throws Exception {
    Time successTime = Time.create(2, 2);
    TaskState state = pending.running(time).success(successTime);
    assertEquals(task, state.task());
    assertEquals(successTime, state.time());
    assertEquals(target, state.target());
  }

  public void testRunningToFailed() throws Exception {
    Time failureTime = Time.create(20, 2);
    List<Failure> failures = asList(Failure.create("1", new IOException("ok")));
    TaskState.Failed state = pending.running(time).failed(failureTime, failures);
    assertEquals(task, state.task());
    assertEquals(failureTime, state.time());
    assertEquals(target, state.target());
    assertEquals(failures, state.failures());
  }

}
