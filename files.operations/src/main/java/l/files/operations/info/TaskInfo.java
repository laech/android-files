package l.files.operations.info;

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
     * on start of this task.
     */
    long getTaskElapsedStartTime();

    TaskStatus getTaskStatus();

    enum TaskStatus {
        PENDING,
        RUNNING,
        FINISHED,
    }
}
