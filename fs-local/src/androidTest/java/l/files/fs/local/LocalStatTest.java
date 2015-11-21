package l.files.fs.local;

import org.junit.Test;

import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.local.Stat.lstat;
import static l.files.fs.local.Stat.stat;
import static org.junit.Assert.assertEquals;

public final class LocalStatTest extends FileBaseTest {

    @Test
    public void local_stat() throws Exception {
        LocalFile link = dir1().resolve("link").createLink(dir2());
        assertEquals(lstat(link.path().bytes()), link.stat(NOFOLLOW));
        assertEquals(stat(link.path().bytes()), link.stat(FOLLOW));
    }

}
