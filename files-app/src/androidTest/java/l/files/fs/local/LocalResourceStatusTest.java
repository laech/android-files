package l.files.fs.local;

import android.system.Os;
import android.system.StructStat;

import java.io.File;

import l.files.common.testing.FileBaseTest;

import static com.google.common.io.Files.write;
import static java.nio.charset.StandardCharsets.UTF_8;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.local.LocalResourceStatus.stat;
import static l.files.fs.local.Stat.lstat;
import static l.files.fs.local.Unistd.symlink;

public final class LocalResourceStatusTest extends FileBaseTest {

    public void testSymbolicLink() throws Exception {
        File file = tmp().createDir("file");
        File link = tmp().get("link");
        symlink(file.getPath(), link.getPath());
        assertFalse(stat(LocalResource.create(file), NOFOLLOW).isSymbolicLink());
        assertFalse(stat(LocalResource.create(link), FOLLOW).isSymbolicLink());
        assertTrue(stat(LocalResource.create(link), NOFOLLOW).isSymbolicLink());
        assertEquals(Stat.lstat(LocalResource.create(link).getFile().getPath()), stat(LocalResource.create(link), NOFOLLOW).getStat());
        assertEquals(Stat.stat(LocalResource.create(link).getFile().getPath()), stat(LocalResource.create(link), FOLLOW).getStat());
    }

    public void testIsDirectory() throws Exception {
        File dir = tmp().createDir("a");
        assertTrue(stat(LocalResource.create(dir), NOFOLLOW).isDirectory());
    }

    public void testIsRegularFile() throws Exception {
        File file = tmp().createFile("a");
        assertTrue(stat(LocalResource.create(file), NOFOLLOW).isRegularFile());
    }

    public void testInodeNumber() throws Exception {
        File f = tmp().createFile("a");
        assertEquals(lstat(LocalResource.create(f).getFile().getPath()).getIno(), stat(LocalResource.create(f), NOFOLLOW).getInode());
    }

    public void testDeviceId() throws Exception {
        File f = tmp().createFile("a");
        assertEquals(lstat(LocalResource.create(f).getFile().getPath()).getDev(), stat(LocalResource.create(f), NOFOLLOW).getDevice());
    }

    public void testModificationTime() throws Exception {
        LocalResourceStatus actual = stat(LocalResource.create(tmp().get()), NOFOLLOW);
        StructStat expected = Os.stat(tmp().get().getPath());
        assertEquals(expected.st_mtime, actual.getModificationTime().getSeconds());
    }

    public void testAccessTime() throws Exception {
        LocalResourceStatus actual = stat(LocalResource.create(tmp().get()), NOFOLLOW);
        StructStat expected = Os.stat(tmp().get().getPath());
        assertEquals(expected.st_atime, actual.getModificationTime().getSeconds());
    }

    public void testName() throws Exception {
        File file = tmp().createFile("a");
        assertEquals(file.getName(), stat(LocalResource.create(file), NOFOLLOW).getName());
    }

    public void testSize() throws Exception {
        File file = tmp().createFile("a");
        write("hello world", file, UTF_8);
        assertEquals(file.length(), stat(LocalResource.create(file), NOFOLLOW).getSize());
    }

    public void testBasicMediaType() throws Exception {
        File file = tmp().createFile("a.txt");
        assertEquals("text/plain", stat(LocalResource.create(file), NOFOLLOW).getBasicMediaType().toString());
    }

    public void testBasicMediaTypeOfUnreadableLinkTargetShouldNotCrash() throws Exception {
        File file = tmp().createFile("a/a.txt");
        File parent = file.getParentFile();
        File link = tmp().get("link");
        LocalResource.create(link).createSymbolicLink(LocalResource.create(file));

        assertTrue(parent.setReadable(false));
        assertTrue(parent.setExecutable(false));

        assertEquals("application/octet-stream", stat(LocalResource.create(link), NOFOLLOW).getBasicMediaType().toString());
    }

}
