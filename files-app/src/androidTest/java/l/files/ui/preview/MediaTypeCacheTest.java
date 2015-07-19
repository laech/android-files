package l.files.ui.preview;

import com.google.common.net.MediaType;

import static com.google.common.net.MediaType.APPLICATION_BINARY;

public final class MediaTypeCacheTest
    extends PersistenceCacheTest<MediaType, MediaTypeCache> {

  @Override MediaTypeCache newCache() {
    return new MediaTypeCache();
  }

  @Override MediaType newValue() {
    return APPLICATION_BINARY;
  }
}
