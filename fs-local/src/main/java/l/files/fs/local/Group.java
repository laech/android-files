package l.files.fs.local;

import android.system.ErrnoException;

import com.google.auto.value.AutoValue;

/**
 * @see <a href="http://pubs.opengroup.org/onlinepubs/7908799/xsh/grp.h.html">grp.h</a>
 */
@AutoValue
abstract class Group extends Native {

    Group() {
    }

    public abstract String getName();

    public abstract int getGid();

    public static Group create(String name, int gid) {
        return new AutoValue_Group(name, gid);
    }

    static {
        init();
    }

    private static native void init();

    /**
     * @see <a href="http://pubs.opengroup.org/onlinepubs/7908799/xsh/getgrgid.html">getgrid()</a>
     */
    public static native Group getgrgid(int gid) throws ErrnoException;

}
