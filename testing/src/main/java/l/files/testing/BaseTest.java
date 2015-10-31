package l.files.testing;

import android.content.Context;

import org.junit.After;
import org.junit.Before;

import java.io.File;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertTrue;

public abstract class BaseTest {

    @Before
    public void setUp() throws Exception {
        Dexmaker.setup(getContext().getCacheDir());
        delete(getTestContext().getExternalCacheDir());
    }

    @After
    public void tearDown() throws Exception {
        delete(getTestContext().getExternalCacheDir());
    }

    private void delete(File file) {
        if (file == null) {
            return;
        }
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
