package l.files.fs.local;

import android.system.ErrnoException;

/**
 * @see <a href="http://pubs.opengroup.org/onlinepubs/7908799/xsh/unistd.h.html">unistd.h</a>
 */
final class Unistd extends Native
{

    private Unistd()
    {
    }

    /**
     * @see <a href="http://pubs.opengroup.org/onlinepubs/7908799/xsh/close.html">close</a>
     */
    public static native void close(int fd) throws ErrnoException;

}
