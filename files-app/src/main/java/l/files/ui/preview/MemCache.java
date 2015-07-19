package l.files.ui.preview;

import android.util.LruCache;

import l.files.common.graphics.Rect;
import l.files.fs.Resource;
import l.files.fs.Stat;
import l.files.logging.Logger;

abstract class MemCache<V> extends Cache<V> {

  final Logger log = Logger.get(getClass());

  @Override V get(Resource res, Stat stat, Rect constraint) {
    Snapshot<V> value = delegate().get(key(res, stat, constraint));
    if (value == null) {
      log.verbose("get - no entry for %s", res);
      return null;
    }
    if (!value.time().equals(stat.mtime())) {
      log.verbose("get - time not equal %s %s", value.time(), stat.atime());
      return null;
    }
    return value.get();
  }

  @Override Snapshot<V> put(Resource res, Stat stat, Rect constraint, V value) {
    log.verbose("put - %s", res);
    return delegate().put(key(res, stat, constraint), Snapshot.of(value, stat.mtime()));
  }

  Snapshot<V> remove(Resource res, Stat stat, Rect constraint) {
    log.verbose("remove - %s", res);
    return delegate().remove(key(res, stat, constraint));
  }

  abstract String key(Resource res, Stat stat, Rect constraint);

  abstract LruCache<String, Snapshot<V>> delegate();

}
