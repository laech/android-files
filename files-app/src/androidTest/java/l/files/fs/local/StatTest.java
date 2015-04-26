package l.files.fs.local;

import android.system.Os;
import android.system.StructStat;

import static android.system.OsConstants.ENOENT;

public final class StatTest extends ResourceBaseTest {

    public void testException() {
        try {
            Stat.stat("/not/exist");
            fail();
        } catch (ErrnoException e) {
            assertEquals(ENOENT, e.errno());
        }
    }

    public void testStat() throws Exception {
        String path = dir1().resolve("link").createSymbolicLink(dir2()).getPath();
        checkEquals(Os.stat(path), Stat.stat(path));
        checkEquals(Os.lstat(path), Stat.lstat(path));
    }

    private static void checkEquals(StructStat expected, Stat actual) {
        assertEquals(expected.st_mtime, actual.getMtime());
        assertEquals(expected.st_atime, actual.getAtime());
        assertEquals(expected.st_ctime, actual.getCtime());
        assertEquals(expected.st_blksize, actual.getBlksize());
        assertEquals(expected.st_blocks, actual.getBlocks());
        assertEquals(expected.st_dev, actual.getDev());
        assertEquals(expected.st_gid, actual.getGid());
        assertEquals(expected.st_mode, actual.getMode());
        assertEquals(expected.st_uid, actual.getUid());
        assertEquals(expected.st_ino, actual.getIno());
        assertEquals(expected.st_nlink, actual.getNlink());
        assertEquals(expected.st_rdev, actual.getRdev());
        assertEquals(expected.st_size, actual.getSize());
    }

}
