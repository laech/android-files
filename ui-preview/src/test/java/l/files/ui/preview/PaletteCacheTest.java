package l.files.ui.preview;

import android.graphics.Color;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Palette.Swatch;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = JELLY_BEAN)
public final class PaletteCacheTest
        extends PersistenceCacheTest<Palette, PaletteCache> {

    @Override
    PaletteCache newCache() {
        return new PaletteCache(mockCacheDir());
    }

    @Override
    Palette newValue() {
        return Palette.from(singletonList(new Swatch(Color.BLUE, 1)));
    }

    @Override
    void assertValueEquals(Palette a, Palette b) {
        assertEquals(a.getSwatches(), b.getSwatches());
    }
}
