package l.files.operations;

import com.google.auto.value.AutoValue;

import java.io.IOException;

import l.files.fs.Path;

@AutoValue
public abstract class Failure {

    Failure() {
    }

    public abstract Path path();

    public abstract IOException cause();

    public static Failure create(Path path, IOException cause) {
        return new AutoValue_Failure(path, cause);
    }

}
