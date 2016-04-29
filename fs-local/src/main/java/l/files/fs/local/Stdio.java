package l.files.fs.local;

import linux.ErrnoException;

final class Stdio extends Native {

    private Stdio() {
    }

    static native void remove(byte[] path) throws ErrnoException;

    static native void rename(byte[] oldpath, byte[] newpath) throws ErrnoException;

}
