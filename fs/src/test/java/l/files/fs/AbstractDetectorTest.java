package l.files.fs;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static l.files.fs.File.UTF_8;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;

public abstract class AbstractDetectorTest {

    /**
     * The detector to be tested, using the given file system.
     */
    abstract AbstractDetector detector();

    @Test
    public void detects_directory_type() throws Exception {

        Path dir = createDir("a", "");
        assertEquals("inode/directory", detector().detect(dir));
    }

    @Test
    public void detects_file_type() throws Exception {

        Path file = createTextFile("a", "txt");
        assertEquals("text/plain", detector().detect(file));
    }

    @Test
    public void detects_file_type_uppercase_extension() throws Exception {

        Path file = createTextFile("a", "TXT");
        assertEquals("text/plain", detector().detect(file));
    }

    @Test
    public void detects_linked_file_type() throws Exception {

        Path file = createTextFile("a", "mp3");
        Path link = createLink("b", "txt", file);
        assertEquals("text/plain", detector().detect(link));
    }

    @Test
    public void detects_linked_directory_type() throws Exception {

        Path dir = createDir("a", "");
        Path link = createLink("b", "", dir);
        assertEquals("inode/directory", detector().detect(link));
    }

    @Test
    public void detects_multi_linked_directory_type() throws Exception {

        Path dir = createDir("a", "");
        Path link1 = createLink("b", "", dir);
        Path link2 = createLink("c", "", link1);
        assertEquals("inode/directory", detector().detect(link2));
    }

    @Test(timeout = 1000)
    public void detects_broken_circular_links() throws Exception {

        Stat stat = mock(Stat.class);
        Path link1 = mock(Path.class);
        Path link2 = mock(Path.class);
        FileSystem fs = mock(FileSystem.class);

        given(stat.isSymbolicLink()).willReturn(true);
        given(link1.fileSystem()).willReturn(fs);
        given(link2.fileSystem()).willReturn(fs);
        given(fs.stat(eq(link1), any(LinkOption.class))).willReturn(stat);
        given(fs.stat(eq(link2), any(LinkOption.class))).willReturn(stat);
        given(fs.readLink(link1)).willReturn(link2);
        given(fs.readLink(link2)).willReturn(link1);

        detector().detect(link1);
    }

    protected Path createDir(String base, String ext) throws IOException {
        FileSystem fs = mock(FileSystem.class);
        Stat stat = mock(Stat.class);
        Path dir = mock(Path.class);
        Name name = mockName(base, ext);
        given(stat.isDirectory()).willReturn(true);
        given(dir.fileSystem()).willReturn(fs);
        given(dir.name()).willReturn(name);
        given(fs.stat(eq(dir), any(LinkOption.class))).willReturn(stat);
        return dir;
    }

    protected Path createLink(String base, String ext, final Path target) throws IOException {
        FileSystem fs = mock(FileSystem.class);
        Path link = mock(Path.class);
        Stat linkStat = mock(Stat.class);
        Stat targetStat = Files.stat(target, NOFOLLOW);
        Name name = mockName(base, ext);
        given(linkStat.isSymbolicLink()).willReturn(true);
        given(link.fileSystem()).willReturn(fs);
        given(fs.stat(link, NOFOLLOW)).willReturn(linkStat);
        given(fs.stat(link, FOLLOW)).willReturn(targetStat);
        given(link.name()).willReturn(name);
        given(fs.readLink(link)).willReturn(target);
        given(fs.newInputStream(link)).will(new Answer<InputStream>() {
            @Override
            public InputStream answer(InvocationOnMock invocation) throws Throwable {
                return Files.newInputStream(target);
            }
        });
        return link;
    }

    protected Path createTextFile(String base, String ext) throws IOException {
        return createTextFile(base, ext, "hello world");
    }

    protected Path createTextFile(String base, String ext, String content) throws IOException {
        FileSystem fs = mock(FileSystem.class);
        Stat stat = mock(Stat.class);
        Path file = mock(Path.class);
        Name name = mockName(base, ext);
        given(stat.isRegularFile()).willReturn(true);
        given(file.name()).willReturn(name);
        given(file.fileSystem()).willReturn(fs);
        given(fs.stat(eq(file), any(LinkOption.class))).willReturn(stat);
        given(fs.newInputStream(file)).willReturn(
                new ByteArrayInputStream(content.getBytes(UTF_8))
        );
        return file;
    }

    private Name mockName(String base, String ext) {
        Name name = mock(Name.class);
        given(name.base()).willReturn(base);
        given(name.ext()).willReturn(ext);
        given(name.dotExt()).willReturn(ext.isEmpty() ? ext : "." + ext);
        given(name.toString()).willReturn(ext.isEmpty() ? base : base + "." + ext);
        return name;
    }

}
