package l.files.operations;

import org.junit.Test;

import java.nio.file.Path;
import java.util.Set;

import static java.nio.file.Files.*;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public final class MoveTest extends PasteTest {

    @Test
    public void movedCountInitialZero() throws Exception {
        Path src = createFile(dir1().resolve("a"));
        Path dstDir = createDirectory(dir1().resolve("b"));
        Move move = create(src, dstDir);
        assertEquals(move.getMovedItemCount(), 0);
    }

    @Test
    public void movesSymlink() throws Exception {
        Path target = createFile(dir1().resolve("target"));
        Path link =
            createSymbolicLink(dir1().resolve("link"), target);

        Move move =
            create(link, createDirectory(dir1().resolve("moved")));
        move.execute();

        Path actual =
            readSymbolicLink(dir1().resolve("moved/link"));
        assertEquals(target, actual);
        assertEquals(1, move.getMovedItemCount());
    }

    @Test
    public void movesFile() throws Exception {
        Path srcFile = createFile(dir1().resolve("a.txt"));
        Path dstDir = createDirectory(dir1().resolve("dst"));
        Path dstFile = dstDir.resolve("a.txt");
        write(srcFile, singleton("Test"));

        Move move = create(srcFile, dstDir);
        move.execute();

        assertFalse(exists(srcFile, NOFOLLOW_LINKS));
        assertEquals(singletonList("Test"), readAllLines(dstFile));
        assertEquals(move.getMovedItemCount(), 1);
    }


    @Test
    public void movesDirectory() throws Exception {
        Path srcDir = createDirectory(dir1().resolve("a"));
        Path dstDir = createDirectory(dir1().resolve("dst"));
        Path srcFile = srcDir.resolve("test.txt");
        Path dstFile = dstDir.resolve("a/test.txt");
        write(srcFile, singleton("Test"));

        Move move = create(srcDir, dstDir);
        move.execute();

        assertFalse(exists(srcDir, NOFOLLOW_LINKS));
        assertEquals(singletonList("Test"), readAllLines(dstFile));
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
