package l.files.ui.preview

import java.lang.System.currentTimeMillis

internal class NoPreviewCacheTest :
  PersistenceCacheTest<Boolean, NoPreviewCache>() {

  override fun newCache() = NoPreviewCache { mockCacheDir() }
  override fun newValue() = currentTimeMillis() % 2 == 0L
}
