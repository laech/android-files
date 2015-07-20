package l.files.ui.preview;

import android.util.LruCache;

import l.files.common.graphics.Rect;
import l.files.fs.Resource;
import l.files.fs.Stat;

abstract class MemCache<V> extends Cache<V> {

  @Override V get(Resource res, Stat stat, Rect constraint) {
    Snapshot<V> value = delegate().get(key(res, stat, constraint));
    if (value == null) {
      return null;
    }
    if (!value.time().equals(stat.mtime())) {
      return null;
    }
    return value.get();
  }

  @Override Snapshot<V> put(Resource res, Stat stat, Rect constraint, V value) {
    return delegate().put(key(res, stat, constraint), Snapshot.of(value, stat.mtime()));
  }

  Snapshot<V> remove(Resource res, Stat stat, Rect constraint) {
    return delegate().remove(key(res, stat, constraint));
  }

  abstract String key(Resource res, Stat stat, Rect constraint);

  abstract LruCache<String, Snapshot<V>> delegate();

}
