package l.files.operations;

import l.files.fs.Resource;
import l.files.fs.local.ResourceBaseTest;

import static java.util.Arrays.asList;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class SizeTest extends ResourceBaseTest {

    public void testSize() throws Exception {
        Resource a = dir1().resolve("a").createDirectory();
        Resource b = dir1().resolve("a/b").createFile();
        Resource c = dir1().resolve("c").createFile();
        Resource d = dir1().resolve("d").createDirectory();

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
