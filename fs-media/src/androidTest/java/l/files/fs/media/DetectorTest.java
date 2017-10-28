package l.files.fs.media;

import org.junit.Test;

import java.io.IOException;

import l.files.fs.Path;
import l.files.testing.fs.PathBaseTest;
import l.files.testing.fs.Paths;

import static android.support.test.InstrumentationRegistry.getContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public final class DetectorTest extends PathBaseTest {

    private Detector detector() {
        return Detector.INSTANCE;
    }

    @Test
    public void can_detect_by_name() throws Exception {
        Path file = createTextFile("a.txt", "");
        assertEquals("text/plain", detector().detect(getContext(), file));
    }

    @Test
    public void can_detect_by_content() throws Exception {
        Path file = createTextFile("a.png");
        assertEquals("text/plain", detector().detect(getContext(), file));
    }

    @Test
    public void detects_directory_type() throws Exception {
        Path dir = createDir("a");
        assertEquals("inode/directory", detector().detect(getContext(), dir));
    }

    @Test
    public void detects_file_type() throws Exception {
        Path file = createTextFile("a.txt");
        assertEquals("text/plain", detector().detect(getContext(), file));
    }

    @Test
    public void detects_file_type_uppercase_extension() throws Exception {
        Path file = createTextFile("a.TXT");
        assertEquals("text/plain", detector().detect(getContext(), file));
    }

    @Test
    public void detects_linked_file_type() throws Exception {
        Path file = createTextFile("a.mp3");
        Path link = createSymbolicLink("b.txt", file);
        assertEquals("text/plain", detector().detect(getContext(), link));
    }

    @Test
    public void detects_linked_directory_type() throws Exception {
        Path dir = createDir("a");
        Path link = createSymbolicLink("b", dir);
        assertEquals("inode/directory", detector().detect(getContext(), link));
    }

    @Test
    public void detects_multi_linked_directory_type() throws Exception {
        Path dir = createDir("a");
        Path link1 = createSymbolicLink("b", dir);
        Path link2 = createSymbolicLink("c", link1);
        assertEquals("inode/directory", detector().detect(getContext(), link2));
    }

    @Test
    public void fails_on_broken_circular_links() throws Exception {
        Path link1 = dir1().concat("link1");
        Path link2 = dir1().concat("link2");
        link1.createSymbolicLink(link2);
        link2.createSymbolicLink(link1);
        try {
            detector().detect(getContext(), link1);
            fail();
        } catch (IOException e) {
            // Pass
        }
    }

    private Path createDir(String name) throws IOException {
        return dir1().concat(name).createDirectory();
    }

    private Path createSymbolicLink(String name, Path target) throws IOException {
        Path link = dir1().concat(name);
        link.createSymbolicLink(target);
        return link;
    }

    private Path createTextFile(String name) throws IOException {
        return createTextFile(name, "hello world");
    }

    private Path createTextFile(String name, String content) throws IOException {
        Path path = dir1().concat(name);
        Paths.writeUtf8(path, content);
        return path;
    }

}
