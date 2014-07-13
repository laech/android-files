package l.files.io.file.operations;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.write;
import static java.util.Arrays.asList;
import static l.files.io.file.Files.readlink;
import static l.files.io.file.Files.symlink;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public final class CopyTest extends PasteTest {

    public void testCopySummary() throws Exception {
        File dstDir = tmp().createDir("dir");
        File srcDir = tmp().createDir("a");
        tmp().createFile("a/file");

        Copy copy = create(asList(srcDir.getPath()), dstDir.getPath());
        copy.call();

        List<File> files = asList(
                tmp().get("a"),
                tmp().get("a/file")
        );

        assertThat(copy.getCopiedByteCount(), is(getSize(files)));
        assertThat(copy.getCopiedItemCount(), is(files.size()));
    }

    public long getSize(Iterable<File> files) {
        long size = 0;
        for (File file : files) {
            size += file.length();
        }
        return size;
    }

    public void testCopiesSymlink() throws Exception {
        File target = tmp().createFile("target");
        File link = tmp().get("link");
        symlink(target.getPath(), link.getPath());

        copy(link, tmp().createDir("copied"));

        assertEquals(target.getPath(), readlink(tmp().get("copied/link").getPath()));
    }

    public void testCopiesDirectory() throws Exception {
        File srcDir = tmp().createDir("a");
        File dstDir = tmp().createDir("dst");
        File srcFile = new File(srcDir, "test.txt");
        File dstFile = new File(dstDir, "a/test.txt");
        write("Testing", srcFile, UTF_8);

        copy(srcDir, dstDir);
        assertEquals("Testing", Files.toString(srcFile, UTF_8));
        assertEquals("Testing", Files.toString(dstFile, UTF_8));
    }

    public void testCopiesEmptyFile() throws Exception {
        File srcFile = tmp().createFile("empty");
        File dstDir = tmp().createDir("dst");

        copy(srcFile, dstDir);
        assertTrue(tmp().get("dst/empty").exists());
    }

    public void testCopiesFile() throws Exception {
        File srcFile = tmp().createFile("test.txt");
        File dstDir = tmp().createDir("dst");
        File dstFile = new File(dstDir, "test.txt");
        write("Testing", srcFile, UTF_8);

        copy(srcFile, dstDir);
        assertEquals("Testing", Files.toString(srcFile, UTF_8));
        assertEquals("Testing", Files.toString(dstFile, UTF_8));
    }

    private void copy(File src, File dstDir) throws IOException, InterruptedException {
        create(asList(src.getPath()), dstDir.getPath()).call();
    }

    @Override
    protected Copy create(Iterable<String> sources, String dstDir) {
        return new Copy(sources, dstDir);
    }
}
