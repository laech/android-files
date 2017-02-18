package l.files.fs.event;

import java.io.Closeable;

import javax.annotation.Nullable;

public interface Observation extends Closeable {

    boolean isClosed();

    @Nullable
    Throwable closeReason();
}
