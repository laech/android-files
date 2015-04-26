package l.files.fs.local;

import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.local.Stat.lstat;
import static l.files.fs.local.Stat.stat;

public final class LocalResourceStatusTest extends ResourceBaseTest {

    public void testStat() throws Exception {
        LocalResource link = (LocalResource) dir1().resolve("link").createSymbolicLink(dir2());
        assertEquals(lstat(link.getPath()), link.readStatus(NOFOLLOW).getStat());
        assertEquals(stat(link.getPath()), link.readStatus(FOLLOW).getStat());
    }

}
