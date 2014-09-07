package l.files.operations;

import com.google.auto.value.AutoValue;

import java.io.IOException;

@AutoValue
public abstract class Failure {
  public Failure() {}

  /**
   * The path of the failed file.
   */
  public abstract String path();

  /**
   * The cause of the failure.
   */
  public abstract IOException cause();

  public static Failure create(String path, IOException exception) {
    return new AutoValue_Failure(path, exception);
  }
}
