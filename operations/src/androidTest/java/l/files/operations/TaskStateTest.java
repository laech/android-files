package l.files.operations;

import java.io.IOException;
import java.util.List;

import l.files.common.testing.BaseTest;

import static java.util.Arrays.asList;
import static l.files.operations.TaskKind.COPY;
import static l.files.operations.testing.TaskStateSubject.assertThat;

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
    assertThat(pending).task(task).time(time).target(target);
  }

  public void testPendingToRunning() throws Exception {
    Progress items = Progress.create(10101, 1);
    Progress bytes = Progress.create(10100, 0);
    Time time = Time.create(1010, 11);
    TaskState state = pending.running(time, items, bytes);
    assertThat(state)
        .task(task)
        .time(time)
        .items(items)
        .bytes(bytes)
        .target(target);
  }

  public void testPendingToRunningWithoutProgress() throws Exception {
    Time time = Time.create(10101, 100);
    TaskState state = pending.running(time);
    assertThat(state)
        .task(task)
        .time(time)
        .items(Progress.none())
        .bytes(Progress.none())
        .target(target);
  }

  public void testRunningToRunningDoesNotChangeTime() throws Exception {
    Time time = Time.create(102, 2);
    Progress items = Progress.create(4, 2);
    Progress bytes = Progress.create(9, 2);
    TaskState state = pending.running(time).running(items, bytes);
    assertThat(state)
        .task(task)
        .time(time)
        .items(items)
        .bytes(bytes)
        .target(target);
  }

  public void testRunningToSuccess() throws Exception {
    Time successTime = Time.create(2, 2);
    TaskState state = pending.running(time).success(successTime);
    assertThat(state).task(task).target(target).time(successTime);
  }

  public void testRunningToFailed() throws Exception {
    Time failureTime = Time.create(20, 2);
    List<Failure> failures = asList(Failure.create("1", new IOException("ok")));
    TaskState state = pending.running(time).failed(failureTime, failures);
    assertThat(state)
        .task(task)
        .target(target)
        .time(failureTime)
        .failures(failures);
  }

}
