package l.files.fs.local;

import android.system.OsConstants;
import android.util.Log;

import com.google.common.collect.AbstractIterator;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

import l.files.fs.NotDirectoryException;
import l.files.fs.UncheckedIOException;

final class LocalResourceStream extends Native
        implements Iterable<LocalPathEntry>, Closeable {

    // TODO use callback instead of iterable

    /*
     * Design note: this basically uses <dirent.h> to read directory entries,
     * returning simple DirectoryStream.Entry without using stat/lstat will yield
     * much better performance when directory is large.
     */

    private final LocalResource parent;
    private final long dir;

    private boolean iterated;

    LocalResourceStream(LocalResource parent, long dir) {
        this.parent = parent;
        this.dir = dir;
    }

    @Override
    public Iterator<LocalPathEntry> iterator() {
        if (iterated) {
            throw new IllegalStateException("iterator() has already been called");
        }
        iterated = true;

        return new AbstractIterator<LocalPathEntry>() {
            @Override
            protected LocalPathEntry computeNext() {
                Log.e("ABC", dir + "");
                Dirent entry = readNext();
                if (entry == null) {
                    return endOfData();
                }
                return toEntry(entry);
            }
        };
    }

    private Dirent readNext() {
        try {
            Dirent next = Dirent.readdir(dir);
            while (next != null && isSelfOrParent(next)) {
                next = Dirent.readdir(dir);
            }
            return next;
        } catch (ErrnoException e) {
            throw new UncheckedIOException(e.toIOException(parent.getPath()));
        }
    }

    private boolean isSelfOrParent(Dirent entry) {
        return entry.getName().equals(".") || entry.getName().equals("..");
    }

    private LocalPathEntry toEntry(Dirent entry) {
        return LocalPathEntry.create(
                parent.resolve(entry.getName()),
                entry.getInode(),
                entry.getType() == Dirent.DT_DIR);
    }

    @Override
    public void close() throws IOException {
        try {
            Dirent.closedir(dir);
        } catch (ErrnoException e) {
            throw e.toIOException(parent.getPath());
        }
    }

    public static LocalResourceStream open(LocalResource resource) throws IOException {
        try {
            return new LocalResourceStream(resource, open(resource.getPath()));
        } catch (ErrnoException e) {
            if (e.isCausedByNoFollowLink(resource)) {
                throw new NotDirectoryException(resource.getPath(), e);
            }
            throw e.toIOException(resource.getPath());
        }
    }

    /**
     * Open only if path is directory, will not follow symlink.
     */
    private static native long open(String path) throws ErrnoException;

}
