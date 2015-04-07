package l.files.fs.local;

import java.io.File;

import l.files.common.testing.FileBaseTest;

import static com.google.common.io.Files.write;
import static java.nio.charset.StandardCharsets.UTF_8;
import static l.files.fs.local.LocalResourceStatus.stat;
import static l.files.fs.local.Stat.lstat;
import static l.files.fs.local.Unistd.symlink;

public final class LocalResourceStatusTest extends FileBaseTest {

    public void testSymbolicLink() throws Exception {
        File file = tmp().createDir("file");
        File link = tmp().get("link");
        symlink(file.getPath(), link.getPath());
        assertFalse(stat(file, false).isSymbolicLink());
        assertFalse(stat(link, true).isSymbolicLink());
        assertTrue(stat(link, false).isSymbolicLink());
        assertEquals(Stat.lstat(link.getPath()), stat(link, false).getStat());
        assertEquals(Stat.stat(link.getPath()), stat(link, true).getStat());
    }

    public void testIsDirectory() throws Exception {
        File dir = tmp().createDir("a");
        assertTrue(stat(dir, false).isDirectory());
    }

    public void testIsRegularFile() throws Exception {
        File file = tmp().createFile("a");
        assertTrue(stat(file, false).isRegularFile());
    }

    public void testInodeNumber() throws Exception {
        File f = tmp().createFile("a");
        assertEquals(lstat(f.getPath()).getIno(), stat(f, false).getInode());
    }

    public void testDeviceId() throws Exception {
        File f = tmp().createFile("a");
        assertEquals(lstat(f.getPath()).getDev(), stat(f, false).getDevice());
    }

    public void testLastModifiedTime() throws Exception {
        LocalResourceStatus file = stat(tmp().get(), false);
        assertEquals(tmp().get().lastModified(), file.getLastModifiedTime());
    }

    public void testReadable() throws Exception {
        File file = tmp().createFile("a");
        assertTrue(file.setReadable(false));
        assertFalse(stat(file, false).isReadable());
        assertTrue(file.setReadable(true));
        assertTrue(stat(file, false).isReadable());
    }

    public void testWritable() throws Exception {
        File file = tmp().createFile("a");
        assertTrue(file.setWritable(false));
        assertFalse(stat(file, false).isWritable());
        assertTrue(file.setWritable(true));
        assertTrue(stat(file, false).isWritable());
    }

    public void testName() throws Exception {
        File file = tmp().createFile("a");
        assertEquals(file.getName(), stat(file, false).getName());
    }

    public void testSize() throws Exception {
        File file = tmp().createFile("a");
        write("hello world", file, UTF_8);
        assertEquals(file.length(), stat(file, false).getSize());
    }

    public void testBasicMediaType() throws Exception {
        File file = tmp().createFile("a.txt");
        assertEquals("text/plain", stat(file, false).getBasicMediaType().toString());
    }

    public void testBasicMediaTypeOfUnreadableLinkTargetShouldNotCrash() throws Exception {
        File file = tmp().createFile("a/a.txt");
        File parent = file.getParentFile();
        File link = tmp().get("link");
        LocalPath.of(link).getResource().createSymbolicLink(LocalPath.of(file).getResource());

        assertTrue(parent.setReadable(false));
        assertTrue(parent.setExecutable(false));

        assertEquals("application/octet-stream", stat(link, false).getBasicMediaType().toString());
    }

}
