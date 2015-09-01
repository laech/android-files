package l.files.operations;

import com.google.auto.value.AutoValue;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * Represents the state of a task. Instances of this will be posted to the event
 * bus during task execution.
 */
public abstract class TaskState {

  TaskState() {
  }

  /**
   * The source task this state is for.
   */
  public abstract TaskId getTask();

  /**
   * Gets the source/destination of the source task.
   */
  public abstract Target getTarget();

  /**
   * Gets the time when the state transitioned to this one. This time will
   * stay the same for subsequent state updates of the same kind. i.e. this
   * value will only change if the next state is of a different kind.
   */
  public abstract Time getTime();

  /**
   * Returns true if the task is finished (success or failure).
   */
  public boolean isFinished() {
    return this instanceof Success || this instanceof Failed;
  }

  @AutoValue
  public static abstract class Pending extends TaskState {

    Pending() {
    }

    public Running running(Time time) {
      return running(time, Progress.NONE, Progress.NONE);
    }

    public Running running(Time time, Progress items, Progress bytes) {
      return new AutoValue_TaskState_Running(
          getTask(), getTarget(), time, items, bytes);
    }

  }

  @AutoValue
  public static abstract class Running extends TaskState {

    Running() {
    }

    /**
     * Number of items to process.
     */
    public abstract Progress getItems();

    /**
     * Number of bytes to process.
     */
    public abstract Progress getBytes();

    public Running running(Progress items, Progress bytes) {
      // Do not update the time as specified by the contract on time()
      return new AutoValue_TaskState_Running(
          getTask(), getTarget(), getTime(), items, bytes);
    }

    public Success success(Time time) {
      return new AutoValue_TaskState_Success(
          getTask(), getTarget(), time);
    }

    public Failed failed(Time time, List<Failure> failures) {
      return new AutoValue_TaskState_Failed(getTask(), getTarget(), time,
          unmodifiableList(new ArrayList<>(failures)));
    }
  }

  @AutoValue
  public static abstract class Success extends TaskState {
    Success() {
    }
  }

  @AutoValue
  public static abstract class Failed extends TaskState {

    Failed() {
    }

    /**
     * The file failures of the task, may be empty if the task if caused by
     * other errors.
     */
    public abstract List<Failure> getFailures();

  }

  public static Pending pending(TaskId task, Target target, Time time) {
    return new AutoValue_TaskState_Pending(task, target, time);
  }

}
