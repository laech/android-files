package l.files.ui.preview

internal class MediaTypeCacheTest :
  PersistenceCacheTest<String, MediaTypeCache>() {

  override fun newCache() = MediaTypeCache { mockCacheDir() }
  override fun newValue() = "application/xml"
}
