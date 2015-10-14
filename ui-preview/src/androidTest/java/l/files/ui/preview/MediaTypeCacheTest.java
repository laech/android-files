package l.files.ui.preview;

public final class MediaTypeCacheTest
        extends PersistenceCacheTest<String, MediaTypeCache> {

    @Override
    MediaTypeCache newCache() {
        return new MediaTypeCache(dir2());
    }

    @Override
    String newValue() {
        return "application/xml";
    }
}
