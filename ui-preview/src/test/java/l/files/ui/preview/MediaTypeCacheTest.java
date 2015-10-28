package l.files.ui.preview;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = JELLY_BEAN)
public final class MediaTypeCacheTest
        extends PersistenceCacheTest<String, MediaTypeCache> {

    @Override
    MediaTypeCache newCache() {
        return new MediaTypeCache(mockCacheDir());
    }

    @Override
    String newValue() {
        return "application/xml";
    }
}
