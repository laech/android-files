package l.files.testing;

import android.content.Context;
import android.test.InstrumentationTestCase;

import java.io.File;

public abstract class BaseTest extends InstrumentationTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Dexmaker.setup(this);
        delete(getTestContext().getExternalCacheDir());
    }

    @Override
    protected void tearDown() throws Exception {
        File dir = getTestContext().getExternalCacheDir();
        if (dir != null) {
            File[] children = dir.listFiles();
            if (children != null) {
                for (File child : children) {
                    delete(child);
                }
            }
        }
        super.tearDown();
    }

    private void delete(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    delete(child);
                }
            }
        }
        assertTrue(file.delete() || !file.exists());
    }

    protected Context getContext() {
        return getInstrumentation().getTargetContext();
    }

    protected Context getTestContext() {
        return getInstrumentation().getContext();
    }

}
