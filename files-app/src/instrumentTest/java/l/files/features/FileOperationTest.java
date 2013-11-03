package l.files.features;

import l.files.test.BaseFilesActivityTest;

import java.io.File;

import static org.apache.commons.io.FileUtils.waitFor;

public final class FileOperationTest extends BaseFilesActivityTest {

    public void testCopy() throws Exception {
        final File a = dir().newFile("a");
        final File b = dir().newFile("b");
        final File c = dir().newDir("c");
        final File d = dir().newDir("d");

        screen().check(a, true)
                .check(b, true)
                .check(c, true)
                .copy()
                .selectItem(d)
                .paste();

        assertTrue(waitFor(new File(dir().get(), "d/a"), 5));
        assertTrue(waitFor(new File(dir().get(), "d/b"), 5));
        assertTrue(waitFor(new File(dir().get(), "d/c"), 5));
    }
}
