package l.files.fs.local;

import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.local.Stat.lstat64;
import static l.files.fs.local.Stat.stat64;

public final class LocalStatTest extends FileBaseTest {

    public void testStat() throws Exception {
        final LocalFile link =
                (LocalFile) dir1().resolve("link").createLink(dir2());
        assertEquals(lstat64(link.path()), link.stat(NOFOLLOW).stat());
        assertEquals(stat64(link.path()), link.stat(FOLLOW).stat());
    }

}
