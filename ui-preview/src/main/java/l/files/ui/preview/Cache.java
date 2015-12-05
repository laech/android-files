package l.files.ui.preview;

import android.support.annotation.Nullable;

import java.io.IOException;

import l.files.fs.Path;
import l.files.fs.Stat;

abstract class Cache<V> {

    @Nullable
    abstract V get(Path path, Stat stat, Rect constraint, boolean matchTime)
            throws IOException;

    @Nullable
    abstract Snapshot<V> put(Path path, Stat stat, Rect constraint, V value)
            throws IOException;

}
