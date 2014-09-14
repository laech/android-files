package l.files.operations.testing;

import com.google.common.truth.FailureStrategy;
import com.google.common.truth.Subject;
import com.google.common.truth.SubjectFactory;

import java.util.List;

import l.files.operations.Failure;
import l.files.operations.Progress;
import l.files.operations.Target;
import l.files.operations.TaskId;
import l.files.operations.TaskState;
import l.files.operations.Time;

import static com.google.common.base.Objects.equal;
import static com.google.common.truth.Truth.ASSERT;

public final class TaskStateSubject
    extends Subject<TaskStateSubject, TaskState> {

  public TaskStateSubject(FailureStrategy failureStrategy, TaskState subject) {
    super(failureStrategy, subject);
  }

  public static SubjectFactory<TaskStateSubject, TaskState> taskState() {
    return new SubjectFactory<TaskStateSubject, TaskState>() {
      @Override
      public TaskStateSubject getSubject(FailureStrategy fs, TaskState that) {
        return new TaskStateSubject(fs, that);
      }
    };
  }

  public static TaskStateSubject assertThat(TaskState state) {
    return ASSERT.about(taskState()).that(state);
  }

  public TaskStateSubject isPending() {
    isA(TaskState.Pending.class);
    return this;
  }

  public TaskStateSubject isRunning() {
    isA(TaskState.Running.class);
    return this;
  }

  public TaskStateSubject isSuccess() {
    isA(TaskState.Success.class);
    return this;
  }

  public TaskStateSubject isFailure() {
    isA(TaskState.Failed.class);
    return this;
  }

  public TaskStateSubject task(TaskId task) {
    if (!equal(getSubject().task(), task)) {
      fail("has task", task);
    }
    return this;
  }

  public TaskStateSubject target(Target target) {
    if (!equal(getSubject().target(), target)) {
      fail("has target", target);
    }
    return this;
  }

  public TaskStateSubject time(Time time) {
    if (!equal(getSubject().time(), time)) {
      fail("has time", time);
    }
    return this;
  }

  public TaskStateSubject items(Progress items) {
    isRunning();
    if (!equal(((TaskState.Running) getSubject()).items(), items)) {
      fail("has items", items);
    }
    return this;
  }

  public TaskStateSubject bytes(Progress bytes) {
    isRunning();
    if (!equal(((TaskState.Running) getSubject()).bytes(), bytes)) {
      fail("has bytes", bytes);
    }
    return this;
  }

  public TaskStateSubject failures(List<Failure> failures) {
    isFailure();
    if (!equal(((TaskState.Failed) getSubject()).failures(), failures)) {
      fail("has failures", failures);
    }
    return this;
  }
}
