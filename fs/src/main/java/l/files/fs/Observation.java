package l.files.fs;

import java.io.Closeable;

public interface Observation extends Closeable {

    boolean isClosed();

}
