package l.files.fs.local;

import android.system.ErrnoException;

import java.io.Closeable;
import java.io.IOException;

import l.files.fs.LinkOption;
import l.files.fs.NotDirectory;
import l.files.fs.Resource;

import static java.util.Objects.requireNonNull;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.local.ErrnoExceptions.isCausedByNoFollowLink;
import static l.files.fs.local.ErrnoExceptions.toIOException;

final class LocalResourceStream extends Native implements Closeable
{

    /*
     * Design note: this basically uses <dirent.h> to read directory entries,
     * returning simple DirectoryStream.Entry without using stat/lstat will
     * yield much better performance when directory is large.
     */

    static
    {
        init();
    }

    private final Resource parent;
    private final long dir;
    private final Callback callback;

    private LocalResourceStream(
            final Resource parent,
            final long dir,
            final Callback callback)
    {
        this.parent = parent;
        this.dir = dir;
        this.callback = callback;
    }

    @Override
    public void close() throws IOException
    {
        try
        {
            close(dir);
        }
        catch (final ErrnoException e)
        {
            throw toIOException(e, parent.path());
        }
    }

    public static void list(
            final Resource resource,
            final LinkOption option,
            final Callback callback) throws IOException
    {
        requireNonNull(resource, "resource");
        requireNonNull(option, "option");
        requireNonNull(callback, "callback");

        try
        {
            final long dir = open(resource.path(), option == FOLLOW);
            try (LocalResourceStream stream =
                         new LocalResourceStream(resource, dir, callback))
            {
                stream.list(dir);
            }
        }
        catch (final ErrnoException e)
        {
            if (option == NOFOLLOW && isCausedByNoFollowLink(e, resource))
            {
                throw new NotDirectory(resource.path(), e);
            }
            throw toIOException(e, resource.path());
        }
    }

    @SuppressWarnings("unused") // Called from native code
    private boolean notify(
            final long ino,
            final String name,
            final boolean directory) throws IOException
    {
        return ".".equals(name)
                || "..".equals(name)
                || callback.accept(ino, name, directory);
    }

    private native void list(long dir) throws ErrnoException;

    private static native long open(String path, boolean followLink)
            throws ErrnoException;

    private static native void close(long dir) throws ErrnoException;

    private static native void init();

    interface Callback
    {
        boolean accept(long inode, String name, boolean directory)
                throws IOException;
    }

}
