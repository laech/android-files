package l.files.preview;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.nio.ByteBuffer;

public final class RectCacheTest extends PersistenceCacheTest<Rect, RectCache> {

    public void test_invalid_input_ignored() throws Exception {
        assertNotNull(cache.read(byteInput(1, 1)));
        assertNull(cache.read(byteInput(0, 0)));
        assertNull(cache.read(byteInput(-1, -1)));
    }

    private DataInput byteInput(int width, int height) {
        byte[] bytes = ByteBuffer.allocate(8).putInt(width).putInt(height).array();
        return new DataInputStream(new ByteArrayInputStream(bytes));
    }

    @Override
    RectCache newCache() {
        return new RectCache(dir2());
    }

    @Override
    Rect newValue() {
        return Rect.of(
                random.nextInt(100) + 1,
                random.nextInt(100) + 1);
    }
}
