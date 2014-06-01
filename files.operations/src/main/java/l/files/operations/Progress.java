package l.files.operations;

import android.content.Intent;
import android.os.SystemClock;

import com.google.common.collect.ImmutableList;

import java.util.List;

import static java.util.Collections.emptyList;
import static l.files.io.file.operations.FileOperation.Failure;

/**
 * Helper to retrieve common information from a file operation intent.
 */
public class Progress {

  private static final String TASK_ID = "task_id";
  private static final String TASK_START_TIME = "task_start_time";
  private static final String ELAPSED_TIME_ON_START = "task_elapsed_start";
  private static final String TASK_STATUS = "task_status";
  private static final String FAILURES = "failures";
  private static final String TOTAL_ITEM_COUNT = "total_item_count";
  private static final String DELETED_ITEM_COUNT = "deleted_item_count";
  private static final String ROOT_PATH = "root_path";

  /**
   * The source task is pending to be executed.
   */
  public static final int STATUS_PENDING = 1;
  /**
   * The task has been started but it is now preparing, such as counting the
   * number of items to be processed.
   */
  public static final int STATUS_PREPARING = 2;
  /**
   * The task is now processing the items, such as deleting the items.
   */
  public static final int STATUS_PROCESSING = 3;

  /**
   * The task has finished.
   */
  public static final int STATUS_FINISHED = 4;

  private Progress() {}

  /**
   * Gets the ID of the source task, this ID is globally unique regardless of
   * the type of the task.
   */
  public static int getTaskId(Intent intent) {
    return intent.getIntExtra(TASK_ID, -1);
  }

  /**
   * The time (in millis) the source task started executing.
   */
  public static long getTaskStartTime(Intent intent) {
    return intent.getLongExtra(TASK_START_TIME, -1);
  }

  /**
   * The value of {@link SystemClock#elapsedRealtime()} on start of the task.
   */
  public static long getElapsedTimeOnStart(Intent intent) {
    return intent.getLongExtra(ELAPSED_TIME_ON_START, -1);
  }

  /**
   * The current state of the source task.
   */
  public static int getTaskStatus(Intent intent) {
    return intent.getIntExtra(TASK_STATUS, -1);
  }

  /**
   * Gets the common root path of all the paths the task is executing on.
   */
  public static String getRootPath(Intent intent) {
    return intent.getStringExtra(ROOT_PATH);
  }

  /**
   * Gets the failures of the task execution, empty if none.
   * <p/>
   * TODO use proper type
   */
  public static List<String> getFailures(Intent intent) {
    List<String> failures = intent.getStringArrayListExtra(FAILURES);
    if (failures == null) {
      return emptyList();
    }
    return ImmutableList.copyOf(failures);
  }

  /**
   * Retrieves additional information from a file delete intent.
   */
  public static final class Delete extends Progress {
    public static final String ACTION =
        "l.files.operations.intent.action.DELETE_PROGRESS";

    private Delete() {}

    /**
     * Gets the total number of items to be processed. This value may increase
     * while the task is in {@link #STATUS_PREPARING}.
     */
    public static int getTotalItemCount(Intent intent) {
      return intent.getIntExtra(TOTAL_ITEM_COUNT, -1);
    }

    /**
     * Gets the total number of items deleted so far.
     */
    public static int getDeletedItemCount(Intent intent) {
      return intent.getIntExtra(DELETED_ITEM_COUNT, -1);
    }

    public static Intent create(
        int taskId, long taskStartTime, long elapsedTimeOnStart, int status, List<Failure> failures,
        String rootPath, int totalItemCount, int deletedItemCount) {
      return new Intent(ACTION)
          .putExtra(TASK_ID, taskId)
          .putExtra(TASK_START_TIME, taskStartTime)
          .putExtra(TASK_STATUS, status)
          .putExtra(ELAPSED_TIME_ON_START, elapsedTimeOnStart)
          .putExtra(ROOT_PATH, rootPath)
          .putExtra(TOTAL_ITEM_COUNT, totalItemCount)
          .putExtra(DELETED_ITEM_COUNT, deletedItemCount);
      // TODO
//          .putStringArrayListExtra(FAILURES, newArrayList(failures));
    }
  }
}
