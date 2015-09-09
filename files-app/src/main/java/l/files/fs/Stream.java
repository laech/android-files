package l.files.fs;

import java.io.Closeable;
import java.util.Collection;

public abstract class Stream<T> implements Iterable<T>, Closeable {

    public <C extends Collection<? super T>> C to(C collection) {
        for (T item : this) {
            collection.add(item);
        }
        return collection;
    }

}
