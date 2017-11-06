package l.files.ui.preview;

import android.support.annotation.Nullable;

import java.io.IOException;

import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.ui.base.graphics.Rect;

interface Cache<V> {

    @Nullable
    V get(Path path, Stat stat, Rect constraint, boolean matchTime)
            throws IOException;

    @Nullable
    Snapshot<V> put(Path path, Stat stat, Rect constraint, V value)
            throws IOException;

}
