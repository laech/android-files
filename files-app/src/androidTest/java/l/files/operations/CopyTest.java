package l.files.operations;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.List;

import l.files.fs.Resource;
import l.files.fs.local.LocalResource;

import static com.google.common.io.Files.write;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;

public final class CopyTest extends PasteTest {

    public void testCopySummary() throws Exception {
        File dstDir = tmp().createDir("dir");
        File srcDir = tmp().createDir("a");
        tmp().createFile("a/file");

        Copy copy = create(asList(srcDir.getPath()), dstDir.getPath());
        copy.execute();

        List<File> files = asList(
                tmp().get("a"),
                tmp().get("a/file")
        );

        assertEquals(getSize(files), copy.getCopiedByteCount());
        assertEquals(files.size(), copy.getCopiedItemCount());
    }

    public long getSize(Iterable<File> files) {
        long size = 0;
        for (File file : files) {
            size += file.length();
        }
        return size;
    }

    public void testCopiesSymlink() throws Exception {
        Resource target = LocalResource.create(tmp().createFile("target"));
        Resource link = LocalResource.create(tmp().get("link"));
        link.createSymbolicLink(target);

        copy(new File(link.getUri()), tmp().createDir("copied"));

        LocalResource copied = LocalResource.create(tmp().get("copied/link"));
        assertEquals(target, copied.readSymbolicLink());
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
        create(asList(src.getPath()), dstDir.getPath()).execute();
    }

    @Override
    protected Copy create(Iterable<String> sources, String dstDir) {
        return new Copy(Iterables.transform(sources, new Function<String, Resource>() {
            @Override
            public Resource apply(String input) {
                return LocalResource.create(new File(input));
            }
        }), LocalResource.create(new File(dstDir)));
    }

}
