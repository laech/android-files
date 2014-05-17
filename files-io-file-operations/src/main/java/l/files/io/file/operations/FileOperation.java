package l.files.io.file.operations;

import com.google.auto.value.AutoValue;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

public interface FileOperation extends Callable<List<FileOperation.Failure>> {

  /**
   * Executes this operation and returns the failures.
   */
  @Override List<Failure> call() throws CancellationException;

  @AutoValue
  public static abstract class Failure {
    Failure() {}

    public abstract String path();
    public abstract IOException exception();

    public static Failure create(String path, IOException exception) {
      return new AutoValue_FileOperation_Failure(path, exception);
    }
  }
}
