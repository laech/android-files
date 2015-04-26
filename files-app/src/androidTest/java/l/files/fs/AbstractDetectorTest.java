package l.files.fs;

import l.files.fs.local.ResourceBaseTest;

public abstract class AbstractDetectorTest extends ResourceBaseTest {

    /**
     * The detector to be tested, using the given file system.
     */
    protected abstract AbstractDetector detector();

    public void testDetect_directory() throws Exception {
        Resource dir = dir1().resolve("a").createDirectory();
        assertEquals("inode/directory", detector().detect(dir).toString());
    }

    public void testDetect_file() throws Exception {
        Resource file = dir1().resolve("a.txt").createFile();
        assertEquals("text/plain", detector().detect(file).toString());
    }

    public void testDetect_symlinkFile() throws Exception {
        Resource file = dir1().resolve("a.mp3").createFile();
        Resource link = dir1().resolve("b.txt").createSymbolicLink(file);
        assertEquals("text/plain", detector().detect(link).toString());
    }

    public void testDetect_symlinkDirectory() throws Exception {
        Resource dir = dir1().resolve("a").createDirectory();
        Resource link = dir1().resolve("b").createSymbolicLink(dir);
        assertEquals("inode/directory", detector().detect(link).toString());
    }

    public void testDetect_symlinkDirectoryMulti() throws Exception {
        Resource dir = dir1().resolve("a").createDirectory();
        Resource link1 = dir1().resolve("b").createSymbolicLink(dir);
        Resource link2 = dir1().resolve("c").createSymbolicLink(link1);
        assertEquals("inode/directory", detector().detect(link2).toString());
    }

}
