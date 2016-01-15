package l.files.ui.preview;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import l.files.fs.Path;

final class PaletteCache extends PersistenceCache<Integer> {

    PaletteCache(Path cacheDir) {
        super(cacheDir, 2);
    }

    @Override
    String cacheFileName() {
        return "palettes";
    }

    @Override
    Integer read(DataInput in) throws IOException {
        return in.readInt();
    }

    @Override
    void write(DataOutput out, Integer color) throws IOException {
        out.writeInt(color);
    }

}
