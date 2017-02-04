package l.files.operations;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import l.files.fs.Instant;
import l.files.fs.Path;
import l.files.testing.fs.ExtendedPath;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class CopyTest extends PasteTest {

    public void test_copy_reports_summary() throws Exception {
        Path dstDir = dir1().concat("dir").createDirectory();
        Path srcDir = dir1().concat("a").createDirectory();
        Path srcFile = dir1().concat("a/file").createFile();

        Copy copy = create(singleton(srcDir), dstDir);
        copy.execute();

        List<Path> expected = asList(srcDir, srcFile);
        assertEquals(size(expected), copy.getCopiedByteCount());
        assertEquals(expected.size(), copy.getCopiedItemCount());
    }

    private long size(Iterable<Path> resources) throws IOException {
        long size = 0;
        for (Path file : resources) {
            size += file.stat(NOFOLLOW).size();
        }
        return size;
    }

    public void test_preserves_timestamps_for_file() throws Exception {
        Path src = dir1().concat("a").createFile();
        Path dir = dir1().concat("dir").createDirectory();
        testCopyPreservesTimestamp(src, dir);
    }

    public void test_preserves_timestamps_for_empty_dir() throws Exception {
        Path src = dir1().concat("dir1").createDirectory();
        Path dir = dir1().concat("dir2").createDirectory();
        testCopyPreservesTimestamp(src, dir);
    }

    public void test_preserves_timestamps_for_full_dir() throws Exception {
        Path dir = dir1().concat("dir2").createDirectory();
        Path src = dir1().concat("dir1").createDirectory();
        src.concat("a").createFile();
        src.concat("b").createDirectory();
        src.concat("c").createSymbolicLink(src);
        testCopyPreservesTimestamp(src, dir);
    }

    private void testCopyPreservesTimestamp(
            Path src,
            Path dir) throws IOException, InterruptedException {
        Path dst = dir.concat(src.name().toPath());
        assertFalse(dst.exists(NOFOLLOW));

        Instant mtime = newInstant();
        src.setLastModifiedTime(NOFOLLOW, mtime);

        copy(src, dir);

        assertTrue(dst.exists(NOFOLLOW));
        assertEquals(mtime, mtime(src));
        assertEquals(mtime, mtime(dst));
    }

    private Instant newInstant() {
        return Instant.of(100001, SDK_INT >= LOLLIPOP ? 101 : 0);
    }

    private Instant mtime(Path srcFile) throws IOException {
        return srcFile.stat(NOFOLLOW).lastModifiedTime();
    }

    public void test_copies_link() throws Exception {
        Path target = dir1().concat("target").createFile();
        Path link = dir1().concat("link").createSymbolicLink(target);

        copy(link, dir1().concat("copied").createDirectory());

        Path copied = dir1().concat("copied/link");
        assertEquals(target, copied.readSymbolicLink());
    }

    public void test_copies_directory() throws Exception {
        ExtendedPath srcDir = dir1().concat("a").createDirectory();
        ExtendedPath dstDir = dir1().concat("dst").createDirectory();
        ExtendedPath srcFile = srcDir.concat("test.txt");
        ExtendedPath dstFile = dstDir.concat("a/test.txt");
        srcFile.writeUtf8("Testing");

        copy(srcDir, dstDir);
        assertEquals("Testing", srcFile.readAllUtf8());
        assertEquals("Testing", dstFile.readAllUtf8());
    }

    public void test_copies_empty_directory() throws Exception {
        Path src = dir1().concat("empty").createDirectory();
        Path dir = dir1().concat("dst").createDirectory();
        copy(src, dir);
        assertTrue(dir1().concat("dst/empty").exists(NOFOLLOW));
    }

    public void test_copies_empty_file() throws Exception {
        Path srcFile = dir1().concat("empty").createFile();
        Path dstDir = dir1().concat("dst").createDirectory();

        copy(srcFile, dstDir);
        assertTrue(dir1().concat("dst/empty").exists(NOFOLLOW));
    }

    public void test_copies_file() throws Exception {
        ExtendedPath srcFile = dir1().concat("test.txt").createFile();
        ExtendedPath dstDir = dir1().concat("dst").createDirectory();
        ExtendedPath dstFile = dstDir.concat("test.txt");
        srcFile.writeUtf8("Testing");

        copy(srcFile, dstDir);
        assertEquals("Testing", srcFile.readAllUtf8());
        assertEquals("Testing", dstFile.readAllUtf8());
    }

    private void copy(Path src, Path dstDir)
            throws IOException, InterruptedException {
        create(singleton(src), dstDir).execute();
    }

    @Override
    Copy create(Set<? extends Path> sourcePaths, Path destinationDir) {
        return new Copy(sourcePaths, destinationDir);
    }

}
