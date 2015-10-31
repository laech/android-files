package l.files.testing;

import java.io.File;

final class Dexmaker {

    /**
     * Workaround for https://code.google.com/p/dexmaker/issues/detail?id=2
     */
    static void setup(File dir) {
        System.setProperty("dexmaker.dexcache", dir.getAbsolutePath());
    }

    private Dexmaker() {
    }
}
