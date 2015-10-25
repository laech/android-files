package l.files.fs.local;

import java.io.IOException;

import l.files.fs.File;

public abstract class AbstractDetectorTest extends FileBaseTest {

    /**
     * The detector to be tested, using the given file system.
     */
    abstract AbstractDetector detector();

    public void test_detects_directory_type() throws Exception {
        File dir = dir1().resolve("a").createDir();
        assertEquals("inode/directory", detector().detect(dir));
    }

    public void test_detects_file_type() throws Exception {
        File file = createTextFile("a.txt");
        assertEquals("text/plain", detector().detect(file));
    }

    public void test_detects_file_type_uppercase_extension() throws Exception {
        File file = createTextFile("a.TXT");
        assertEquals("text/plain", detector().detect(file));
    }

    public void test_detects_linked_file_type() throws Exception {
        File file = createTextFile("a.mp3");
        File link = dir1().resolve("b.txt").createLink(file);
        assertEquals("text/plain", detector().detect(link));
    }

    private File createTextFile(String name) throws IOException {
        File file = dir1().resolve(name).createFile();
        file.writeAllUtf8("hello world");
        return file;
    }

    public void test_detects_linked_directory_type() throws Exception {
        File dir = dir1().resolve("a").createDir();
        File link = dir1().resolve("b").createLink(dir);
        assertEquals("inode/directory", detector().detect(link));
    }

    public void test_detects_multi_linked_directory_type() throws Exception {
        File dir = dir1().resolve("a").createDir();
        File link1 = dir1().resolve("b").createLink(dir);
        File link2 = dir1().resolve("c").createLink(link1);
        assertEquals("inode/directory", detector().detect(link2));
    }

    public void test_detects_broken_links_no_stack_overflow() throws Exception {
        File file = dir1().root().resolve("/proc/self/fdinfo/0");
        try {
            detector().detect(file);
        } catch (IOException ignored) {
        }
        // Pass without StackOverflowError
        // Had a bug before where files in fdinfo crash the stack
    }

}
