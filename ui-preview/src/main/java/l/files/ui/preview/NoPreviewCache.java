package l.files.ui.preview;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import l.files.fs.Path;

final class NoPreviewCache extends PersistenceCache<Boolean> {

    NoPreviewCache(Path cacheDir) {
        super(cacheDir, (byte) 1);
    }

    @Override
    String cacheFileName() {
        return "non-images";
    }

    @Override
    Boolean read(DataInput in) throws IOException {
        return in.readBoolean();
    }

    @Override
    void write(DataOutput out, Boolean value) throws IOException {
        out.writeBoolean(value);
    }

}
