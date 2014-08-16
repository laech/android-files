package l.files.operations.info;

import java.util.Collection;

import static l.files.io.file.operations.FileOperation.Failure;

/**
 * Information of a running task.
 * And instance of this event will be posted to the event bus periodically
 * while the task is running.
 *
 * @see l.files.operations.Events#get()
 */
public interface TaskInfo {

  /**
   * Gets the ID of the task.
   * This ID is globally unique regard less of the task type.
   */
  int getTaskId();

  /**
   * Gets the start time of this task in milliseconds.
   */
  long getTaskStartTime();

  /**
   * Gets the value of {@link android.os.SystemClock#elapsedRealtime()}
   * when this task starts {@link TaskStatus#RUNNING}.
   */
  long getElapsedRealtimeOnRun();

  TaskStatus getTaskStatus();

  Collection<Failure> getFailures();

  enum TaskStatus {
    PENDING,
    RUNNING,
    FINISHED,
  }
}
