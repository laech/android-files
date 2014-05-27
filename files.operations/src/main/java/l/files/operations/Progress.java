package l.files.operations;

import java.util.List;

import static l.files.io.file.operations.FileOperation.Failure;

/**
 * Contains information regarding the current state of a file processing task.
 */
public abstract class Progress {

  public static enum State {
    /**
     * The source task is pending to be executed.
     */
    PENDING,

    /**
     * The task has been started but it is now preparing, such as counting the
     * number of items to be processed.
     */
    PREPARING,

    /**
     * The task is now processing the items, such as deleting the items.
     */
    PROCESSING,

    /**
     * The task has finished.
     */
    FINISHED
  }

  /**
   * The ID of the source task, this ID is globally unique regardless of the
   * type of the task.
   */
  public abstract int taskId();

  /**
   * The time (in millis) the source task started executing.
   */
  public abstract long taskStartTime();

  /**
   * The current state of the source task.
   */
  public abstract State state();

  /**
   * The failures of the task execution, empty if none.
   */
  public abstract List<Failure> failures();
}
