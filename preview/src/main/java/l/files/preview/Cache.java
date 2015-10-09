package l.files.preview;

import android.support.annotation.Nullable;

import java.io.IOException;

import l.files.fs.File;
import l.files.fs.Stat;

abstract class Cache<V> {

    @Nullable
    abstract V get(File res, Stat stat, Rect constraint)
            throws IOException;

    @Nullable
    abstract Snapshot<V> put(File res, Stat stat, Rect constraint, V value)
            throws IOException;

}
