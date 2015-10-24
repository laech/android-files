package l.files.operations;

import java.io.IOException;

import l.files.fs.File;
import l.files.testing.fs.FileBaseTest;

import static l.files.operations.Files.getNonExistentDestinationFile;

public final class FilesTest extends FileBaseTest {

    public void testGetNonExistentDestinationFile_largeNumberSuffix() throws Exception {
        String tooBig = Long.MAX_VALUE + "" + Long.MAX_VALUE;
        testExistent(dir1().resolve("a " + tooBig).createFile(), "a " + tooBig + " 2");
    }

    public void testGetNonExistentDestinationFile_numberOverflow() throws Exception {
        String tooBig = String.valueOf(Long.MAX_VALUE);
        testExistent(dir1().resolve("a " + tooBig).createFile(), "a " + tooBig + " 2");
    }

    public void testGetNonExistentDestinationFile_file() throws Exception {
        testExistent(dir1().resolve("a").createFile(), "a 2");
        testExistent(dir1().resolve("b.txt").createFile(), "b 2.txt");
        testExistent(dir1().resolve("c 2.mp4").createFile(), "c 3.mp4");
        testExistent(dir1().resolve("c2.mp4").createFile(), "c3.mp4");
        testExistent(dir1().resolve("d 2").createFile(), "d 3");
        testExistent(dir1().resolve("dir/x").createFiles(), "x");
    }

    public void testGetNonExistentDestinationFile_directory() throws Exception {
        testExistent(dir1().resolve("a").createDir(), "a 2");
        testExistent(dir1().resolve("b.txt").createDir(), "b.txt 2");
        testExistent(dir1().resolve("c 2.png").createDir(), "c 2.png 2");
        testExistent(dir1().resolve("a2").createDir(), "a3");
        testExistent(dir1().resolve("a 3").createDir(), "a 4");
        testExistent(dir1().resolve("a3").createDir(), "a4");
        testExistent(dir1().resolve("d 2").createDir(), "d 3");
        testExistent(dir1().resolve("d2").createDir(), "d3");
        testExistent(dir1().resolve("dir/x").createDirs(), "x");
    }

    public void testGetNonExistentDestinationFile_hiddenResourceNoExtension() throws Exception {
        testExistent(dir1().resolve(".a").createDir(), ".a 2");
        testExistent(dir1().resolve(".b").createFile(), ".b 2");
    }

    private void testExistent(File file, String expectedName) throws IOException {
        File expected = dir1().resolve(expectedName);
        File actual = getNonExistentDestinationFile(file, dir1());
        assertEquals(expected, actual);
    }

}
