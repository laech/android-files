package l.files.operations;

import java.util.Set;

import l.files.fs.Path;
import l.files.testing.fs.Paths;

import static java.util.Collections.singleton;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class MoveTest extends PasteTest {

    public void test_movedCountInitialZero() throws Exception {
        Path src = dir1().concat("a").createFile();
        Path dstDir = dir1().concat("b").createDirectory();
        Move move = create(src, dstDir);
        assertEquals(move.getMovedItemCount(), 0);
    }

    public void test_movesSymlink() throws Exception {
        Path target = dir1().concat("target").createFile();
        Path link = dir1().concat("link").createSymbolicLink(target);

        Move move = create(link, dir1().concat("moved").createDirectory());
        move.execute();

        Path actual = dir1().concat("moved/link").readSymbolicLink();
        assertEquals(target, actual);
        assertEquals(1, move.getMovedItemCount());
    }

    public void test_movesFile() throws Exception {
        Path srcFile = dir1().concat("a.txt").createFile();
        Path dstDir = dir1().concat("dst").createDirectory();
        Path dstFile = dstDir.concat("a.txt");
        Paths.writeUtf8(srcFile, "Test");

        Move move = create(srcFile, dstDir);
        move.execute();

        assertFalse(srcFile.exists(NOFOLLOW));
        assertEquals("Test", Paths.readAllUtf8(dstFile));
        assertEquals(move.getMovedItemCount(), 1);
    }

    public void test_movesDirectory() throws Exception {
        Path srcDir = dir1().concat("a").createDirectory();
        Path dstDir = dir1().concat("dst").createDirectory();
        Path srcFile = srcDir.concat("test.txt");
        Path dstFile = dstDir.concat("a/test.txt");
        Paths.writeUtf8(srcFile, "Test");

        Move move = create(srcDir, dstDir);
        move.execute();

        assertFalse(srcDir.exists(NOFOLLOW));
        assertEquals("Test", Paths.readAllUtf8(dstFile));
        assertEquals(move.getMovedItemCount(), 1);
    }

    @Override
    Move create(Set<? extends Path> sourcePaths, Path destinationDir) {
        return new Move(sourcePaths, destinationDir);
    }

    private Move create(Path src, Path dstDir) {
        return create(singleton(src), dstDir);
    }

}
