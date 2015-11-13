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
import static org.mockito.Mockito.mock;

public abstract class AbstractDetectorTest {

    /**
     * The detector to be tested, using the given file system.
     */
    abstract AbstractDetector detector();

    @Test
    public void detects_directory_type() throws Exception {

        File dir = createDir("a", "");
        assertEquals("inode/directory", detector().detect(dir));
    }

    @Test
    public void detects_file_type() throws Exception {

        File file = createTextFile("a", "txt");
        assertEquals("text/plain", detector().detect(file));
    }

    @Test
    public void detects_file_type_uppercase_extension() throws Exception {

        File file = createTextFile("a", "TXT");
        assertEquals("text/plain", detector().detect(file));
    }

    @Test
    public void detects_linked_file_type() throws Exception {

        File file = createTextFile("a", "mp3");
        File link = createLink("b", "txt", file);
        assertEquals("text/plain", detector().detect(link));
    }

    @Test
    public void detects_linked_directory_type() throws Exception {

        File dir = createDir("a", "");
        File link = createLink("b", "", dir);
        assertEquals("inode/directory", detector().detect(link));
    }

    @Test
    public void detects_multi_linked_directory_type() throws Exception {

        File dir = createDir("a", "");
        File link1 = createLink("b", "", dir);
        File link2 = createLink("c", "", link1);
        assertEquals("inode/directory", detector().detect(link2));
    }

    @Test(timeout = 1000)
    public void detects_broken_circular_links() throws Exception {

        Stat stat = mock(Stat.class);
        File link1 = mock(File.class);
        File link2 = mock(File.class);

        given(stat.isSymbolicLink()).willReturn(true);
        given(link1.stat(any(LinkOption.class))).willReturn(stat);
        given(link2.stat(any(LinkOption.class))).willReturn(stat);
        given(link1.readLink()).willReturn(link2);
        given(link2.readLink()).willReturn(link1);

        detector().detect(link1);
    }

    protected File createDir(String base, String ext) throws IOException {
        Stat stat = mock(Stat.class);
        File dir = mock(File.class);
        Name name = mockName(base, ext);
        given(stat.isDirectory()).willReturn(true);
        given(dir.name()).willReturn(name);
        given(dir.stat(any(LinkOption.class))).willReturn(stat);
        return dir;
    }

    protected File createLink(String base, String ext, final File target) throws IOException {
        File link = mock(File.class);
        Stat linkStat = mock(Stat.class);
        Stat targetStat = target.stat(NOFOLLOW);
        Name name = mockName(base, ext);
        given(linkStat.isSymbolicLink()).willReturn(true);
        given(link.stat(NOFOLLOW)).willReturn(linkStat);
        given(link.stat(FOLLOW)).willReturn(targetStat);
        given(link.name()).willReturn(name);
        given(link.readLink()).willReturn(target);
        given(link.newInputStream()).will(new Answer<InputStream>() {
            @Override
            public InputStream answer(InvocationOnMock invocation) throws Throwable {
                return target.newInputStream();
            }
        });
        return link;
    }

    protected File createTextFile(String base, String ext) throws IOException {
        return createTextFile(base, ext, "hello world");
    }

    protected File createTextFile(String base, String ext, String content) throws IOException {
        Stat stat = mock(Stat.class);
        File file = mock(File.class);
        Name name = mockName(base, ext);
        given(stat.isRegularFile()).willReturn(true);
        given(file.stat(any(LinkOption.class))).willReturn(stat);
        given(file.name()).willReturn(name);
        given(file.newInputStream()).willReturn(
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
