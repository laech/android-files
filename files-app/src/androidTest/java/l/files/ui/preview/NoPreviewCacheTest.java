package l.files.ui.preview;

import static java.lang.System.currentTimeMillis;

public final class NoPreviewCacheTest
    extends PersistenceCacheTest<Boolean, NoPreviewCache> {

  @Override NoPreviewCache newCache() {
    return new NoPreviewCache(getTestContext());
  }

  @Override Boolean newValue() {
    return currentTimeMillis() % 2 == 0;
  }
}
