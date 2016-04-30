package linux;

public final class Stdio extends Native {

    private Stdio() {
    }

    public static native void remove(byte[] path) throws ErrnoException;

    public static native void rename(byte[] oldpath, byte[] newpath) throws ErrnoException;

}
