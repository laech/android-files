package l.files.operations;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import l.files.fs.Instant;
import l.files.fs.Resource;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class CopyTest extends PasteTest
{

    public void test_copy_reports_summary() throws Exception
    {
        final Resource dstDir = dir1().resolve("dir").createDirectory();
        final Resource srcDir = dir1().resolve("a").createDirectory();
        final Resource srcFile = dir1().resolve("a/file").createFile();

        final Copy copy = create(singleton(srcDir), dstDir);
        copy.execute();

        final List<Resource> expected = asList(srcDir, srcFile);
        assertEquals(size(expected), copy.getCopiedByteCount());
        assertEquals(expected.size(), copy.getCopiedItemCount());
    }

    private long size(final Iterable<Resource> resources) throws IOException
    {
        long size = 0;
        for (final Resource resource : resources)
        {
            size += resource.stat(NOFOLLOW).size();
        }
        return size;
    }

    public void test_preserves_timestamps_for_file() throws Exception
    {
        final Resource src = dir1().resolve("a").createFile();
        final Resource dir = dir1().resolve("dir").createDirectory();
        testCopyPreservesTimestamp(src, dir);
    }

    public void test_preserves_timestamps_for_empty_dir() throws Exception
    {
        final Resource src = dir1().resolve("dir1").createDirectory();
        final Resource dir = dir1().resolve("dir2").createDirectory();
        testCopyPreservesTimestamp(src, dir);
    }

    public void test_preserves_timestamps_for_full_dir() throws Exception
    {
        final Resource dir = dir1().resolve("dir2").createDirectory();
        final Resource src = dir1().resolve("dir1").createDirectory();
        src.resolve("a").createFile();
        src.resolve("b").createDirectory();
        src.resolve("c").createLink(src);
        testCopyPreservesTimestamp(src, dir);
    }

    private void testCopyPreservesTimestamp(
            final Resource src,
            final Resource dir) throws IOException, InterruptedException
    {
        final Resource dst = dir.resolve(src.name());
        assertFalse(dst.exists(NOFOLLOW));

        final Instant atime = Instant.of(123, 456);
        final Instant mtime = Instant.of(100001, 101);
        src.setAccessed(NOFOLLOW, atime);
        src.setModified(NOFOLLOW, mtime);

        copy(src, dir);

        assertTrue(dst.exists(NOFOLLOW));
        assertEquals(atime, atime(src));
        assertEquals(atime, atime(dst));
        assertEquals(mtime, mtime(src));
        assertEquals(mtime, mtime(dst));
    }

    private Instant mtime(final Resource srcFile) throws IOException
    {
        return srcFile.stat(NOFOLLOW).modified();
    }

    private Instant atime(final Resource res) throws IOException
    {
        return res.stat(NOFOLLOW).accessed();
    }

    public void test_copies_link() throws Exception
    {
        final Resource target = dir1().resolve("target").createFile();
        final Resource link = dir1().resolve("link").createLink(target);

        copy(link, dir1().resolve("copied").createDirectory());

        final Resource copied = dir1().resolve("copied/link");
        assertEquals(target, copied.readLink());
    }

    public void test_copies_directory() throws Exception
    {
        final Resource srcDir = dir1().resolve("a").createDirectory();
        final Resource dstDir = dir1().resolve("dst").createDirectory();
        final Resource srcFile = srcDir.resolve("test.txt");
        final Resource dstFile = dstDir.resolve("a/test.txt");
        try (Writer out = srcFile.writer(NOFOLLOW, UTF_8))
        {
            out.write("Testing");
        }

        copy(srcDir, dstDir);
        assertEquals("Testing", srcFile.readString(NOFOLLOW, UTF_8));
        assertEquals("Testing", dstFile.readString(NOFOLLOW, UTF_8));
    }

    public void test_copies_empty_directory() throws Exception
    {
        final Resource src = dir1().resolve("empty").createDirectory();
        final Resource dir = dir1().resolve("dst").createDirectory();
        copy(src, dir);
        assertTrue(dir1().resolve("dst/empty").exists(NOFOLLOW));
    }

    public void test_copies_empty_file() throws Exception
    {
        final Resource srcFile = dir1().resolve("empty").createFile();
        final Resource dstDir = dir1().resolve("dst").createDirectory();

        copy(srcFile, dstDir);
        assertTrue(dir1().resolve("dst/empty").exists(NOFOLLOW));
    }

    public void test_copies_file() throws Exception
    {
        final Resource srcFile = dir1().resolve("test.txt").createFile();
        final Resource dstDir = dir1().resolve("dst").createDirectory();
        final Resource dstFile = dstDir.resolve("test.txt");
        try (Writer writer = srcFile.writer(NOFOLLOW, UTF_8))
        {
            writer.write("Testing");
        }

        copy(srcFile, dstDir);
        assertEquals("Testing", srcFile.readString(NOFOLLOW, UTF_8));
        assertEquals("Testing", dstFile.readString(NOFOLLOW, UTF_8));
    }

    private void copy(final Resource src, final Resource dstDir)
            throws IOException, InterruptedException
    {
        create(singleton(src), dstDir).execute();
    }

    @Override
    protected Copy create(
            final Iterable<Resource> sources,
            final Resource dstDir)
    {
        return new Copy(sources, dstDir);
    }

}
