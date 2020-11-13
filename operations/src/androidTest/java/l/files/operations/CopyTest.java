package l.files.operations;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.nio.file.Files.*;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;

public final class CopyTest extends PasteTest {

    @Test
    public void copy_reports_summary() throws Exception {
        Path dstDir = createDirectory(dir1().concat("dir").toJavaPath());
        Path srcDir = createDirectory(dir1().concat("a").toJavaPath());
        Path srcFile = createFile(dir1().concat("a/file").toJavaPath());

        Copy copy = create(singleton(srcDir), dstDir);
        copy.execute();

        List<Path> expected = asList(srcDir, srcFile);
        assertEquals(size(expected), copy.getCopiedByteCount());
        assertEquals(expected.size(), copy.getCopiedItemCount());
    }

    private long size(Iterable<Path> resources) throws IOException {
        long size = 0;
        for (Path file : resources) {
            size += readAttributes(
                file,
                BasicFileAttributes.class,
                NOFOLLOW_LINKS
            ).size();
        }
        return size;
    }

    @Test
    public void preserves_timestamps_for_file() throws Exception {
        Path src = createFile(dir1().concat("a").toJavaPath());
        Path dir = createDirectory(dir1().concat("dir").toJavaPath());
        testCopyPreservesTimestamp(src, dir);
    }

    @Test
    public void preserves_timestamps_for_empty_dir() throws Exception {
        Path src = createDirectory(dir1().concat("dir1").toJavaPath());
        Path dir = createDirectory(dir1().concat("dir2").toJavaPath());
        testCopyPreservesTimestamp(src, dir);
    }

    @Test
    public void preserves_timestamps_for_full_dir() throws Exception {
        Path dir = createDirectory(dir1().concat("dir2").toJavaPath());
        Path src = createDirectory(dir1().concat("dir1").toJavaPath());
        createFile(src.resolve("a"));
        createDirectory(src.resolve("b"));
        createSymbolicLink(src.resolve("c"), src);
        testCopyPreservesTimestamp(src, dir);
    }

    private void testCopyPreservesTimestamp(
        Path src,
        Path dir
    ) throws IOException, InterruptedException {
        Path dst = dir.resolve(src.getFileName());
        assertFalse(exists(dst, NOFOLLOW_LINKS));

        BasicFileAttributeView view = getFileAttributeView(
            src,
            BasicFileAttributeView.class,
            NOFOLLOW_LINKS
        );

        Instant mtime = view.readAttributes()
            .lastModifiedTime()
            .toInstant()
            .minusSeconds(10);

        setLastModifiedTime(src, FileTime.from(mtime));
        assertEquals(
            mtime,
            view.readAttributes().lastModifiedTime().toInstant()
        );

        copy(src, dir);

        assertTrue(exists(dst, NOFOLLOW_LINKS));
        assertEquals(
            mtime,
            view.readAttributes().lastModifiedTime().toInstant()
        );
        assertEquals("src", mtime, mtime(src));
        assertEquals("dst", mtime, mtime(dst));
    }

    private Instant mtime(Path srcFile) throws IOException {
        return readAttributes(
            srcFile,
            BasicFileAttributes.class,
            NOFOLLOW_LINKS
        )
            .lastModifiedTime()
            .toInstant();
    }

    @Test
    public void copies_link() throws Exception {
        Path target = createFile(dir1().concat("target").toJavaPath());
        Path link =
            createSymbolicLink(dir1().concat("link").toJavaPath(), target);

        copy(link, createDirectory(dir1().toJavaPath().resolve("copied")));

        Path copied = dir1().toJavaPath().resolve("copied/link");
        assertEquals(target, readSymbolicLink(copied));
    }

    @Test
    public void copies_directory() throws Exception {
        Path srcDir = createDirectory(dir1().concat("a").toJavaPath());
        Path dstDir = createDirectory(dir1().concat("dst").toJavaPath());
        Path srcFile = srcDir.resolve("test.txt");
        Path dstFile = dstDir.resolve("a/test.txt");
        write(srcFile, singleton("Testing"));

        copy(srcDir, dstDir);
        assertEquals(singletonList("Testing"), readAllLines(srcFile));
        assertEquals(singletonList("Testing"), readAllLines(dstFile));
    }

    @Test
    public void copies_empty_directory() throws Exception {
        Path src = createDirectory(dir1().concat("empty").toJavaPath());
        Path dir = createDirectory(dir1().concat("dst").toJavaPath());
        copy(src, dir);
        assertTrue(exists(
            dir1().toJavaPath().resolve("dst/empty"),
            NOFOLLOW_LINKS
        ));
    }

    @Test
    public void copies_empty_file() throws Exception {
        Path srcFile = createFile(dir1().concat("empty").toJavaPath());
        Path dstDir = createDirectory(dir1().concat("dst").toJavaPath());

        copy(srcFile, dstDir);
        Path expected = dstDir.resolve("empty");
        if (!exists(expected, NOFOLLOW_LINKS)) {
            try (Stream<Path> stream = list(dstDir)) {
                fail("File " + expected + " doesn't exist, all files are: " +
                    stream.collect(toList()));
            }
        }
    }


    @Test
    public void copies_file() throws Exception {
        Path srcFile = createFile(dir1().concat("test.txt").toJavaPath());
        Path dstDir = createDirectory(dir1().concat("dst").toJavaPath());
        Path dstFile = dstDir.resolve("test.txt");
        write(srcFile, singleton("Testing"));

        copy(srcFile, dstDir);
        assertEquals(singletonList("Testing"), readAllLines(srcFile));
        assertEquals(singletonList("Testing"), readAllLines(dstFile));
    }

    private void copy(Path src, Path dstDir)
        throws InterruptedException {
        create(singleton(src), dstDir).execute();
    }

    @Override
    Copy create(Set<? extends Path> sourcePaths, Path destinationDir) {
        return new Copy(sourcePaths, destinationDir);
    }

}
