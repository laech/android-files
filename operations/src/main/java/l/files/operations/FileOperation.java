package l.files.operations;

/**
 * Operation on a or a set of files. Failures that occur during the operation
 * will not stop the process, instead the failures will be recorded and then a
 * {@link FileException} will be thrown at the end of processing.
 *
 * @see FileException
 * @see FileException#failures()
 */
interface FileOperation {

  /**
   * @throws InterruptedException if the thread was interrupted
   * @throws FileException        if any file failed to be operated on
   */
  void execute() throws FileException, InterruptedException;

  /**
   * Returns true if this task is finished (due to normal or abnormal
   * termination).
   */
  boolean isDone();

}
