package l.files.fs.local;

import auto.parcel.AutoParcel;

/**
 * @see <a href="http://pubs.opengroup.org/onlinepubs/7908799/xsh/grp.h.html">grp.h</a>
 */
@AutoParcel
abstract class Group extends Native {

    Group() {
    }

    public abstract String getName();

    public abstract int getGid();

    public static Group create(String name, int gid) {
        return new AutoParcel_Group(name, gid);
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
