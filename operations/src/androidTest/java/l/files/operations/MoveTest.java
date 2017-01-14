package l.files.operations;

import java.util.Map;

import l.files.fs.FileSystem;
import l.files.fs.Path;

import static java.util.Collections.singletonMap;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class MoveTest extends PasteTest {

    public void test_movedCountInitialZero() throws Exception {
        Path src = fs.createFile(dir1().concat("a"));
        Path dstDir = fs.createDir(dir1().concat("b"));
        Move move = create(src, dstDir);
        assertEquals(move.getMovedItemCount(), 0);
    }

    public void test_movesSymlink() throws Exception {
        Path target = fs.createFile(dir1().concat("target"));
        Path link = fs.createSymbolicLink(dir1().concat("link"), target);

        Move move = create(link, fs.createDir(dir1().concat("moved")));
        move.execute();

        Path actual = fs.readSymbolicLink(dir1().concat("moved/link"));
        assertEquals(target, actual);
        assertEquals(1, move.getMovedItemCount());
    }

    public void test_movesFile() throws Exception {
        Path srcFile = fs.createFile(dir1().concat("a.txt"));
        Path dstDir = fs.createDir(dir1().concat("dst"));
        Path dstFile = dstDir.concat("a.txt");
        fs.writeUtf8(srcFile, "Test");

        Move move = create(srcFile, dstDir);
        move.execute();

        assertFalse(fs.exists(srcFile, NOFOLLOW));
        assertEquals("Test", fs.readAllUtf8(dstFile));
        assertEquals(move.getMovedItemCount(), 1);
    }

    public void test_movesDirectory() throws Exception {
        Path srcDir = fs.createDir(dir1().concat("a"));
        Path dstDir = fs.createDir(dir1().concat("dst"));
        Path srcFile = srcDir.concat("test.txt");
        Path dstFile = dstDir.concat("a/test.txt");
        fs.writeUtf8(srcFile, "Test");

        Move move = create(srcDir, dstDir);
        move.execute();

        assertFalse(fs.exists(srcDir, NOFOLLOW));
        assertEquals("Test", fs.readAllUtf8(dstFile));
        assertEquals(move.getMovedItemCount(), 1);
    }

    @Override
    Move create(
            Map<? extends Path, ? extends FileSystem> sourcePaths,
            FileSystem destinationFs,
            Path destinationDir) {
        return new Move(sourcePaths, destinationFs, destinationDir);
    }

    private Move create(Path src, Path dstDir) {
        return create(singletonMap(src, fs), fs, dstDir);
    }

}
