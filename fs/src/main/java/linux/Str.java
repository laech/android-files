package linux;

import l.files.fs.Native;

public final class Str extends Native {

    private Str() {
    }

    public static native String strerror(int errnum);

}
