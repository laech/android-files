package l.files.operations;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import l.files.fs.Resource;

import static java.lang.Thread.sleep;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class CopyTest extends PasteTest {

    public void testCopySummary() throws Exception {
        Resource dstDir = dir1().resolve("dir").createDirectory();
        Resource srcDir = dir1().resolve("a").createDirectory();
        Resource srcFile = dir1().resolve("a/file").createFile();

        Copy copy = create(singleton(srcDir), dstDir);
        copy.execute();

        List<Resource> expected = asList(srcDir, srcFile);
        assertEquals(getSize(expected), copy.getCopiedByteCount());
        assertEquals(expected.size(), copy.getCopiedItemCount());
    }

    public long getSize(Iterable<Resource> resources) throws IOException {
        long size = 0;
        for (Resource resource : resources) {
            size += resource.readStatus(NOFOLLOW).getSize();
        }
        return size;
    }

    public void testCopiesFileTimes() throws Exception {
        Resource srcFile = dir1().resolve("a").createFile();
        Resource dstDir = dir1().resolve("dst").createDirectory();
        Resource dstFile = dstDir.resolve(srcFile.getName());

        sleep(5);
        copy(srcFile, dstDir);

        assertEquals(
                srcFile.readStatus(NOFOLLOW).getAccessTime(),
                dstFile.readStatus(NOFOLLOW).getAccessTime()
        );
        assertEquals(
                srcFile.readStatus(NOFOLLOW).getModificationTime(),
                dstFile.readStatus(NOFOLLOW).getModificationTime()
        );
    }

    public void testCopiesSymlink() throws Exception {
        Resource target = dir1().resolve("target").createFile();
        Resource link = dir1().resolve("link").createSymbolicLink(target);

        copy(link, dir1().resolve("copied").createDirectory());

        Resource copied = dir1().resolve("copied/link");
        assertEquals(target, copied.readSymbolicLink());
    }

    public void testCopiesDirectory() throws Exception {
        Resource srcDir = dir1().resolve("a").createDirectory();
        Resource dstDir = dir1().resolve("dst").createDirectory();
        Resource srcFile = srcDir.resolve("test.txt");
        Resource dstFile = dstDir.resolve("a/test.txt");
        try (Writer out = srcFile.openWriter(UTF_8)) {
            out.write("Testing");
        }

        copy(srcDir, dstDir);
        assertEquals("Testing", srcFile.readString(UTF_8));
        assertEquals("Testing", dstFile.readString(UTF_8));
    }

    public void testCopiesEmptyFile() throws Exception {
        Resource srcFile = dir1().resolve("empty").createFile();
        Resource dstDir = dir1().resolve("dst").createDirectory();

        copy(srcFile, dstDir);
        assertTrue(dir1().resolve("dst/empty").exists(NOFOLLOW));
    }

    public void testCopiesFile() throws Exception {
        Resource srcFile = dir1().resolve("test.txt").createFile();
        Resource dstDir = dir1().resolve("dst").createDirectory();
        Resource dstFile = dstDir.resolve("test.txt");
        try (Writer writer = srcFile.openWriter(UTF_8)) {
            writer.write("Testing");
        }

        copy(srcFile, dstDir);
        assertEquals("Testing", srcFile.readString(UTF_8));
        assertEquals("Testing", dstFile.readString(UTF_8));
    }

    private void copy(Resource src, Resource dstDir)
            throws IOException, InterruptedException {
        create(singleton(src), dstDir).execute();
    }

    @Override
    protected Copy create(Iterable<Resource> sources, Resource dstDir) {
        return new Copy(sources, dstDir);
    }

}
