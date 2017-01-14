package l.files.fs.media;

import java.io.IOException;

import l.files.fs.Path;
import l.files.fs.local.LocalFileSystem;
import l.files.testing.fs.PathBaseTest;

public final class DetectorTest extends PathBaseTest {

    public DetectorTest() {
        super(LocalFileSystem.INSTANCE);
    }

    private Detector detector() {
        return Detector.INSTANCE;
    }

    public void test_can_detect_by_name() throws Exception {
        Path file = createTextFile("a.txt", "");
        assertEquals("text/plain", detector().detect(getContext(), fs, file));
    }

    public void test_can_detect_by_content() throws Exception {
        Path file = createTextFile("a.png");
        assertEquals("text/plain", detector().detect(getContext(), fs, file));
    }

    public void test_detects_directory_type() throws Exception {
        Path dir = createDir("a");
        assertEquals("inode/directory", detector().detect(getContext(), fs, dir));
    }

    public void test_detects_file_type() throws Exception {
        Path file = createTextFile("a.txt");
        assertEquals("text/plain", detector().detect(getContext(), fs, file));
    }

    public void test_detects_file_type_uppercase_extension() throws Exception {
        Path file = createTextFile("a.TXT");
        assertEquals("text/plain", detector().detect(getContext(), fs, file));
    }

    public void test_detects_linked_file_type() throws Exception {
        Path file = createTextFile("a.mp3");
        Path link = createSymbolicLink("b.txt", file);
        assertEquals("text/plain", detector().detect(getContext(), fs, link));
    }

    public void test_detects_linked_directory_type() throws Exception {
        Path dir = createDir("a");
        Path link = createSymbolicLink("b", dir);
        assertEquals("inode/directory", detector().detect(getContext(), fs, link));
    }

    public void test_detects_multi_linked_directory_type() throws Exception {
        Path dir = createDir("a");
        Path link1 = createSymbolicLink("b", dir);
        Path link2 = createSymbolicLink("c", link1);
        assertEquals("inode/directory", detector().detect(getContext(), fs, link2));
    }

    public void test_fails_on_broken_circular_links() throws Exception {
        Path link1 = dir1().concat("link1");
        Path link2 = dir1().concat("link2");
        fs.createSymbolicLink(link1, link2);
        fs.createSymbolicLink(link2, link1);
        try {
            detector().detect(getContext(), fs, link1);
            fail();
        } catch (IOException e) {
            // Pass
        }
    }

    protected Path createDir(String name) throws IOException {
        return fs.createDir(dir1().concat(name));
    }

    protected Path createSymbolicLink(String name, Path target) throws IOException {
        Path link = dir1().concat(name);
        fs.createSymbolicLink(link, target);
        return link;
    }

    protected Path createTextFile(String name) throws IOException {
        return createTextFile(name, "hello world");
    }

    protected Path createTextFile(String name, String content) throws IOException {
        Path path = dir1().concat(name);
        fs.writeUtf8(path, content);
        return path;
    }

}
