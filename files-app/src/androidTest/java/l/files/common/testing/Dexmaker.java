package l.files.common.testing;

import android.test.AndroidTestCase;
import android.test.InstrumentationTestCase;

import java.io.File;

final class Dexmaker {

    /**
     * Workaround for https://code.google.com/p/dexmaker/issues/detail?id=2
     */
    static void setup(AndroidTestCase test) {
        setup(test.getContext().getCacheDir());
    }

    /**
     * Workaround for https://code.google.com/p/dexmaker/issues/detail?id=2
     */
    static void setup(InstrumentationTestCase test) {
        setup(test.getInstrumentation().getTargetContext().getCacheDir());
    }

    private static void setup(File dir) {
        System.setProperty("dexmaker.dexcache", dir.getAbsolutePath());
    }

    private Dexmaker() {
    }
}
