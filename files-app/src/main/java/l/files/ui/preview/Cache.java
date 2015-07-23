package l.files.ui.preview;

import android.support.annotation.Nullable;

import java.io.IOException;

import l.files.common.graphics.Rect;
import l.files.fs.Resource;
import l.files.fs.Stat;

abstract class Cache<V> {

  @Nullable
  abstract V get(Resource res, Stat stat, Rect constraint)
      throws IOException;

  @Nullable
  abstract Snapshot<V> put(Resource res, Stat stat, Rect constraint, V value)
      throws IOException;

}
