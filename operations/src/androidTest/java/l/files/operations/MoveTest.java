package l.files.operations;

import org.junit.Test;

import java.util.Collection;

import l.files.fs.Path;

import static java.util.Collections.singleton;
import static l.files.fs.Files.createDir;
import static l.files.fs.Files.createFile;
import static l.files.fs.Files.createSymbolicLink;
import static l.files.fs.Files.exists;
import static l.files.fs.Files.readAllUtf8;
import static l.files.fs.Files.readSymbolicLink;
import static l.files.fs.Files.writeUtf8;
import static l.files.fs.LinkOption.NOFOLLOW;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public final class MoveTest extends PasteTest {

    @Test
    public void movedCountInitialZero() throws Exception {
        Path src = createFile(dir1().resolve("a"));
        Path dstDir = createDir(dir1().resolve("b"));
        Move move = create(src, dstDir);
        assertEquals(move.getMovedItemCount(), 0);
    }

    @Test
    public void movesSymlink() throws Exception {
        Path target = createFile(dir1().resolve("target"));
        Path link = createSymbolicLink(dir1().resolve("link"), target);

        Move move = create(link, createDir(dir1().resolve("moved")));
        move.execute();

        Path actual = readSymbolicLink(dir1().resolve("moved/link"));
        assertEquals(target, actual);
        assertEquals(1, move.getMovedItemCount());
    }

    @Test
    public void movesFile() throws Exception {
        Path srcFile = createFile(dir1().resolve("a.txt"));
        Path dstDir = createDir(dir1().resolve("dst"));
        Path dstFile = dstDir.resolve("a.txt");
        writeUtf8(srcFile, "Test");

        Move move = create(srcFile, dstDir);
        move.execute();

        assertFalse(exists(srcFile, NOFOLLOW));
        assertEquals("Test", readAllUtf8(dstFile));
        assertEquals(move.getMovedItemCount(), 1);
    }

    @Test
    public void movesDirectory() throws Exception {
        Path srcDir = createDir(dir1().resolve("a"));
        Path dstDir = createDir(dir1().resolve("dst"));
        Path srcFile = srcDir.resolve("test.txt");
        Path dstFile = dstDir.resolve("a/test.txt");
        writeUtf8(srcFile, "Test");

        Move move = create(srcDir, dstDir);
        move.execute();

        assertFalse(exists(srcDir, NOFOLLOW));
        assertEquals("Test", readAllUtf8(dstFile));
        assertEquals(move.getMovedItemCount(), 1);
    }

    @Override
    Move create(Collection<Path> sources, Path dstDir) {
        return new Move(sources, dstDir);
    }

    private Move create(Path src, Path dstDir) {
        return create(singleton(src), dstDir);
    }

}
