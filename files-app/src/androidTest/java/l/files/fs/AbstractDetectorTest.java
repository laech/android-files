package l.files.fs;

import l.files.fs.local.ResourceBaseTest;

public abstract class AbstractDetectorTest extends ResourceBaseTest
{
    /**
     * The detector to be tested, using the given file system.
     */
    protected abstract AbstractDetector detector();

    public void testDetect_directory() throws Exception
    {
        final Resource dir = dir1().resolve("a").createDirectory();
        assertEquals("inode/directory", detector().detect(dir).toString());
    }

    public void testDetect_file() throws Exception
    {
        final Resource file = dir1().resolve("a.txt").createFile();
        assertEquals("text/plain", detector().detect(file).toString());
    }

    public void testDetect_symlinkFile() throws Exception
    {
        final Resource file = dir1().resolve("a.mp3").createFile();
        final Resource link = dir1().resolve("b.txt").createLink(file);
        assertEquals("text/plain", detector().detect(link).toString());
    }

    public void testDetect_symlinkDirectory() throws Exception
    {
        final Resource dir = dir1().resolve("a").createDirectory();
        final Resource link = dir1().resolve("b").createLink(dir);
        assertEquals("inode/directory", detector().detect(link).toString());
    }

    public void testDetect_symlinkDirectoryMulti() throws Exception
    {
        final Resource dir = dir1().resolve("a").createDirectory();
        final Resource link1 = dir1().resolve("b").createLink(dir);
        final Resource link2 = dir1().resolve("c").createLink(link1);
        assertEquals("inode/directory", detector().detect(link2).toString());
    }
}
