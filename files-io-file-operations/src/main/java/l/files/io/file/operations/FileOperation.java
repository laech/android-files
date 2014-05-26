package l.files.io.file.operations;

import com.google.auto.value.AutoValue;

import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * Operation on a or a set of files. Failures that occur during the operation
 * will not stop the process, instead the failures will be recorded and then a
 * {@link FileException} will be thrown at the end of processing.
 *
 * @see FileException
 * @see FileException#failures()
 */
public interface FileOperation<V> extends Callable<V> {

  /**
   * @throws InterruptedException if the thread was interrupted
   * @throws FileException if any file failed to be operated on
   */
  @Override V call() throws InterruptedException, FileException;

  @AutoValue
  public static abstract class Failure {
    Failure() {}

    /**
     * The path of the failed file.
     */
    public abstract String path();

    /**
     * The cause of the failure.
     */
    public abstract IOException cause();

    public static Failure create(String path, IOException exception) {
      return new AutoValue_FileOperation_Failure(path, exception);
    }
  }
}
