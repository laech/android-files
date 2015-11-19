package l.files.ui.preview;

import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;

import l.files.fs.File;
import l.files.fs.Stat;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

abstract class MemCache<V> extends Cache<V> {

    private static final ThreadLocal<CharBuffer> keys = new ThreadLocal<CharBuffer>() {

        @Override
        protected CharBuffer initialValue() {
            return new CharBuffer();
        }


    };

    private long lastModifiedTime(Stat stat) {
        return stat.lastModifiedTime().to(MILLISECONDS);
    }

    @Override
    V get(File file, @Nullable Stat stat, Rect constraint) {
        CharBuffer key = key(file, stat, constraint);
        Snapshot<V> value = delegate().get(key);
        if (value == null) {
            return null;
        }
        if (stat != null && lastModifiedTime(stat) != value.time()) {
            return null;
        }
        return value.get();
    }

    private CharBuffer key(File res, @Nullable Stat stat, Rect constraint) {
        CharBuffer key = keys.get();
        key.clear();
        key(key, res, stat, constraint);
        return key;
    }

    @Override
    Snapshot<V> put(File file, Stat stat, Rect constraint, V value) {
        return delegate().put(
                key(file, stat, constraint).copy(),
                Snapshot.of(value, lastModifiedTime(stat)));
    }

    Snapshot<V> remove(File file, Stat stat, Rect constraint) {
        return delegate().remove(key(file, stat, constraint));
    }

    abstract void key(CharBuffer buffer, File res, Stat stat, Rect constraint);

    abstract LruCache<CharBuffer, Snapshot<V>> delegate();

}
