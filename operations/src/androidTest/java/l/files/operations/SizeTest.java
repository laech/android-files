package l.files.operations;

import org.junit.Test;

import l.files.fs.Path;
import l.files.testing.fs.PathBaseTest;

import static java.util.Arrays.asList;
import static l.files.fs.LinkOption.NOFOLLOW;
import static org.junit.Assert.assertEquals;

public final class SizeTest extends PathBaseTest {

    @Test
    public void size() throws Exception {
        Path a = dir1().concat("a").createDirectory();
        Path b = dir1().concat("a/b").createFile();
        Path c = dir1().concat("c").createFile();
        Path d = dir1().concat("d").createDirectory();

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
