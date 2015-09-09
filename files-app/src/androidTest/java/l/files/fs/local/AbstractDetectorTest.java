package l.files.fs.local;

import java.io.IOException;
import java.io.Writer;

import l.files.fs.File;

import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class AbstractDetectorTest extends FileBaseTest {

    /**
     * The detector to be tested, using the given file system.
     */
    abstract AbstractDetector detector();

    public void test_detects_directory_type() throws Exception {
        File dir = dir1().resolve("a").createDirectory();
        assertEquals("inode/directory", detector().detect(dir));
    }

    public void test_detects_file_type() throws Exception {
        File file = createTextFile("a.txt");
        assertEquals("text/plain", detector().detect(file));
    }

    public void test_detects_linked_file_type() throws Exception {
        File file = createTextFile("a.mp3");
        File link = dir1().resolve("b.txt").createLink(file);
        assertEquals("text/plain", detector().detect(link));
    }

    private File createTextFile(String name) throws IOException {
        File file = dir1().resolve(name).createFile();
        try (Writer writer = file.writer(UTF_8)) {
            writer.write("hello world");
        }
        return file;
    }

    public void test_detects_linked_directory_type() throws Exception {
        File dir = dir1().resolve("a").createDirectory();
        File link = dir1().resolve("b").createLink(dir);
        assertEquals("inode/directory", detector().detect(link));
    }

    public void test_detects_multi_linked_directory_type() throws Exception {
        File dir = dir1().resolve("a").createDirectory();
        File link1 = dir1().resolve("b").createLink(dir);
        File link2 = dir1().resolve("c").createLink(link1);
        assertEquals("inode/directory", detector().detect(link2));
    }

}
