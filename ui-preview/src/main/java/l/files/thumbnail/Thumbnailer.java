package l.files.thumbnail;

import java.io.IOException;

import javax.annotation.Nullable;

import l.files.fs.Path;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

public interface Thumbnailer {

    @Nullable
    ScaledBitmap create(Path path, Rect max) throws IOException;

}
