package l.files.fs.event;

import java.io.Closeable;

import androidx.annotation.Nullable;

public interface Observation extends Closeable {

    boolean isClosed();

    @Nullable
    Throwable closeReason();
}
