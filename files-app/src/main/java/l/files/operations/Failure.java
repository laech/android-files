package l.files.operations;

import com.google.auto.value.AutoValue;

import java.io.IOException;

import l.files.fs.Resource;

@AutoValue
public abstract class Failure {

  Failure() {}

  public abstract Resource resource();

  public abstract IOException cause();

  public static Failure create(Resource resource, IOException cause) {
    return new AutoValue_Failure(resource, cause);
  }

}
