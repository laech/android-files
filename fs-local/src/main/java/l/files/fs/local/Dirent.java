package l.files.fs.local;

import com.google.auto.value.AutoValue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import l.files.fs.LinkOption;
import l.files.fs.Stream;

import static l.files.fs.File.UTF_8;
import static l.files.fs.LinkOption.FOLLOW;

/**
 * @see <a href="http://pubs.opengroup.org/onlinepubs/7908799/xsh/dirent.h.html">dirent.h</a>
 */
final class Dirent extends Native {

    private static final byte[] NAME_SELF = ".".getBytes(UTF_8);
    private static final byte[] NAME_PARENT = "..".getBytes(UTF_8);

    static final int DT_UNKNOWN = 0;
    static final int DT_FIFO = 1;
    static final int DT_CHR = 2;
    static final int DT_DIR = 4;
    static final int DT_BLK = 6;
    static final int DT_REG = 8;
    static final int DT_LNK = 10;
    static final int DT_SOCK = 12;
    static final int DT_WHT = 14;

    private final long inode;
    private final int type;
    private final byte[] name;

    Dirent(long inode, int type, byte[] name) {
        this.inode = inode;
        this.type = type;
        this.name = name;
    }

    long inode() {
        return inode;
    }

    int type() {
        return type;
    }

    byte[] name() {
        return name;
    }

    static Dirent create(long inode, int type, byte[] name) {
        return new Dirent(inode, type, name);
    }

    static Stream<Dirent> stream(
            final LocalFile file,
            final LinkOption option,
            final boolean dirOnly) throws IOException {

        final long dir;
        try {
            dir = opendir(file.path().bytes(), option == FOLLOW);
        } catch (ErrnoException e) {
            throw e.toIOException(file.path());
        }

        return new Stream<Dirent>() {

            boolean closed;

            @Override
            public void close() throws IOException {
                if (closed) {
                    return;
                }
                closed = true;

                try {
                    closedir(dir);
                } catch (ErrnoException e) {
                    throw e.toIOException();
                }
            }

            @Override
            public Iterator<Dirent> iterator() {
                return Dirent.iterator(dir, dirOnly);
            }

        };
    }

    private static Iterator<Dirent> iterator(
            final long dir,
            final boolean dirOnly) {

        return new Iterator<Dirent>() {

            Dirent next;

            @Override
            public boolean hasNext() {
                if (next == null) {
                    readNext();
                }
                return next != null;
            }

            private void readNext() {
                do {
                    try {

                        next = readdir(dir);

                        if (next == null) {
                            continue;
                        }
                        if (dirOnly && next.type() != DT_DIR) {
                            continue;
                        }
                        if (Arrays.equals(next.name(), NAME_SELF)) {
                            continue;
                        }
                        if (Arrays.equals(next.name(), NAME_PARENT)) {
                            continue;
                        }

                        break;

                    } catch (ErrnoException e) {
                        this.<RuntimeException>rethrow(e.toIOException());
                    }
                }
                while (next != null);
            }

            @SuppressWarnings("unchecked")
            private <E extends Exception> void rethrow(Exception e) throws E {
                throw (E) e;
            }

            @Override
            public Dirent next() {
                if (next == null) {
                    readNext();
                }
                if (next == null) {
                    throw new NoSuchElementException();
                }
                Dirent result = next;
                next = null;
                return result;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

        };
    }

    static {
        init();
    }

    private static native void init();

    /**
     * @see <a href="http://pubs.opengroup.org/onlinepubs/7908799/xsh/opendir.html">opendir()</a>
     */
    static native long opendir(byte[] path, boolean followLink) throws ErrnoException;

    /**
     * @see <a href="http://pubs.opengroup.org/onlinepubs/7908799/xsh/closedir.html">closedir()</a>
     */
    static native void closedir(long dir) throws ErrnoException;

    /**
     * Note: this will also return the "." and  ".." directories.
     *
     * @see <a href="http://pubs.opengroup.org/onlinepubs/7908799/xsh/readdir.html">readdir()</a>
     */
    static native Dirent readdir(long dir) throws ErrnoException;

}
