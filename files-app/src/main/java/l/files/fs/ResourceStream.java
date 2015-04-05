package l.files.fs;

import java.io.Closeable;

public interface ResourceStream<T extends PathEntry> extends Iterable<T>, Closeable {

}
