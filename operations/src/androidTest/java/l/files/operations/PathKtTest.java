package l.files.operations;

import l.files.testing.fs.PathBaseTest;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static l.files.fs.PathKt.getNonExistentDestinationFile;
import static org.junit.Assert.assertEquals;

public final class PathKtTest extends PathBaseTest {

    private Path createFile(String name) throws IOException {
        return Files.createFile(dir1().concat(name).toJavaPath());
    }

    private Path createDir(String name) throws IOException {
        return Files.createDirectory(dir1().concat(name).toJavaPath());
    }

    @Test
    public void getNonExistentDestinationFile_largeNumberSuffix()
        throws Exception {
        String tooBig = Long.MAX_VALUE + "" + Long.MAX_VALUE;
        testExistent(createFile("a " + tooBig), "a " + tooBig + " 2");
    }

    @Test
    public void getNonExistentDestinationFile_numberOverflow()
        throws Exception {
        String tooBig = String.valueOf(Long.MAX_VALUE);
        testExistent(createFile("a " + tooBig), "a " + tooBig + " 2");
    }

    @Test
    public void getNonExistentDestinationFile_file() throws Exception {
        testExistent(createFile("a"), "a 2");
        testExistent(createFile("b.txt"), "b 2.txt");
        testExistent(createFile("c 2.mp4"), "c 3.mp4");
        testExistent(createFile("c2.mp4"), "c3.mp4");
        testExistent(createFile("d 2"), "d 3");
        testExistent(createFile("dir"), "dir 2");
    }

    @Test
    public void getNonExistentDestinationFile_directory() throws Exception {
        testExistent(createDir("a"), "a 2");
        testExistent(createDir("b.txt"), "b.txt 2");
        testExistent(createDir("c 2.png"), "c 2.png 2");
        testExistent(createDir("a2"), "a3");
        testExistent(createDir("a 3"), "a 4");
        testExistent(createDir("a3"), "a4");
        testExistent(createDir("d 2"), "d 3");
        testExistent(createDir("d2"), "d3");
        testExistent(createDir("dir"), "dir 2");
    }

    @Test
    public void getNonExistentDestinationFile_hiddenResourceNoExtension()
        throws Exception {
        testExistent(createDir(".a"), ".a 2");
        testExistent(createFile(".b"), ".b 2");
    }

    private void testExistent(Path file, String expectedName) {
        Path expected = dir1().toJavaPath().resolve(expectedName);
        Path actual = getNonExistentDestinationFile(file, dir1().toJavaPath());
        assertEquals(expected, actual);
    }

}
