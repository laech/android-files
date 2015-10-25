package l.files.fs.local;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static java.lang.System.loadLibrary;

class Native {

    private static boolean loaded = false;

    static {
        load();
    }

    synchronized static void load() {
        if (!loaded) {
            loaded = true;

            if (SDK_INT >= LOLLIPOP) {
                loadLibrary("fslocalstat64");
            } else {
                loadLibrary("fslocalstat");
            }
            loadLibrary("fslocal");
        }
    }

}
