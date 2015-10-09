package l.files.ui.preview;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import l.files.common.graphics.Rect;
import l.files.fs.File;

final class RectCache extends PersistenceCache<Rect> {

    RectCache(File cacheDir) {
        super(cacheDir);
    }

    @Override
    String cacheFileName() {
        return "sizes";
    }

    @Override
    Rect read(DataInput in) throws IOException {
        int width = in.readInt();
        int height = in.readInt();
        try {
            return Rect.of(width, height);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    void write(DataOutput out, Rect rect) throws IOException {
        out.writeInt(rect.width());
        out.writeInt(rect.height());
    }

}
