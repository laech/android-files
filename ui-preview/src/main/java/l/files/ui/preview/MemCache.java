package l.files.ui.preview;

import android.support.v4.util.LruCache;

import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.ui.base.graphics.Rect;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

abstract class MemCache<V> extends Cache<V> {

    private static final ThreadLocal<ByteBuffer> keys = new ThreadLocal<ByteBuffer>() {

        @Override
        protected ByteBuffer initialValue() {
            return new ByteBuffer(64);
        }

    };

    private long lastModifiedTime(Stat stat) {
        return stat.lastModifiedTime().to(MILLISECONDS);
    }

    @Override
    V get(Path path, Stat stat, Rect constraint, boolean matchTime) {
        ByteBuffer key = key(path, stat, constraint);
        Snapshot<V> value = delegate().get(key);
        if (value == null) {
            return null;
        }
        if (matchTime && lastModifiedTime(stat) != value.time()) {
            return null;
        }
        return value.get();
    }

    private ByteBuffer key(Path path, Stat stat, Rect constraint) {
        ByteBuffer key = keys.get();
        key.clear();
        key(key, path, stat, constraint);
        return key;
    }

    @Override
    Snapshot<V> put(Path path, Stat stat, Rect constraint, V value) {
        return delegate().put(
                key(path, stat, constraint).copy(),
                Snapshot.of(value, lastModifiedTime(stat)));
    }

    Snapshot<V> remove(Path path, Stat stat, Rect constraint) {
        return delegate().remove(key(path, stat, constraint));
    }

    abstract void key(ByteBuffer buffer, Path path, Stat stat, Rect constraint);

    abstract LruCache<ByteBuffer, Snapshot<V>> delegate();

}
