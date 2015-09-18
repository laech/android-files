package l.files.operations;

import java.io.Writer;
import java.util.Collection;

import l.files.fs.File;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singleton;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class MoveTest extends PasteTest {

    public void testMovedCountInitialZero() throws Exception {
        File src = dir1().resolve("a").createFile();
        File dstDir = dir1().resolve("b").createDir();
        Move move = create(src, dstDir);
        assertEquals(move.getMovedItemCount(), 0);
    }

    public void testMovesSymlink() throws Exception {
        File target = dir1().resolve("target").createFile();
        File link = dir1().resolve("link").createLink(target);

        Move move = create(link, dir1().resolve("moved").createDir());
        move.execute();

        File actual = dir1().resolve("moved/link").readLink();
        assertEquals(target, actual);
        assertEquals(1, move.getMovedItemCount());
    }

    public void testMovesFile() throws Exception {
        File srcFile = dir1().resolve("a.txt").createFile();
        File dstDir = dir1().resolve("dst").createDir();
        File dstFile = dstDir.resolve("a.txt");
        try (Writer out = srcFile.writer(UTF_8)) {
            out.write("Test");
        }
        Move move = create(srcFile, dstDir);
        move.execute();

        assertFalse(srcFile.exists(NOFOLLOW));
        assertEquals("Test", dstFile.readAll(UTF_8));
        assertEquals(move.getMovedItemCount(), 1);
    }

    public void testMovesDirectory() throws Exception {
        File srcDir = dir1().resolve("a").createDir();
        File dstDir = dir1().resolve("dst").createDir();
        File srcFile = srcDir.resolve("test.txt");
        File dstFile = dstDir.resolve("a/test.txt");
        try (Writer out = srcFile.writer(UTF_8)) {
            out.write("Test");
        }

        Move move = create(srcDir, dstDir);
        move.execute();

        assertFalse(srcDir.exists(NOFOLLOW));
        assertEquals("Test", dstFile.readAll(UTF_8));
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
