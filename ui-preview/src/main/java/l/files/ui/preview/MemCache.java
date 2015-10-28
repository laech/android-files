package l.files.ui.preview;

import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;

import l.files.fs.File;
import l.files.fs.Stat;

abstract class MemCache<V> extends Cache<V> {

    @Override
    V get(File res, @Nullable Stat stat, Rect constraint) {
        Snapshot<V> value = delegate().get(key(res, stat, constraint));
        if (value == null) {
            return null;
        }
        if (stat != null && !stat.lastModifiedTime().equals(value.time())) {
            return null;
        }
        return value.get();
    }

    @Override
    Snapshot<V> put(File res, Stat stat, Rect constraint, V value) {
        return delegate().put(key(res, stat, constraint), Snapshot.of(value, stat.lastModifiedTime()));
    }

    Snapshot<V> remove(File res, Stat stat, Rect constraint) {
        return delegate().remove(key(res, stat, constraint));
    }

    abstract String key(File res, Stat stat, Rect constraint);

    abstract LruCache<String, Snapshot<V>> delegate();

}
