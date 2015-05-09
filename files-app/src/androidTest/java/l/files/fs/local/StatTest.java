package l.files.fs.local;

import android.system.Os;
import android.system.StructStat;

import static android.system.OsConstants.ENOENT;

public final class StatTest extends ResourceBaseTest
{

    public void testException()
    {
        try
        {
            Stat.stat("/not/exist");
            fail();
        }
        catch (final ErrnoException e)
        {
            assertEquals(ENOENT, e.errno());
        }
    }

    public void testStat() throws Exception
    {
        final String path = dir1().resolve("link").createLink(dir2()).path();
        checkEquals(Os.stat(path), Stat.stat(path));
        checkEquals(Os.lstat(path), Stat.lstat(path));
    }

    private static void checkEquals(final StructStat expected, final Stat actual)
    {
        assertEquals(expected.st_mtime, actual.mtime());
        assertEquals(expected.st_atime, actual.atime());
        assertEquals(expected.st_ctime, actual.ctime());
        assertEquals(expected.st_blksize, actual.blksize());
        assertEquals(expected.st_blocks, actual.blocks());
        assertEquals(expected.st_dev, actual.dev());
        assertEquals(expected.st_gid, actual.gid());
        assertEquals(expected.st_mode, actual.mode());
        assertEquals(expected.st_uid, actual.uid());
        assertEquals(expected.st_ino, actual.ino());
        assertEquals(expected.st_nlink, actual.nlink());
        assertEquals(expected.st_rdev, actual.rdev());
        assertEquals(expected.st_size, actual.size());
    }

}
