package l.files.fs;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;

public abstract class Stream<T> implements Iterable<T>, Closeable {

    /**
     * Places all elements of this stream into the given collection
     * and close this stream.
     */
    public <C extends Collection<? super T>> C to(C collection) throws IOException {
        try {
            for (T item : this) {
                collection.add(item);
            }
            return collection;
        } finally {
            close();
        }
    }

}
