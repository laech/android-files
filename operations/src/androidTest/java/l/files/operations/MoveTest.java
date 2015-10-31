package l.files.operations;

import org.junit.Test;

import java.util.Collection;

import l.files.fs.File;

import static java.util.Collections.singleton;
import static l.files.fs.LinkOption.NOFOLLOW;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public final class MoveTest extends PasteTest {

    @Test
    public void movedCountInitialZero() throws Exception {
        File src = dir1().resolve("a").createFile();
        File dstDir = dir1().resolve("b").createDir();
        Move move = create(src, dstDir);
        assertEquals(move.getMovedItemCount(), 0);
    }

    @Test
    public void movesSymlink() throws Exception {
        File target = dir1().resolve("target").createFile();
        File link = dir1().resolve("link").createLink(target);

        Move move = create(link, dir1().resolve("moved").createDir());
        move.execute();

        File actual = dir1().resolve("moved/link").readLink();
        assertEquals(target, actual);
        assertEquals(1, move.getMovedItemCount());
    }

    @Test
    public void movesFile() throws Exception {
        File srcFile = dir1().resolve("a.txt").createFile();
        File dstDir = dir1().resolve("dst").createDir();
        File dstFile = dstDir.resolve("a.txt");
        srcFile.writeAllUtf8("Test");

        Move move = create(srcFile, dstDir);
        move.execute();

        assertFalse(srcFile.exists(NOFOLLOW));
        assertEquals("Test", dstFile.readAllUtf8());
        assertEquals(move.getMovedItemCount(), 1);
    }

    @Test
    public void movesDirectory() throws Exception {
        File srcDir = dir1().resolve("a").createDir();
        File dstDir = dir1().resolve("dst").createDir();
        File srcFile = srcDir.resolve("test.txt");
        File dstFile = dstDir.resolve("a/test.txt");
        srcFile.writeAllUtf8("Test");

        Move move = create(srcDir, dstDir);
        move.execute();

        assertFalse(srcDir.exists(NOFOLLOW));
        assertEquals("Test", dstFile.readAllUtf8());
        assertEquals(move.getMovedItemCount(), 1);
    }

    @Override
    Move create(Collection<File> sources, File dstDir) {
        return new Move(sources, dstDir);
    }

    private Move create(File src, File dstDir) {
        return create(singleton(src), dstDir);
    }

}
