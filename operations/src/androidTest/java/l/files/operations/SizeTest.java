package l.files.operations;

import l.files.fs.Path;
import l.files.fs.local.LocalFileSystem;
import l.files.testing.fs.PathBaseTest;

import static java.util.Arrays.asList;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class SizeTest extends PathBaseTest {

    public SizeTest() {
        super(LocalFileSystem.INSTANCE);
    }

    public void test_size() throws Exception {
        Path a = fs.createDir(dir1().concat("a"));
        Path b = fs.createFile(dir1().concat("a/b"));
        Path c = fs.createFile(dir1().concat("c"));
        Path d = fs.createDir(dir1().concat("d"));

        Size size = new Size(asList(a, b, c, d));
        size.execute();

        long expected
                = fs.stat(a, NOFOLLOW).size()
                + fs.stat(b, NOFOLLOW).size()
                + fs.stat(c, NOFOLLOW).size()
                + fs.stat(d, NOFOLLOW).size();
        assertEquals(expected, size.getSize());
    }

}
