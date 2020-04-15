package l.files.ui.preview;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import l.files.fs.Path;
import l.files.ui.base.graphics.Rect;

final class RectCache extends PersistenceCache<Rect> {

    RectCache(Path cacheDir) {
        super(cacheDir, 1);
    }

    @Override
    String cacheFileName() {
        return "sizes";
    }

    @Override
    Rect read(DataInput in) throws IOException {
        int width = in.readInt();
        int height = in.readInt();
        return Rect.of(width, height);
    }

    @Override
    void write(DataOutput out, Rect rect) throws IOException {
        out.writeInt(rect.width());
        out.writeInt(rect.height());
    }

}
