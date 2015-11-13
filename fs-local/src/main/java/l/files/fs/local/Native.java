package l.files.fs.local;

import static java.lang.System.loadLibrary;

class Native {

    private static boolean loaded = false;

    static {
        load();
    }

    synchronized static void load() {
        if (!loaded) {
            loaded = true;
            loadLibrary("fslocal");
        }
    }

}
