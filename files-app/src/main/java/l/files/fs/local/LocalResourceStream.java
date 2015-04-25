package l.files.fs.local;

import java.io.Closeable;
import java.io.IOException;

import l.files.fs.LinkOption;
import l.files.fs.NotDirectoryException;

import static java.util.Objects.requireNonNull;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;

final class LocalResourceStream extends Native implements Closeable {

    /*
     * Design note: this basically uses <dirent.h> to read directory entries,
     * returning simple DirectoryStream.Entry without using stat/lstat will yield
     * much better performance when directory is large.
     */

    static {
        init();
    }

    private final LocalResource parent;
    private final long dir;
    private final Callback callback;

    private LocalResourceStream(
            LocalResource parent,
            long dir,
            Callback callback) {
        this.parent = parent;
        this.dir = dir;
        this.callback = callback;
    }

    @Override
    public void close() throws IOException {
        try {
            close(dir);
        } catch (ErrnoException e) {
            throw e.toIOException(parent.getPath());
        }
    }

    public static void list(
            LocalResource resource,
            LinkOption option,
            Callback callback) throws IOException {

        requireNonNull(resource, "resource");
        requireNonNull(option, "option");
        requireNonNull(callback, "callback");

        try {
            long dir = open(resource.getPath(), option == FOLLOW);
            try (LocalResourceStream stream = new LocalResourceStream(resource, dir, callback)) {
                stream.list(dir);
            }
        } catch (ErrnoException e) {
            if (option == NOFOLLOW && e.isCausedByNoFollowLink(resource)) {
                throw new NotDirectoryException(resource.getPath(), e);
            }
            throw e.toIOException(resource.getPath());
        }
    }

    @SuppressWarnings("unused") // Called from native code
    private boolean notify(long ino, String name, boolean directory)
            throws IOException {
        return ".".equals(name)
                || "..".equals(name)
                || callback.accept(ino, name, directory);
    }

    private native void list(long dir) throws ErrnoException;

    private static native long open(String path, boolean followLink)
            throws ErrnoException;

    private static native void close(long dir) throws ErrnoException;

    private static native void init();

    interface Callback {
        boolean accept(long inode, String name, boolean directory)
                throws IOException;
    }

}
