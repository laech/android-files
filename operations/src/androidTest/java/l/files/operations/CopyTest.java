package l.files.operations;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import l.files.fs.Instant;
import l.files.fs.Name;
import l.files.fs.Path;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static l.files.fs.Files.createDir;
import static l.files.fs.Files.createFile;
import static l.files.fs.Files.createSymbolicLink;
import static l.files.fs.Files.exists;
import static l.files.fs.Files.readAllUtf8;
import static l.files.fs.Files.readSymbolicLink;
import static l.files.fs.Files.setLastModifiedTime;
import static l.files.fs.Files.stat;
import static l.files.fs.Files.writeUtf8;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class CopyTest extends PasteTest {

    public void test_copy_reports_summary() throws Exception {
        Path dstDir = createDir(dir1().resolve("dir"));
        Path srcDir = createDir(dir1().resolve("a"));
        Path srcFile = createFile(dir1().resolve("a/file"));

        Copy copy = create(srcDir.parent(), singleton(srcDir.name()), dstDir);
        copy.execute();

        List<Path> expected = asList(srcDir, srcFile);
        assertEquals(size(expected), copy.getCopiedByteCount());
        assertEquals(expected.size(), copy.getCopiedItemCount());
    }

    private long size(Iterable<Path> resources) throws IOException {
        long size = 0;
        for (Path file : resources) {
            size += stat(file, NOFOLLOW).size();
        }
        return size;
    }

    public void test_preserves_timestamps_for_file() throws Exception {
        Path src = createFile(dir1().resolve("a"));
        Path dir = createDir(dir1().resolve("dir"));
        testCopyPreservesTimestamp(src, dir);
    }

    public void test_preserves_timestamps_for_empty_dir() throws Exception {
        Path src = createDir(dir1().resolve("dir1"));
        Path dir = createDir(dir1().resolve("dir2"));
        testCopyPreservesTimestamp(src, dir);
    }

    public void test_preserves_timestamps_for_full_dir() throws Exception {
        Path dir = createDir(dir1().resolve("dir2"));
        Path src = createDir(dir1().resolve("dir1"));
        createFile(src.resolve("a"));
        createDir(src.resolve("b"));
        createSymbolicLink(src.resolve("c"), src);
        testCopyPreservesTimestamp(src, dir);
    }

    private void testCopyPreservesTimestamp(
            Path src,
            Path dir) throws IOException, InterruptedException {
        Path dst = dir.resolve(src.name());
        assertFalse(exists(dst, NOFOLLOW));

        Instant mtime = newInstant();
        setLastModifiedTime(src, NOFOLLOW, mtime);

        copy(src, dir);

        assertTrue(exists(dst, NOFOLLOW));
        assertEquals(mtime, mtime(src));
        assertEquals(mtime, mtime(dst));
    }

    private Instant newInstant() {
        return Instant.of(100001, SDK_INT >= LOLLIPOP ? 101 : 0);
    }

    private Instant mtime(Path srcFile) throws IOException {
        return stat(srcFile, NOFOLLOW).lastModifiedTime();
    }

    public void test_copies_link() throws Exception {
        Path target = createFile(dir1().resolve("target"));
        Path link = createSymbolicLink(dir1().resolve("link"), target);

        copy(link, createDir(dir1().resolve("copied")));

        Path copied = dir1().resolve("copied/link");
        assertEquals(target, readSymbolicLink(copied));
    }

    public void test_copies_directory() throws Exception {
        Path srcDir = createDir(dir1().resolve("a"));
        Path dstDir = createDir(dir1().resolve("dst"));
        Path srcFile = srcDir.resolve("test.txt");
        Path dstFile = dstDir.resolve("a/test.txt");
        writeUtf8(srcFile, "Testing");

        copy(srcDir, dstDir);
        assertEquals("Testing", readAllUtf8(srcFile));
        assertEquals("Testing", readAllUtf8(dstFile));
    }

    public void test_copies_empty_directory() throws Exception {
        Path src = createDir(dir1().resolve("empty"));
        Path dir = createDir(dir1().resolve("dst"));
        copy(src, dir);
        assertTrue(exists(dir1().resolve("dst/empty"), NOFOLLOW));
    }

    public void test_copies_empty_file() throws Exception {
        Path srcFile = createFile(dir1().resolve("empty"));
        Path dstDir = createDir(dir1().resolve("dst"));

        copy(srcFile, dstDir);
        assertTrue(exists(dir1().resolve("dst/empty"), NOFOLLOW));
    }

    public void test_copies_file() throws Exception {
        Path srcFile = createFile(dir1().resolve("test.txt"));
        Path dstDir = createDir(dir1().resolve("dst"));
        Path dstFile = dstDir.resolve("test.txt");
        writeUtf8(srcFile, "Testing");

        copy(srcFile, dstDir);
        assertEquals("Testing", readAllUtf8(srcFile));
        assertEquals("Testing", readAllUtf8(dstFile));
    }

    private void copy(Path src, Path dstDir)
            throws IOException, InterruptedException {
        create(src.parent(), singleton(src.name()), dstDir).execute();
    }

    @Override
    Copy create(Path sourceDirectory, Collection<Name> sourceFiles, Path destinationDirectory) {
        return new Copy(sourceDirectory, sourceFiles, destinationDirectory);
    }
}
