package l.files.operations;

import java.io.IOException;

import auto.parcel.AutoParcel;
import l.files.fs.Resource;

@AutoParcel
public abstract class Failure {

    Failure() {
    }

    public abstract Resource getResource();

    public abstract IOException getCause();

    public static Failure create(Resource resource, IOException cause) {
        return new AutoParcel_Failure(resource, cause);
    }

}
