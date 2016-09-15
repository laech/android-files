package l.files.ui.preview;

import android.support.v4.util.LruCache;

import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.ui.base.graphics.Rect;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

abstract class MemCache<K, V> extends Cache<V> {

    private long lastModifiedTime(Stat stat) {
        return stat.lastModifiedTime().to(MILLISECONDS);
    }

    @Override
    V get(Path path, Stat stat, Rect constraint, boolean matchTime) {
        K key = getKey(path, stat, constraint);
        Snapshot<V> value = delegate().get(key);
        if (value == null) {
            return null;
        }
        if (matchTime && lastModifiedTime(stat) != value.time()) {
            return null;
        }
        return value.get();
    }

    abstract K getKey(Path path, Stat stat, Rect constraint);

    @Override
    Snapshot<V> put(Path path, Stat stat, Rect constraint, V value) {
        return delegate().put(
                getKey(path, stat, constraint),
                Snapshot.of(value, lastModifiedTime(stat)));
    }

    Snapshot<V> remove(Path path, Stat stat, Rect constraint) {
        return delegate().remove(getKey(path, stat, constraint));
    }

    abstract LruCache<K, Snapshot<V>> delegate();

}
