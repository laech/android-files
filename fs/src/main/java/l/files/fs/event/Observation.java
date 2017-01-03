package l.files.fs.event;

import java.io.Closeable;

public interface Observation extends Closeable {

    boolean isClosed();

}
