package l.files.fs.local;

import com.google.auto.value.AutoValue;

/**
 * @see <a href="http://pubs.opengroup.org/onlinepubs/7908799/xsh/pwd.h.html">pwd.h</a>
 */
@AutoValue
abstract class Passwd extends Native {

    Passwd() {
    }

    public abstract String getName();

    public abstract int getUid();

    public abstract int getGid();

    public abstract String getDir();

    public abstract String getShell();

    public static Passwd create(String name, int uid, int gid, String dir, String shell) {
        return new AutoValue_Passwd(name, uid, gid, dir, shell);
    }

    static {
        init();
    }

    private static native void init();

    /**
     * @see <a href="http://pubs.opengroup.org/onlinepubs/7908799/xsh/getpwuid.html">getpwuid()</a>
     */
    public static native Passwd getpwuid(int uid) throws ErrnoException;

}
