package l.files.operations;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import l.files.fs.File;
import l.files.fs.Name;
import l.files.fs.Stat;

import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.operations.Files.getNonExistentDestinationFile;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;

public final class FilesTest {

    private File dir;

    @Before
    public void setUp() throws Exception {
        dir = mock(File.class);

        File file = mock(File.class);
        given(dir.resolve(anyString())).willReturn(file);
    }

    private Name mockName(String base, String ext) {
        Name name = mock(Name.class);
        given(name.base()).willReturn(base);
        given(name.ext()).willReturn(ext);
        given(name.dotExt()).willReturn(ext.isEmpty() ? ext : "." + ext);
        given(name.toString()).willReturn(ext.isEmpty() ? base : base + "." + ext);
        return name;
    }


    private File mockFile(String base, String ext, boolean isDir) throws IOException {
        Name name = mockName(base, ext);
        Stat stat = mock(Stat.class);
        File file = mock(File.class);
        given(stat.isRegularFile()).willReturn(!isDir);
        given(stat.isDirectory()).willReturn(isDir);
        given(file.stat(FOLLOW)).willReturn(stat);
        given(file.name()).willReturn(name);
        given(file.exists(NOFOLLOW)).willReturn(true);
        given(dir.resolve(name)).willReturn(file);
        given(dir.resolve(file.name())).willReturn(file);
        return file;
    }

    private File mockFile(String base, String ext) throws IOException {
        return mockFile(base, ext, false);
    }

    private File mockDir(String base, String ext) throws IOException {
        return mockFile(base, ext, true);
    }

    @Test
    public void getNonExistentDestinationFile_largeNumberSuffix() throws Exception {
        String tooBig = Long.MAX_VALUE + "" + Long.MAX_VALUE;
        testExistent(mockFile("a " + tooBig, ""), "a " + tooBig + " 2");
    }

    @Test
    public void getNonExistentDestinationFile_numberOverflow() throws Exception {
        String tooBig = String.valueOf(Long.MAX_VALUE);
        testExistent(mockFile("a " + tooBig, ""), "a " + tooBig + " 2");
    }

    @Test
    public void getNonExistentDestinationFile_file() throws Exception {
        testExistent(mockFile("a", ""), "a 2");
        testExistent(mockFile("b", "txt"), "b 2.txt");
        testExistent(mockFile("c 2", "mp4"), "c 3.mp4");
        testExistent(mockFile("c2", "mp4"), "c3.mp4");
        testExistent(mockFile("d 2", ""), "d 3");
        testExistent(mockFile("dir/x", ""), "x");
    }

    @Test
    public void getNonExistentDestinationFile_directory() throws Exception {
        testExistent(mockDir("a", ""), "a 2");
        testExistent(mockDir("b", "txt"), "b.txt 2");
        testExistent(mockDir("c 2", "png"), "c 2.png 2");
        testExistent(mockDir("a2", ""), "a3");
        testExistent(mockDir("a 3", ""), "a 4");
        testExistent(mockDir("a3", ""), "a4");
        testExistent(mockDir("d 2", ""), "d 3");
        testExistent(mockDir("d2", ""), "d3");
        testExistent(mockDir("dir/x", ""), "x");
    }

    @Test
    public void getNonExistentDestinationFile_hiddenResourceNoExtension() throws Exception {
        testExistent(mockDir("", "a"), ".a 2");
        testExistent(mockFile("", "b"), ".b 2");
    }

    private void testExistent(File file, String expectedName) throws IOException {
        File expected = dir.resolve(expectedName);
        File actual = getNonExistentDestinationFile(file, dir);
        assertEquals(expected, actual);
    }

}
