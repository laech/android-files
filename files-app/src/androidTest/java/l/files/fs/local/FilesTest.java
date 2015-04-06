package l.files.fs.local;

import java.io.File;

import l.files.common.testing.FileBaseTest;

import static l.files.fs.local.Files.getNonExistentDestinationFile;

public final class FilesTest extends FileBaseTest {

    public void testGetNonExistentDestinationFile_file() {
        testExistent(tmp().createFile("a"), "a 2");
        testExistent(tmp().createFile("b.txt"), "b 2.txt");
        testExistent(tmp().createFile("c 2.mp4"), "c 3.mp4");
        testExistent(tmp().createFile(".mp4"), "2.mp4");
        testExistent(tmp().createFile("d 2"), "d 3");
        testExistent(tmp().createFile("dir/x"), "x");
    }

    public void testGetNonExistentDestinationFile_directory() {
        testExistent(tmp().createDir("a"), "a 2");
        testExistent(tmp().createDir("b.txt"), "b.txt 2");
        testExistent(tmp().createDir("c 2.mp4"), "c 2.mp4 2");
        testExistent(tmp().createDir(".mp4"), ".mp4 2");
        testExistent(tmp().createDir("a2"), "a2 2");
        testExistent(tmp().createDir("a 3"), "a 4");
        testExistent(tmp().createDir("d 2"), "d 3");
        testExistent(tmp().createDir("dir/x"), "x");
    }

    private void testExistent(File file, String expectedName) {
        File expected = new File(tmp().get(), expectedName);
        File actual = getNonExistentDestinationFile(file, tmp().get());
        assertEquals(expected, actual);
    }

}
