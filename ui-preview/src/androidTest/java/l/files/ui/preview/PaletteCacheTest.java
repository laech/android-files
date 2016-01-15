package l.files.ui.preview;

import java.util.Random;

public final class PaletteCacheTest
        extends PersistenceCacheTest<Integer, PaletteCache> {

    static final Random random = new Random();

    @Override
    PaletteCache newCache() {
        return new PaletteCache(mockCacheDir());
    }

    @Override
    Integer newValue() {
        return random.nextInt();
    }

}
