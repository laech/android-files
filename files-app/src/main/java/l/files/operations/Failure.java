package l.files.operations;

import java.io.IOException;

import auto.parcel.AutoParcel;
import l.files.fs.Resource;

@AutoParcel
public abstract class Failure {

  Failure() {}

  public abstract Resource resource();

  public abstract IOException cause();

  public static Failure create(Resource resource, IOException cause) {
    return new AutoParcel_Failure(resource, cause);
  }

}
