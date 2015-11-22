package l.files.ui.preview;

import android.support.annotation.Nullable;

import java.io.IOException;

import l.files.fs.File;
import l.files.fs.Stat;

abstract class Cache<V> {

    @Nullable
    abstract V get(File file, Stat stat, Rect constraint, boolean matchTime)
            throws IOException;

    @Nullable
    abstract Snapshot<V> put(File file, Stat stat, Rect constraint, V value)
            throws IOException;

}
