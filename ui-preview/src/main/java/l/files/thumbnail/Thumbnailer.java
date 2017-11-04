package l.files.thumbnail;

import android.content.Context;

import android.support.annotation.Nullable;

import l.files.fs.Path;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

public interface Thumbnailer<T> {

    boolean accepts(Path path, String mediaType);

    @Nullable
    ScaledBitmap create(T input, Rect max, Context context) throws Exception;

}
