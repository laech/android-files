package l.files.operations;

import java.io.IOException;

/**
 * Operation on a or a set of files. Failures that occur during the operation
 * may or may not stop the process, depending on the implementation.
 */
interface FileOperation {

  /**
   * @throws InterruptedException if the thread was interrupted
   * @throws FileException        if any file failed to be operated on
   */
  void execute() throws FileException, InterruptedException, IOException;

}
