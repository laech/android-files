package l.files.io.file.operations;

import com.google.common.io.Files;

import org.mockito.ArgumentCaptor;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.write;
import static java.util.Arrays.asList;
import static l.files.io.file.Files.readlink;
import static l.files.io.file.Files.symlink;
import static l.files.io.file.operations.Copy.Listener;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class CopyTest extends PasteTest {

    private static final Listener NULL_LISTENER = new Listener() {
        @Override
        public void onCopy(String src, String dst) {
        }
    };

    public void testNotifiesListener() throws Exception {
        File srcDir = tmp().createDir("a");
        tmp().createFile("a/file");
        File dstDir = tmp().createDir("dir");

        Listener listener = mock(Listener.class);
        ArgumentCaptor<String> srcCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> dstCaptor = ArgumentCaptor.forClass(String.class);

        create(listener, asList(srcDir.getPath()), dstDir.getPath()).call();
        verify(listener, atLeastOnce()).onCopy(srcCaptor.capture(), dstCaptor.capture());

        List<String> srcExpected = asList(
                tmp().get("a").getPath(),
                tmp().get("a/file").getPath()
        );
        List<String> dstExpected = asList(
                tmp().get("dir/a").getPath(),
                tmp().get("dir/a/file").getPath()
        );
        List<String> srcActual = srcCaptor.getAllValues();
        List<String> dstActual = dstCaptor.getAllValues();

        assertEquals(srcExpected, srcActual);
        assertEquals(dstExpected, dstActual);
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

    private void copy(File src, File dstDir)
            throws IOException, InterruptedException {
        create(asList(src.getPath()), dstDir.getPath()).call();
    }

    @Override
    protected Copy create(Iterable<String> sources, String dstDir) {
        return create(NULL_LISTENER, sources, dstDir);
    }

    private Copy create(Listener listener, Iterable<String> sources, String dstDir) {
        return new Copy(listener, sources, dstDir);
    }
}
