package l.files.operations;

import org.junit.Test;

import java.util.Set;

import l.files.fs.Path;
import l.files.testing.fs.ExtendedPath;

import static java.util.Collections.singleton;
import static l.files.fs.LinkOption.NOFOLLOW;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public final class MoveTest extends PasteTest {

    @Test
    public void movedCountInitialZero() throws Exception {
        Path src = dir1().concat("a").createFile();
        Path dstDir = dir1().concat("b").createDir();
        Move move = create(src, dstDir);
        assertEquals(move.getMovedItemCount(), 0);
    }

    @Test
    public void movesSymlink() throws Exception {
        Path target = dir1().concat("target").createFile();
        Path link = dir1().concat("link").createSymbolicLink(target);

        Move move = create(link, dir1().concat("moved").createDir());
        move.execute();

        Path actual = dir1().concat("moved/link").readSymbolicLink();
        assertEquals(target, actual);
        assertEquals(1, move.getMovedItemCount());
    }

    @Test
    public void movesFile() throws Exception {
        ExtendedPath srcFile = dir1().concat("a.txt").createFile();
        ExtendedPath dstDir = dir1().concat("dst").createDir();
        ExtendedPath dstFile = dstDir.concat("a.txt");
        srcFile.writeUtf8("Test");

        Move move = create(srcFile, dstDir);
        move.execute();

        assertFalse(srcFile.exists(NOFOLLOW));
        assertEquals("Test", dstFile.readAllUtf8());
        assertEquals(move.getMovedItemCount(), 1);
    }

    @Test
    public void movesDirectory() throws Exception {
        ExtendedPath srcDir = dir1().concat("a").createDir();
        ExtendedPath dstDir = dir1().concat("dst").createDir();
        ExtendedPath srcFile = srcDir.concat("test.txt");
        ExtendedPath dstFile = dstDir.concat("a/test.txt");
        srcFile.writeUtf8("Test");

        Move move = create(srcDir, dstDir);
        move.execute();

        assertFalse(srcDir.exists(NOFOLLOW));
        assertEquals("Test", dstFile.readAllUtf8());
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
