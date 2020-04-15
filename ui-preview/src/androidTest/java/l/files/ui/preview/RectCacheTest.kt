package l.files.ui.preview;

import l.files.ui.base.graphics.Rect;

public final class RectCacheTest extends PersistenceCacheTest<Rect, RectCache> {

    @Override
    RectCache newCache() {
        return new RectCache(mockCacheDir());
    }

    @Override
    Rect newValue() {
        return Rect.of(
                random.nextInt(100) + 1,
                random.nextInt(100) + 1);
    }
}
