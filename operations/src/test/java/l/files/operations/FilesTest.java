package l.files.operations;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import l.files.fs.Name;
import l.files.fs.FileSystem;
import l.files.fs.Path;
import l.files.fs.Stat;

import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.operations.Files.getNonExistentDestinationFile;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;

public final class FilesTest {

    private Path dir;

    @Before
    public void setUp() throws Exception {
        dir = mock(Path.class);

        Path file = mock(Path.class);
        FileSystem fs = mock(FileSystem.class);
        given(dir.concat(anyString())).willReturn(file);
        given(dir.fileSystem()).willReturn(fs);
        given(file.fileSystem()).willReturn(fs);
    }

    private Path mockFile(String nameStr, boolean isDir) throws IOException {
        Name name = Name.fromString(nameStr);
        FileSystem fs = mock(FileSystem.class);
        Stat stat = mock(Stat.class);
        Path file = mock(Path.class);
        given(file.fileSystem()).willReturn(fs);
        given(stat.isRegularFile()).willReturn(!isDir);
        given(stat.isDirectory()).willReturn(isDir);
        given(file.name()).willReturn(name);
        given(fs.stat(file, FOLLOW)).willReturn(stat);
        given(fs.exists(file, NOFOLLOW)).willReturn(true);
        given(dir.concat(name.toPath())).willReturn(file);
        given(dir.concat(file.name().toPath())).willReturn(file);
        return file;
    }

    private Path mockFile(String name) throws IOException {
        return mockFile(name, false);
    }

    private Path mockDir(String name) throws IOException {
        return mockFile(name, true);
    }

    @Test
    public void getNonExistentDestinationFile_largeNumberSuffix() throws Exception {
        String tooBig = Long.MAX_VALUE + "" + Long.MAX_VALUE;
        testExistent(mockFile("a " + tooBig), "a " + tooBig + " 2");
    }

    @Test
    public void getNonExistentDestinationFile_numberOverflow() throws Exception {
        String tooBig = String.valueOf(Long.MAX_VALUE);
        testExistent(mockFile("a " + tooBig), "a " + tooBig + " 2");
    }

    @Test
    public void getNonExistentDestinationFile_file() throws Exception {
        testExistent(mockFile("a"), "a 2");
        testExistent(mockFile("b.txt"), "b 2.txt");
        testExistent(mockFile("c 2.mp4"), "c 3.mp4");
        testExistent(mockFile("c2.mp4"), "c3.mp4");
        testExistent(mockFile("d 2"), "d 3");
        testExistent(mockFile("dir"), "x");
    }

    @Test
    public void getNonExistentDestinationFile_directory() throws Exception {
        testExistent(mockDir("a"), "a 2");
        testExistent(mockDir("b.txt"), "b.txt 2");
        testExistent(mockDir("c 2.png"), "c 2.png 2");
        testExistent(mockDir("a2"), "a3");
        testExistent(mockDir("a 3"), "a 4");
        testExistent(mockDir("a3"), "a4");
        testExistent(mockDir("d 2"), "d 3");
        testExistent(mockDir("d2"), "d3");
        testExistent(mockDir("dir"), "x");
    }

    @Test
    public void getNonExistentDestinationFile_hiddenResourceNoExtension() throws Exception {
        testExistent(mockDir(".a"), ".a 2");
        testExistent(mockFile(".b"), ".b 2");
    }

    private void testExistent(Path file, String expectedName) throws IOException {
        Path expected = dir.concat(expectedName);
        Path actual = getNonExistentDestinationFile(file, dir);
        assertEquals(expected, actual);
    }

}
