package l.files.fs.local;

import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.local.Stat.lstat;
import static l.files.fs.local.Stat.stat;

public final class LocalStatTest extends ResourceBaseTest
{

    public void testStat() throws Exception
    {
        final LocalResource link =
                (LocalResource) dir1().resolve("link").createLink(dir2());
        assertEquals(lstat(link.path()), link.stat(NOFOLLOW).stat());
        assertEquals(stat(link.path()), link.stat(FOLLOW).stat());
    }

}