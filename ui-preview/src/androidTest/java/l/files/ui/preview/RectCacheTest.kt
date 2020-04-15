package l.files.ui.preview

import l.files.ui.base.graphics.Rect

internal class RectCacheTest :
  PersistenceCacheTest<Rect, RectCache>() {

  override fun newCache() = RectCache { mockCacheDir() }

  override fun newValue(): Rect = Rect.of(
    random.nextInt(100) + 1,
    random.nextInt(100) + 1
  )
}
