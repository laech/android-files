package l.files.operations;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import l.files.fs.Instant;
import l.files.fs.Path;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.testing.fs.Files.readAllUtf8;
import static l.files.testing.fs.Files.writeUtf8;

public final class CopyTest extends PasteTest {

    public void test_copy_reports_summary() throws Exception {
        Path dstDir = fs.createDir(dir1().concat("dir"));
        Path srcDir = fs.createDir(dir1().concat("a"));
        Path srcFile = fs.createFile(dir1().concat("a/file"));

        Copy copy = create(singleton(srcDir), dstDir);
        copy.execute();

        List<Path> expected = asList(srcDir, srcFile);
        assertEquals(size(expected), copy.getCopiedByteCount());
        assertEquals(expected.size(), copy.getCopiedItemCount());
    }

    private long size(Iterable<Path> resources) throws IOException {
        long size = 0;
        for (Path file : resources) {
            size += fs.stat(file, NOFOLLOW).size();
        }
        return size;
    }

    public void test_preserves_timestamps_for_file() throws Exception {
        Path src = fs.createFile(dir1().concat("a"));
        Path dir = fs.createDir(dir1().concat("dir"));
        testCopyPreservesTimestamp(src, dir);
    }

    public void test_preserves_timestamps_for_empty_dir() throws Exception {
        Path src = fs.createDir(dir1().concat("dir1"));
        Path dir = fs.createDir(dir1().concat("dir2"));
        testCopyPreservesTimestamp(src, dir);
    }

    public void test_preserves_timestamps_for_full_dir() throws Exception {
        Path dir = fs.createDir(dir1().concat("dir2"));
        Path src = fs.createDir(dir1().concat("dir1"));
        fs.createFile(src.concat("a"));
        fs.createDir(src.concat("b"));
        fs.createSymbolicLink(src.concat("c"), src);
        testCopyPreservesTimestamp(src, dir);
    }

    private void testCopyPreservesTimestamp(
            Path src,
            Path dir) throws IOException, InterruptedException {
        Path dst = dir.concat(src.name().toPath());
        assertFalse(fs.exists(dst, NOFOLLOW));

        Instant mtime = newInstant();
        fs.setLastModifiedTime(src, NOFOLLOW, mtime);

        copy(src, dir);

        assertTrue(fs.exists(dst, NOFOLLOW));
        assertEquals(mtime, mtime(src));
        assertEquals(mtime, mtime(dst));
    }

    private Instant newInstant() {
        return Instant.of(100001, SDK_INT >= LOLLIPOP ? 101 : 0);
    }

    private Instant mtime(Path srcFile) throws IOException {
        return fs.stat(srcFile, NOFOLLOW).lastModifiedTime();
    }

    public void test_copies_link() throws Exception {
        Path target = fs.createFile(dir1().concat("target"));
        Path link = fs.createSymbolicLink(dir1().concat("link"), target);

        copy(link, fs.createDir(dir1().concat("copied")));

        Path copied = dir1().concat("copied/link");
        assertEquals(target, fs.readSymbolicLink(copied));
    }

    public void test_copies_directory() throws Exception {
        Path srcDir = fs.createDir(dir1().concat("a"));
        Path dstDir = fs.createDir(dir1().concat("dst"));
        Path srcFile = srcDir.concat("test.txt");
        Path dstFile = dstDir.concat("a/test.txt");
        writeUtf8(fs, srcFile, "Testing");

        copy(srcDir, dstDir);
        assertEquals("Testing", readAllUtf8(fs, srcFile));
        assertEquals("Testing", readAllUtf8(fs, dstFile));
    }

    public void test_copies_empty_directory() throws Exception {
        Path src = fs.createDir(dir1().concat("empty"));
        Path dir = fs.createDir(dir1().concat("dst"));
        copy(src, dir);
        assertTrue(fs.exists(dir1().concat("dst/empty"), NOFOLLOW));
    }

    public void test_copies_empty_file() throws Exception {
        Path srcFile = fs.createFile(dir1().concat("empty"));
        Path dstDir = fs.createDir(dir1().concat("dst"));

        copy(srcFile, dstDir);
        assertTrue(fs.exists(dir1().concat("dst/empty"), NOFOLLOW));
    }

    public void test_copies_file() throws Exception {
        Path srcFile = fs.createFile(dir1().concat("test.txt"));
        Path dstDir = fs.createDir(dir1().concat("dst"));
        Path dstFile = dstDir.concat("test.txt");
        writeUtf8(fs, srcFile, "Testing");

        copy(srcFile, dstDir);
        assertEquals("Testing", readAllUtf8(fs, srcFile));
        assertEquals("Testing", readAllUtf8(fs, dstFile));
    }

    private void copy(Path src, Path dstDir)
            throws IOException, InterruptedException {
        create(singleton(src), dstDir).execute();
    }

    @Override
    Copy create(Collection<Path> sources, Path dstDir) {
        return new Copy(sources, dstDir);
    }

}
