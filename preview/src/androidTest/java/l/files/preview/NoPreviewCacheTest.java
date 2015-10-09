package l.files.preview;

import static java.lang.System.currentTimeMillis;

public final class NoPreviewCacheTest
        extends PersistenceCacheTest<Boolean, NoPreviewCache> {

    @Override
    NoPreviewCache newCache() {
        return new NoPreviewCache(dir2());
    }

    @Override
    Boolean newValue() {
        return currentTimeMillis() % 2 == 0;
    }
}
