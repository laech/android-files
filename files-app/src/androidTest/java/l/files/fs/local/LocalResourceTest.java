package l.files.fs.local;

import com.google.common.base.Strings;

import junit.framework.TestCase;

import java.io.File;

import l.files.common.testing.TempDir;
import l.files.fs.AccessException;
import l.files.fs.ExistsException;
import l.files.fs.NotDirectoryException;
import l.files.fs.NotFoundException;
import l.files.fs.PathTooLongException;
import l.files.fs.Resource;

public final class LocalResourceTest extends TestCase {

    private TempDir tmp;
    private File directory;
    private Resource resource;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tmp = TempDir.create();
        directory = tmp.get();
        resource = LocalResource.create(directory);
    }

    @Override
    protected void tearDown() throws Exception {
        tmp.delete();
        super.tearDown();
    }

    public void test_createDirectory() throws Exception {
        Resource dir = resource.resolve("a");
        dir.createDirectory();
        assertTrue(dir.readStatus(false).isDirectory());
    }

    public void test_createDirectory_AccessException() throws Exception {
        assertTrue(directory.setWritable(false));
        final Resource dir = resource.resolve("a");
        expectOnCreateDirectory(AccessException.class, dir);
    }

    public void test_createDirectory_ExistsException() throws Exception {
        expectOnCreateDirectory(ExistsException.class, resource);
    }

    public void test_createDirectory_PathTooLongException() throws Exception {
        Resource dir = resource.resolve(Strings.repeat("a", 512));
        expectOnCreateDirectory(PathTooLongException.class, dir);
    }

    public void test_createDirectory_NotFoundException() throws Exception {
        Resource dir = resource.resolve("a/b");
        expectOnCreateDirectory(NotFoundException.class, dir);
    }

    public void test_createDirectory_NotDirectoryException() throws Exception {
        Resource parent = resource.resolve("parent");
        Resource child = parent.resolve("child");
        parent.createFile();
        expectOnCreateDirectory(NotDirectoryException.class, child);
    }

    private void expectOnCreateDirectory(
            final Class<? extends Exception> clazz,
            final Resource resource) throws Exception {
        expect(clazz, new Code() {
            @Override
            public void run() throws Exception {
                resource.createDirectory();
            }
        });
    }

    private void expect(Class<? extends Exception> clazz, Code code) throws Exception {
        try {
            code.run();
            fail();
        } catch (Exception e) {
            if (!clazz.isInstance(e)) {
                throw e;
            }
        }
    }

    private static interface Code {
        void run() throws Exception;
    }

}
