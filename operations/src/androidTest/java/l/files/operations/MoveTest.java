package l.files.operations;

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

public final class MoveTest extends PasteTest {

    public void test_movedCountInitialZero() throws Exception {
        Path src = createFile(dir1().concat("a"));
        Path dstDir = createDir(dir1().concat("b"));
        Move move = create(src, dstDir);
        assertEquals(move.getMovedItemCount(), 0);
    }

    public void test_movesSymlink() throws Exception {
        Path target = createFile(dir1().concat("target"));
        Path link = createSymbolicLink(dir1().concat("link"), target);

        Move move = create(link, createDir(dir1().concat("moved")));
        move.execute();

        Path actual = readSymbolicLink(dir1().concat("moved/link"));
        assertEquals(target, actual);
        assertEquals(1, move.getMovedItemCount());
    }

    public void test_movesFile() throws Exception {
        Path srcFile = createFile(dir1().concat("a.txt"));
        Path dstDir = createDir(dir1().concat("dst"));
        Path dstFile = dstDir.concat("a.txt");
        writeUtf8(srcFile, "Test");

        Move move = create(srcFile, dstDir);
        move.execute();

        assertFalse(exists(srcFile, NOFOLLOW));
        assertEquals("Test", readAllUtf8(dstFile));
        assertEquals(move.getMovedItemCount(), 1);
    }

    public void test_movesDirectory() throws Exception {
        Path srcDir = createDir(dir1().concat("a"));
        Path dstDir = createDir(dir1().concat("dst"));
        Path srcFile = srcDir.concat("test.txt");
        Path dstFile = dstDir.concat("a/test.txt");
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
