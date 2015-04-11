package l.files.fs.local;

import com.google.common.base.Strings;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

import l.files.common.testing.TempDir;
import l.files.fs.AccessException;
import l.files.fs.ExistsException;
import l.files.fs.LoopException;
import l.files.fs.NotDirectoryException;
import l.files.fs.NotExistException;
import l.files.fs.PathTooLongException;
import l.files.fs.Resource;
import l.files.fs.ResourceStatus;

public final class LocalResourceTest extends TestCase {

    private TempDir tmp;
    private File directory;
    private LocalResource resource;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tmp = TempDir.create();
        directory = tmp.get();
        resource = LocalResource.create(directory);
    }

    @Override
    protected void tearDown() throws Exception {
        tmp.delete(); // TODO change to resource deleteAll
        super.tearDown();
    }

    public void test_createFile() throws Exception {
        Resource file = resource.resolve("a");
        file.createFile();
        assertTrue(file.readStatus(false).isRegularFile());
    }

    public void test_createFile_correctPermissions() throws Exception {
        Resource actual = resource.resolve("a");
        actual.createFile();

        File expected = new File(directory, "b");
        assertTrue(expected.createNewFile());

        ResourceStatus status = actual.readStatus(false);
        assertEquals(expected.canRead(), status.isReadable());
        assertEquals(expected.canWrite(), status.isWritable());
        assertEquals(expected.canExecute(), status.isExecutable());
    }

    public void test_createFile_AccessException() throws Exception {
        assertTrue(directory.setWritable(false));
        expectOnCreateFile(AccessException.class, resource.resolve("a"));
    }

    public void test_createFile_ExistsException() throws Exception {
        Resource child = resource.resolve("a");
        child.createFile();
        expectOnCreateFile(ExistsException.class, child);
    }

    public void test_createFile_PathTooLongException() throws Exception {
        expectOnCreateFile(PathTooLongException.class, createLongPath());
    }

    public void test_createFile_LoopException() throws Exception {
        expectOnCreateFile(LoopException.class, createLoop().resolve("a"));
    }

    public void test_createFile_NotExistException() throws Exception {
        expectOnCreateFile(NotExistException.class, resource.resolve("a/b"));
    }

    public void test_createFile_NotDirectoryException() throws Exception {
        Resource parent = resource.resolve("parent");
        Resource child = parent.resolve("child");
        parent.createFile();
        expectOnCreateFile(NotDirectoryException.class, child);
    }

    private void expectOnCreateFile(
            final Class<? extends Exception> clazz,
            final Resource resource) throws Exception {
        expect(clazz, new Code() {
            @Override
            public void run() throws Exception {
                resource.createFile();
            }
        });
    }

    public void test_createDirectory() throws Exception {
        Resource dir = resource.resolve("a");
        dir.createDirectory();
        assertTrue(dir.readStatus(false).isDirectory());
    }

    public void test_createDirectory_correctPermissions() throws Exception {
        Resource actual = resource.resolve("a");
        actual.createDirectory();

        File expected = new File(directory, "b");
        assertTrue(expected.mkdir());

        ResourceStatus status = actual.readStatus(false);
        assertEquals(expected.canRead(), status.isReadable());
        assertEquals(expected.canWrite(), status.isWritable());
        assertEquals(expected.canExecute(), status.isExecutable());
    }

    public void test_createDirectory_AccessException() throws Exception {
        assertTrue(directory.setWritable(false));
        final Resource dir = resource.resolve("a");
        expectOnCreateDirectory(AccessException.class, dir);
    }

    public void test_createDirectory_ExistsException() throws Exception {
        expectOnCreateDirectory(ExistsException.class, resource);
    }

    public void test_createDirectory_LoopException() throws Exception {
        Resource a = createLoop();
        expectOnCreateDirectory(LoopException.class, a.resolve("c"));
    }

    public void test_createDirectory_PathTooLongException() throws Exception {
        Resource dir = createLongPath();
        expectOnCreateDirectory(PathTooLongException.class, dir);
    }

    public void test_createDirectory_NotFoundException() throws Exception {
        Resource dir = resource.resolve("a/b");
        expectOnCreateDirectory(NotExistException.class, dir);
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

    public void test_createDirectories() throws Exception {
        resource.resolve("a/b/c").createDirectories();
        assertTrue(resource.resolve("a/b/c").readStatus(false).isDirectory());
        assertTrue(resource.resolve("a/b").readStatus(false).isDirectory());
        assertTrue(resource.resolve("a/").readStatus(false).isDirectory());
    }

    public void test_createDirectories_NotDirectoryException() throws Exception {
        Resource parent = resource.resolve("a");
        Resource child = parent.resolve("b");
        parent.createFile();
        expectOnCreateDirectories(NotDirectoryException.class, child);
    }

    private void expectOnCreateDirectories(
            final Class<? extends Exception> clazz,
            final Resource resource) throws Exception {
        expect(clazz, new Code() {
            @Override
            public void run() throws Exception {
                resource.createDirectories();
            }
        });
    }

    public void test_createSymbolicLink() throws Exception {
        Resource link = resource.resolve("link");
        link.createSymbolicLink(resource);
        assertTrue(link.readStatus(false).isSymbolicLink());
        assertEquals(resource, link.readSymbolicLink());
    }

    public void test_createSymbolicLink_AccessException() throws Exception {
        assertTrue(directory.setWritable(false));
        Resource link = resource.resolve("a");
        expectOnCreateSymbolicLink(AccessException.class, link, resource);
    }

    public void test_createSymbolicLink_ExistsException() throws Exception {
        Resource link = resource.resolve("a");
        link.createFile();
        expectOnCreateSymbolicLink(ExistsException.class, link, resource);
    }

    public void test_createSymbolicLink_LoopException() throws Exception {
        Resource loop = createLoop();
        Resource link = loop.resolve("a");
        expectOnCreateSymbolicLink(LoopException.class, link, resource);
    }

    public void test_createSymbolicLink_PathTooLongException() throws Exception {
        Resource link = createLongPath();
        expectOnCreateSymbolicLink(PathTooLongException.class, link, resource);
    }

    public void test_createSymbolicLink_NotExistException() throws Exception {
        Resource link = resource.resolve("a/b");
        expectOnCreateSymbolicLink(NotExistException.class, link, resource);
    }

    public void test_createSymbolicLink_NotDirectoryException() throws Exception {
        Resource parent = resource.resolve("parent");
        Resource link = parent.resolve("link");
        parent.createFile();
        expectOnCreateSymbolicLink(NotDirectoryException.class, link, resource);
    }

    private void expectOnCreateSymbolicLink(
            final Class<? extends Exception> clazz,
            final Resource link,
            final Resource target) throws Exception {
        expect(clazz, new Code() {
            @Override
            public void run() throws Exception {
                link.createSymbolicLink(target);
            }
        });
    }

    public void test_readSymbolicLink_AccessException() throws Exception {
        assertTrue(directory.setExecutable(false));
        Resource link = resource.resolve("a");
        expectOnReadSymbolicLink(AccessException.class, link);
    }

    public void test_readSymbolicLink_NotExistException() throws Exception {
        Resource link = resource.resolve("a");
        expectOnReadSymbolicLink(NotExistException.class, link);
    }

    public void test_readSymbolicLink_LoopException() throws Exception {
        Resource loop = createLoop().resolve("a");
        expectOnReadSymbolicLink(LoopException.class, loop);
    }

    public void test_readSymbolicLink_PathTooLongException() throws Exception {
        expectOnReadSymbolicLink(PathTooLongException.class, createLongPath());
    }

    public void test_readSymbolicLink_NotDirectoryException() throws Exception {
        Resource parent = resource.resolve("parent");
        Resource link = parent.resolve("link");
        parent.createFile();
        expectOnReadSymbolicLink(NotDirectoryException.class, link);
    }

    private void expectOnReadSymbolicLink(
            final Class<? extends Exception> clazz,
            final Resource link) throws Exception {
        expect(clazz, new Code() {
            @Override
            public void run() throws Exception {
                link.readSymbolicLink();
            }
        });
    }

    public void test_readStatus_followLink() throws Exception {
        LocalResource child = resource.resolve("a");
        child.createSymbolicLink(resource);

        LocalResourceStatus status = child.readStatus(true);
        assertTrue(status.isDirectory());
        assertFalse(status.isSymbolicLink());
        assertEquals(resource.readStatus(false).getInode(), status.getInode());
    }

    public void test_readStatus_noFollowLink() throws Exception {
        LocalResource child = resource.resolve("a");
        child.createSymbolicLink(resource);

        LocalResourceStatus status = child.readStatus(false);
        assertTrue(status.isSymbolicLink());
        assertFalse(status.isDirectory());
        assertTrue(resource.readStatus(false).getInode() != status.getInode());
    }

    public void test_readStatus_AccessException() throws Exception {
        Resource child = resource.resolve("a");
        assertTrue(directory.setReadable(false));
        assertTrue(directory.setExecutable(false));
        expectOnReadStatus(AccessException.class, child);
    }

    public void test_readStatus_LoopException() throws Exception {
        Resource a = createLoop();
        expectOnReadStatus(LoopException.class, a.resolve("c"));
    }

    public void test_readStatus_PathTooLongException() throws Exception {
        Resource child = createLongPath();
        expectOnReadStatus(PathTooLongException.class, child);
    }

    public void test_readStatus_NotExistException() throws Exception {
        Resource child = resource.resolve("a/b");
        expectOnReadStatus(NotExistException.class, child);
    }

    public void test_readStatus_NotDirectoryException() throws Exception {
        Resource parent = resource.resolve("a");
        Resource child = parent.resolve("b");
        parent.createFile();
        expectOnReadStatus(NotDirectoryException.class, child);
    }

    /**
     * Creates a loop and returns one of the looped resource.
     */
    private Resource createLoop() throws IOException {
        Resource a = resource.resolve("a");
        Resource b = resource.resolve("b");
        a.createSymbolicLink(b);
        b.createSymbolicLink(a);
        return a;
    }

    private Resource createLongPath() {
        return resource.resolve(Strings.repeat("a", 512));
    }

    private void expectOnReadStatus(
            final Class<? extends Exception> clazz,
            final Resource resource) throws Exception {
        expect(clazz, new Code() {
            @Override
            public void run() throws Exception {
                resource.readStatus(false);
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
