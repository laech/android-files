package l.files.fs.media;

import java.io.IOException;

import l.files.fs.Files;
import l.files.fs.Path;
import l.files.testing.fs.PathBaseTest;

public abstract class BasePropertyDetectorTest extends PathBaseTest {

    /**
     * The detector to be tested, using the given file system.
     */
    abstract BasePropertyDetector detector();

    public void test_detects_directory_type() throws Exception {
        Path dir = createDir("a");
        assertEquals("inode/directory", detector().detect(null, dir));
    }

    public void test_detects_file_type() throws Exception {
        Path file = createTextFile("a.txt");
        assertEquals("text/plain", detector().detect(null, file));
    }

    public void test_detects_file_type_uppercase_extension() throws Exception {
        Path file = createTextFile("a.TXT");
        assertEquals("text/plain", detector().detect(null, file));
    }

    public void test_detects_linked_file_type() throws Exception {
        Path file = createTextFile("a.mp3");
        Path link = createSymbolicLink("b.txt", file);
        assertEquals("text/plain", detector().detect(null, link));
    }

    public void test_detects_linked_directory_type() throws Exception {
        Path dir = createDir("a");
        Path link = createSymbolicLink("b", dir);
        assertEquals("inode/directory", detector().detect(null, link));
    }

    public void test_detects_multi_linked_directory_type() throws Exception {
        Path dir = createDir("a");
        Path link1 = createSymbolicLink("b", dir);
        Path link2 = createSymbolicLink("c", link1);
        assertEquals("inode/directory", detector().detect(null, link2));
    }

    public void test_fails_on_broken_circular_links() throws Exception {
        Path link1 = dir1().resolve("link1");
        Path link2 = dir1().resolve("link2");
        Files.createSymbolicLink(link1, link2);
        Files.createSymbolicLink(link2, link1);
        try {
            detector().detect(null, link1);
            fail();
        } catch (IOException e) {
            // Pass
        }
    }

    protected Path createDir(String name) throws IOException {
        return Files.createDir(dir1().resolve(name));
    }

    protected Path createSymbolicLink(String name, Path target) throws IOException {
        Path link = dir1().resolve(name);
        Files.createSymbolicLink(link, target);
        return link;
    }

    protected Path createTextFile(String name) throws IOException {
        return createTextFile(name, "hello world");
    }

    protected Path createTextFile(String name, String content) throws IOException {
        Path path = dir1().resolve(name);
        Files.writeUtf8(path, content);
        return path;
    }

}
