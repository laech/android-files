package l.files.operations;

public interface ProgressInfo extends TaskInfo {

  /**
   * The currently known total number of items to be processed.
   */
  int getTotalItemCount();

  /**
   * The total number of bytes.
   */
  long getTotalByteCount();

  /**
   * The number of items processed so far.
   */
  int getProcessedItemCount();

  /**
   * The total number of bytes processed.
   */
  long getProcessedByteCount();

  /**
   * Returns true if the task is in the progress of cleaning up.
   * Returns false if not, or this task does not need to perform any cleanup.
   */
  boolean isCleanup();

}
