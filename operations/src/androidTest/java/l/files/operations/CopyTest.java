package l.files.operations;

import l.files.fs.Path;
import l.files.testing.fs.Paths;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static l.files.fs.LinkOption.NOFOLLOW;
import static org.junit.Assert.*;

public final class CopyTest extends PasteTest {

    @Test
    public void copy_reports_summary() throws Exception {
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

    @Test
    public void preserves_timestamps_for_file() throws Exception {
        Path src = dir1().concat("a").createFile();
        Path dir = dir1().concat("dir").createDirectory();
        testCopyPreservesTimestamp(src, dir);
    }

    @Test
    public void preserves_timestamps_for_empty_dir() throws Exception {
        Path src = dir1().concat("dir1").createDirectory();
        Path dir = dir1().concat("dir2").createDirectory();
        testCopyPreservesTimestamp(src, dir);
    }

    @Test
    public void preserves_timestamps_for_full_dir() throws Exception {
        Path dir = dir1().concat("dir2").createDirectory();
        Path src = dir1().concat("dir1").createDirectory();
        src.concat("a").createFile();
        src.concat("b").createDirectory();
        src.concat("c").createSymbolicLink(src);
        testCopyPreservesTimestamp(src, dir);
    }

    private void testCopyPreservesTimestamp(
        Path src,
        Path dir
    ) throws IOException, InterruptedException {
        Path dst = dir.concat(src.getFileName());
        assertFalse(dst.exists(NOFOLLOW_LINKS));

        BasicFileAttributeView view = src.getFileAttributeView(
            BasicFileAttributeView.class,
            NOFOLLOW_LINKS
        );

        Instant mtime = view.readAttributes()
            .lastModifiedTime()
            .toInstant()
            .minusSeconds(10);

        src.setLastModifiedTime(FileTime.from(mtime));
        assertEquals(
            mtime,
            view.readAttributes().lastModifiedTime().toInstant()
        );

        copy(src, dir);

        assertTrue(dst.exists(NOFOLLOW_LINKS));
        assertEquals(
            mtime,
            view.readAttributes().lastModifiedTime().toInstant()
        );
        assertEquals("src", mtime, mtime(src));
        assertEquals("dst", mtime, mtime(dst));
    }

    private Instant mtime(Path srcFile) throws IOException {
        return srcFile.readAttributes(BasicFileAttributes.class, NOFOLLOW_LINKS)
            .lastModifiedTime()
            .toInstant();
    }

    @Test
    public void copies_link() throws Exception {
        Path target = dir1().concat("target").createFile();
        Path link = dir1().concat("link").createSymbolicLink(target);

        copy(link, dir1().concat("copied").createDirectory());

        Path copied = dir1().concat("copied/link");
        assertEquals(target, copied.readSymbolicLink());
    }

    @Test
    public void copies_directory() throws Exception {
        Path srcDir = dir1().concat("a").createDirectory();
        Path dstDir = dir1().concat("dst").createDirectory();
        Path srcFile = srcDir.concat("test.txt");
        Path dstFile = dstDir.concat("a/test.txt");
        Paths.writeUtf8(srcFile, "Testing");

        copy(srcDir, dstDir);
        assertEquals("Testing", Paths.readAllUtf8(srcFile));
        assertEquals("Testing", Paths.readAllUtf8(dstFile));
    }

    @Test
    public void copies_empty_directory() throws Exception {
        Path src = dir1().concat("empty").createDirectory();
        Path dir = dir1().concat("dst").createDirectory();
        copy(src, dir);
        assertTrue(dir1().concat("dst/empty").exists(NOFOLLOW_LINKS));
    }

    @Test
    public void copies_empty_file() throws Exception {
        Path srcFile = dir1().concat("empty").createFile();
        Path dstDir = dir1().concat("dst").createDirectory();

        copy(srcFile, dstDir);
        Path expected = dstDir.concat("empty");
        if (!expected.exists(NOFOLLOW_LINKS)) {
            List<Path> all = dstDir.list(new ArrayList<>());
            fail("File " + expected + " doesn't exist, all files are: " + all);
        }
    }


    @Test
    public void copies_file() throws Exception {
        Path srcFile = dir1().concat("test.txt").createFile();
        Path dstDir = dir1().concat("dst").createDirectory();
        Path dstFile = dstDir.concat("test.txt");
        Paths.writeUtf8(srcFile, "Testing");

        copy(srcFile, dstDir);
        assertEquals("Testing", Paths.readAllUtf8(srcFile));
        assertEquals("Testing", Paths.readAllUtf8(dstFile));
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
