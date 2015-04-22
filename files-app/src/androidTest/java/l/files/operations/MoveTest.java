package l.files.operations;

import java.io.Writer;

import l.files.fs.Resource;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singleton;

public final class MoveTest extends PasteTest {

    public void testMovedCountInitialZero() throws Exception {
        Resource src = dir1().resolve("a").createFile();
        Resource dstDir = dir1().resolve("b").createDirectory();
        Move move = create(src, dstDir);
        assertEquals(move.getMovedItemCount(), 0);
    }

    public void testMovesSymlink() throws Exception {
        Resource target = dir1().resolve("target").createFile();
        Resource link = dir1().resolve("link").createSymbolicLink(target);

        Move move = create(link, dir1().resolve("moved").createDirectory());
        move.execute();

        Resource actual = dir1().resolve("moved/link").readSymbolicLink();
        assertEquals(target, actual);
        assertEquals(1, move.getMovedItemCount());
    }

    public void testMovesFile() throws Exception {
        Resource srcFile = dir1().resolve("a.txt").createFile();
        Resource dstDir = dir1().resolve("dst").createDirectory();
        Resource dstFile = dstDir.resolve("a.txt");
        try (Writer out = srcFile.openWriter(UTF_8)) {
            out.write("Test");
        }
        Move move = create(srcFile, dstDir);
        move.execute();

        assertFalse(srcFile.exists());
        assertEquals("Test", dstFile.readString(UTF_8));
        assertEquals(move.getMovedItemCount(), 1);
    }

    public void testMovesDirectory() throws Exception {
        Resource srcDir = dir1().resolve("a").createDirectory();
        Resource dstDir = dir1().resolve("dst").createDirectory();
        Resource srcFile = srcDir.resolve("test.txt");
        Resource dstFile = dstDir.resolve("a/test.txt");
        try (Writer out = srcFile.openWriter(UTF_8)) {
            out.write("Test");
        }

        Move move = create(srcDir, dstDir);
        move.execute();

        assertFalse(srcDir.exists());
        assertEquals("Test", dstFile.readString(UTF_8));
        assertEquals(move.getMovedItemCount(), 1);
    }

    @Override
    protected Move create(Iterable<Resource> sources, Resource dstDir) {
        return new Move(sources, dstDir);
    }

    private Move create(Resource src, Resource dstDir) {
        return create(singleton(src), dstDir);
    }

}
