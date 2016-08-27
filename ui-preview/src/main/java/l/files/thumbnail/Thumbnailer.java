package l.files.thumbnail;

import javax.annotation.Nullable;

import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

public interface Thumbnailer<T> {

    @Nullable
    ScaledBitmap create(T input, Rect max) throws Exception;

}
