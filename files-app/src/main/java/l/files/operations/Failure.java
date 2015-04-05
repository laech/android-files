package l.files.operations;

import java.io.IOException;

import auto.parcel.AutoParcel;
import l.files.fs.Path;

@AutoParcel
public abstract class Failure {

    Failure() {
    }

    public abstract Path getPath();

    public abstract IOException getCause();

    public static Failure create(Path path, IOException cause) {
        return new AutoParcel_Failure(path, cause);
    }

}
