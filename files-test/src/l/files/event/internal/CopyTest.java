package l.files.event.internal;

import l.files.test.BaseTest;
import l.files.test.TempDir;

import java.io.File;
import java.io.IOException;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.write;
import static org.apache.commons.io.FileUtils.readFileToString;

public final class CopyTest extends BaseTest {

    private TempDir mDir;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mDir = TempDir.create();
    }

    @Override
    protected void tearDown() throws Exception {
        mDir.delete();
        super.tearDown();
    }

    public void testCopiesFile() throws Exception {
        final File source = mDir.newFile("a");
        final File destination = new File(mDir.get(), "b");
        write("Hello world", source, UTF_8);
        assertTrue(source.setLastModified(1));

        new Copy(source, destination).execute();

        assertTrue(destination.exists());
        assertEquals(source.length(), destination.length());
        assertEquals(source.lastModified(), destination.lastModified());
        assertEquals(readFileToString(source), readFileToString(destination));
    }

    public void testCopiesDirectory() throws Exception {
        final File source = mDir.newDir("a");
        final File destination = new File(mDir.get(), "b");
        mDir.newDir("a/b/c");
        mDir.newDir("a/d");
        mDir.newFile("a/x/y/z.txt");
        assertTrue(source.setLastModified(1));

        new Copy(source, destination).execute();

        assertTrue(new File(mDir.get(), "b/b/c").isDirectory());
        assertTrue(new File(mDir.get(), "b/d").isDirectory());
        assertTrue(new File(mDir.get(), "a/x/y/z.txt").isFile());
        assertEquals(source.lastModified(), destination.lastModified());
    }

    public void testCanNotCopyDirectoryIntoItself() {
        final File source = mDir.newDir("a");
        final File destination = new File(mDir.get(), "a");
        expectIOExceptionOnCopy(source, destination);
    }

    public void testCanNotCopyDirectoryIntoItsSubDirectory() {
        final File source = mDir.newDir("a");
        final File destination = mDir.newDir("a/b");
        expectIOExceptionOnCopy(source, destination);
    }

    public void testCanNotCopyDirectoryToItsSubCanonicalLocation() {
        expectIOExceptionOnCopy(
                mockCanonicalFile(mDir.newDir("a"), mDir.get()),
                mockCanonicalFile(mDir.newDir("b"), new File(mDir.get(), "b")));
    }

    private void expectIOExceptionOnCopy(File source, File destination) {
        try {
            new Copy(source, destination).execute();
            fail();
        } catch (IOException pass) {
        }
    }

    private File mockCanonicalFile(File file, final File canonicalFile) {
        return new File(file.getAbsolutePath()) {
            @Override
            public String getCanonicalPath() throws IOException {
                return canonicalFile.getAbsolutePath();
            }
        };
    }
}
