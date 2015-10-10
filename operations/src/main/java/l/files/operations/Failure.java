package l.files.operations;

import com.google.auto.value.AutoValue;

import java.io.IOException;

import l.files.fs.File;

@AutoValue
public abstract class Failure {

    Failure() {
    }

    public abstract File file();

    public abstract IOException cause();

    public static Failure create(File file, IOException cause) {
        return new AutoValue_Failure(file, cause);
    }

}
