package l.files.fs.local;

import l.files.common.testing.FileBaseTest;

public abstract class LocalFileTypeDetectorTest extends FileBaseTest {

    /**
     * The detector to be tested, using the given file system.
     */
    protected abstract LocalFileTypeDetector detector();

    public void testDetect_directory() throws Exception {
        LocalResource dir = LocalPath.of(tmp().createDir("a")).getResource();
        assertEquals("inode/directory", detector().detect(dir).toString());
    }

    public void testDetect_file() throws Exception {
        LocalResource file = LocalPath.of(tmp().createFile("a.txt")).getResource();
        assertEquals("text/plain", detector().detect(file).toString());
    }

    public void testDetect_symlinkFile() throws Exception {
        LocalResource file = LocalPath.of(tmp().createFile("a.mp3")).getResource();
        LocalResource link = LocalPath.of(tmp().get("b.txt")).getResource();
        link.getResource().createSymbolicLink(file);
        assertEquals("text/plain", detector().detect(link).toString());
    }

    public void testDetect_symlinkDirectory() throws Exception {
        LocalResource dir = LocalPath.of(tmp().createDir("a")).getResource();
        LocalResource link = LocalPath.of(tmp().get("b")).getResource();
        link.getResource().createSymbolicLink(dir);
        assertEquals("inode/directory", detector().detect(link).toString());
    }

    public void testDetect_symlinkDirectoryMulti() throws Exception {
        LocalResource dir = LocalPath.of(tmp().createDir("a")).getResource();
        LocalResource link1 = LocalPath.of(tmp().get("b")).getResource();
        LocalResource link2 = LocalPath.of(tmp().get("c")).getResource();
        link1.getResource().createSymbolicLink(dir);
        link2.getResource().createSymbolicLink(link1);
        assertEquals("inode/directory", detector().detect(link2).toString());
    }

}
