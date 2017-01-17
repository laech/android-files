package l.files.operations;

import java.io.File;

import l.files.fs.Path;
import l.files.fs.local.LocalPath;
import l.files.testing.fs.PathBaseTest;

import static java.util.Arrays.asList;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class SizeTest extends PathBaseTest {

    @Override
    protected Path create(File file) {
        return LocalPath.fromFile(file);
    }

    public void test_size() throws Exception {
        Path a = dir1().concat("a").createDir();
        Path b = dir1().concat("a/b").createFile();
        Path c = dir1().concat("c").createFile();
        Path d = dir1().concat("d").createDir();

        Size size = new Size(asList(a, b, c, d));
        size.execute();

        long expected
                = a.stat(NOFOLLOW).size()
                + b.stat(NOFOLLOW).size()
                + c.stat(NOFOLLOW).size()
                + d.stat(NOFOLLOW).size();
        assertEquals(expected, size.getSize());
    }

}
