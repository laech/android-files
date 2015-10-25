package l.files.fs.local;

final class Stdio extends Native {

    private Stdio() {
    }

    static native void remove(String path) throws ErrnoException;

    static native void rename(String oldpath, String newpath) throws ErrnoException;

}
