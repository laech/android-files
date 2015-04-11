package l.files.fs.local;

import l.files.common.testing.FileBaseTest;

public abstract class LocalFileTypeDetectorTest extends FileBaseTest {

    /**
     * The detector to be tested, using the given file system.
     */
    protected abstract LocalFileTypeDetector detector();

    public void testDetect_directory() throws Exception {
        LocalResource dir = LocalResource.create(tmp().createDir("a"));
        assertEquals("inode/directory", detector().detect(dir).toString());
    }

    public void testDetect_file() throws Exception {
        LocalResource file = LocalResource.create(tmp().createFile("a.txt"));
        assertEquals("text/plain", detector().detect(file).toString());
    }

    public void testDetect_symlinkFile() throws Exception {
        LocalResource file = LocalResource.create(tmp().createFile("a.mp3"));
        LocalResource link = LocalResource.create(tmp().get("b.txt"));
        link.createSymbolicLink(file);
        assertEquals("text/plain", detector().detect(link).toString());
    }

    public void testDetect_symlinkDirectory() throws Exception {
        LocalResource dir = LocalResource.create(tmp().createDir("a"));
        LocalResource link = LocalResource.create(tmp().get("b"));
        link.createSymbolicLink(dir);
        assertEquals("inode/directory", detector().detect(link).toString());
    }

    public void testDetect_symlinkDirectoryMulti() throws Exception {
        LocalResource dir = LocalResource.create(tmp().createDir("a"));
        LocalResource link1 = LocalResource.create(tmp().get("b"));
        LocalResource link2 = LocalResource.create(tmp().get("c"));
        link1.createSymbolicLink(dir);
        link2.createSymbolicLink(link1);
        assertEquals("inode/directory", detector().detect(link2).toString());
    }

}
