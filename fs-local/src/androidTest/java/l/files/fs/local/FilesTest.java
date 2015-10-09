package l.files.fs.local;

import l.files.fs.File;

import static l.files.fs.local.Files.getNonExistentDestinationFile;

public final class FilesTest extends FileBaseTest {

    public void testGetNonExistentDestinationFile_file() throws Exception {
        testExistent(dir1().resolve("a").createFile(), "a 2");
        testExistent(dir1().resolve("b.txt").createFile(), "b 2.txt");
        testExistent(dir1().resolve("c 2.mp4").createFile(), "c 3.mp4");
        testExistent(dir1().resolve("d 2").createFile(), "d 3");
        testExistent(dir1().resolve("dir/x").createFiles(), "x");
    }

    public void testGetNonExistentDestinationFile_directory() throws Exception {
        testExistent(dir1().resolve("a").createDir(), "a 2");
        testExistent(dir1().resolve("b.txt").createDir(), "b.txt 2");
        testExistent(dir1().resolve("c 2.mp4").createDir(), "c 2.mp4 2");
        testExistent(dir1().resolve("a2").createDir(), "a2 2");
        testExistent(dir1().resolve("a 3").createDir(), "a 4");
        testExistent(dir1().resolve("d 2").createDir(), "d 3");
        testExistent(dir1().resolve("dir/x").createDirs(), "x");
    }

    public void testGetNonExistentDestinationFile_hiddenResourceNoExtension() throws Exception {
        testExistent(dir1().resolve(".a").createDir(), ".a 2");
        testExistent(dir1().resolve(".b").createFile(), ".b 2");
    }

    private void testExistent(File file, String expectedName) {
        java.io.File expected = new java.io.File(dir1().resolve(expectedName).path());
        java.io.File actual = getNonExistentDestinationFile(
                new java.io.File(file.path()),
                new java.io.File(dir1().path()));
        assertEquals(expected, actual);
    }

}
