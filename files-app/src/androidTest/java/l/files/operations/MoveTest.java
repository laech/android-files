package l.files.operations;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;

import java.io.File;

import l.files.fs.Path;
import l.files.fs.Resource;
import l.files.fs.local.LocalPath;

import static com.google.common.io.Files.write;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;

public final class MoveTest extends PasteTest {

    public void testMovedCountInitialZero() {
        Move move = create(tmp().createFile("a"), tmp().createDir("b"));
        assertEquals(move.getMovedItemCount(), 0);
    }

    public void testMovesSymlink() throws Exception {
        Resource target = LocalPath.of(tmp().createFile("target")).getResource();
        Resource link = LocalPath.of(tmp().get("link")).getResource();
        link.createSymbolicLink(target);

        Move move = create(new File(link.getUri()), tmp().createDir("moved"));
        move.execute();

        Resource actual = LocalPath.of(tmp().get("moved/link")).getResource().readSymbolicLink();
        assertEquals(target, actual);
        assertEquals(1, move.getMovedItemCount());
    }

    public void testMovesFile() throws Exception {
        File srcFile = tmp().createFile("a.txt");
        File dstDir = tmp().createDir("dst");
        File dstFile = new File(dstDir, "a.txt");
        write("Test", srcFile, UTF_8);

        Move move = create(srcFile, dstDir);
        move.execute();

        assertFalse(srcFile.exists());
        assertEquals(Files.toString(dstFile, UTF_8), "Test");
        assertEquals(move.getMovedItemCount(), 1);
    }

    public void testMovesDirectory() throws Exception {
        File srcDir = tmp().createDir("a");
        File dstDir = tmp().createDir("dst");
        File srcFile = new File(srcDir, "test.txt");
        File dstFile = new File(dstDir, "a/test.txt");
        write("Test", srcFile, UTF_8);

        Move move = create(srcDir, dstDir);
        move.execute();

        assertFalse(srcDir.exists());
        assertEquals(Files.toString(dstFile, UTF_8), "Test");
        assertEquals(move.getMovedItemCount(), 1);
    }

    @Override
    protected Move create(Iterable<String> sources, String dstDir) {
        return new Move(Iterables.transform(sources, new Function<String, Path>() {
            @Override
            public Path apply(String s) {
                return LocalPath.of(s);
            }
        }), LocalPath.of(dstDir));
    }

    private Move create(File src, File dst) {
        return create(asList(src.getPath()), dst.getPath());
    }
}
