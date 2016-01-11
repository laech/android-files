package l.files.operations;

import l.files.fs.Path;
import l.files.testing.fs.PathBaseTest;

import static java.util.Arrays.asList;
import static l.files.fs.Files.createDir;
import static l.files.fs.Files.createFile;
import static l.files.fs.Files.stat;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class SizeTest extends PathBaseTest {

    public void teset_size() throws Exception {
        Path a = createDir(dir1().resolve("a"));
        Path b = createFile(dir1().resolve("a/b"));
        Path c = createFile(dir1().resolve("c"));
        Path d = createDir(dir1().resolve("d"));

        Size size = new Size(asList(a, b, c, d));
        size.execute();

        long expected
                = stat(a, NOFOLLOW).size()
                + stat(b, NOFOLLOW).size()
                + stat(c, NOFOLLOW).size()
                + stat(d, NOFOLLOW).size();
        assertEquals(expected, size.getSize());
    }

}
