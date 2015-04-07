package l.files.fs.local;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import l.files.fs.Resource;
import l.files.fs.UncheckedIOException;

final class LocalResourceStream implements Resource.Stream<LocalPathEntry> {

    /*
     * Design note: this basically uses <dirent.h> to read directory entries,
     * returning simple DirectoryStream.Entry without using stat/lstat will yield
     * much better performance when directory is large.
     */

    private final LocalPath parent;
    private final long dir;

    private boolean iterated;

    LocalResourceStream(LocalPath parent, long dir) {
        this.parent = parent;
        this.dir = dir;
    }

    @Override
    public Iterator<LocalPathEntry> iterator() {
        if (iterated) {
            throw new IllegalStateException("iterator() has already been called");
        }
        iterated = true;

        return new Iterator<LocalPathEntry>() {

            Dirent next;

            @Override
            public boolean hasNext() {
                if (next == null) {
                    readNext();
                }
                return next != null;
            }

            @Override
            public LocalPathEntry next() {
                if (next == null) {
                    readNext();
                }
                if (next == null) {
                    throw new NoSuchElementException();
                }
                LocalPathEntry entry = toEntry(next);
                next = null;
                return entry;
            }

            private void readNext() {
                try {
                    next = Dirent.readdir(dir);
                    while (next != null && isSelfOrParent(next)) {
                        next = Dirent.readdir(dir);
                    }
                } catch (ErrnoException e) {
                    throw new UncheckedIOException(e.toIOException());
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
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
            throw e.toIOException();
        }
    }

    public static LocalResourceStream open(LocalPath path) throws IOException {
        try {
            return new LocalResourceStream(path, Dirent.opendir(path.toString()));
        } catch (ErrnoException e) {
            throw e.toIOException();
        }
    }

}
