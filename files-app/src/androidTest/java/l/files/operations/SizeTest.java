package l.files.operations;

import l.files.fs.File;
import l.files.fs.local.FileBaseTest;

import static java.util.Arrays.asList;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class SizeTest extends FileBaseTest {

    public void testSize() throws Exception {
        File a = dir1().resolve("a").createDir();
        File b = dir1().resolve("a/b").createFile();
        File c = dir1().resolve("c").createFile();
        File d = dir1().resolve("d").createDir();

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
