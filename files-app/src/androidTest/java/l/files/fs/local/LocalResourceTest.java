package l.files.fs.local;

import android.system.Os;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import l.files.fs.AccessException;
import l.files.fs.CrossDeviceException;
import l.files.fs.ExistsException;
import l.files.fs.Instant;
import l.files.fs.InvalidException;
import l.files.fs.IsDirectoryException;
import l.files.fs.NotDirectoryException;
import l.files.fs.NotEmptyException;
import l.files.fs.NotExistException;
import l.files.fs.Permission;
import l.files.fs.Resource;
import l.files.fs.ResourceStatus;

import static android.test.MoreAsserts.assertNotEqual;
import static com.google.common.collect.Sets.powerSet;
import static com.google.common.io.Files.write;
import static java.lang.System.nanoTime;
import static java.lang.Thread.sleep;
import static java.nio.charset.StandardCharsets.UTF_8;
import static l.files.fs.Instant.EPOCH;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.local.LocalResource.mapPermissions;
import static l.files.fs.local.Stat.lstat;

public final class LocalResourceTest extends ResourceBaseTest {

    private LocalResource resource;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        resource = dir1();
    }

    public void test_exists_true() throws Exception {
        assertTrue(dir1().exists(NOFOLLOW));
    }

    public void test_exists_false() throws Exception {
        assertFalse(dir1().resolve("a").exists(NOFOLLOW));
    }

    public void test_exists_false_whenUnableToDetermine() throws Exception {
        Resource file = dir1().resolve("a").createFile();
        assertTrue(file.exists(NOFOLLOW));

        dir1().setPermissions(Collections.<Permission>emptySet());
        assertFalse(file.exists(FOLLOW));
        assertFalse(file.exists(NOFOLLOW));
    }

    public void test_exists_checkLinkNotTarget() throws Exception {
        Resource target = dir1().resolve("target");
        Resource link = dir1().resolve("link").createSymbolicLink(target);
        assertFalse(target.exists(NOFOLLOW));
        assertFalse(link.exists(FOLLOW));
        assertTrue(link.exists(NOFOLLOW));
    }

    public void test_readString() throws Exception {
        Resource file = dir1().resolve("file").createFile();
        String expected = "a\nb\tc";
        try (Writer writer = file.openWriter(UTF_8)) {
            writer.write(expected);
        }
        assertEquals(expected, file.readString(UTF_8));
    }

    public void test_createFile() throws Exception {
        Resource file = resource.resolve("a");
        file.createFile();
        assertTrue(file.readStatus(false).isRegularFile());
    }

    public void test_createFile_correctPermissions() throws Exception {
        Resource actual = resource.resolve("a");
        actual.createFile();

        File expected = new File(resource.getFile(), "b");
        assertTrue(expected.createNewFile());

        ResourceStatus status = actual.readStatus(false);
        assertEquals(expected.canRead(), status.isReadable());
        assertEquals(expected.canWrite(), status.isWritable());
        assertEquals(expected.canExecute(), status.isExecutable());
        assertEquals(
                mapPermissions(lstat(expected.getPath()).getMode()),
                status.getPermissions()
        );
    }

    public void test_createFile_AccessException() throws Exception {
        resource.setPermissions(Collections.<Permission>emptySet());
        expectOnCreateFile(AccessException.class, resource.resolve("a"));
    }

    public void test_createFile_ExistsException() throws Exception {
        Resource child = resource.resolve("a");
        child.createFile();
        expectOnCreateFile(ExistsException.class, child);
    }

    public void test_createFile_NotExistException() throws Exception {
        expectOnCreateFile(NotExistException.class, resource.resolve("a/b"));
    }

    public void test_createFile_NotDirectoryException() throws Exception {
        Resource child = createChildWithNonDirectoryParent();
        expectOnCreateFile(NotDirectoryException.class, child);
    }

    private static void expectOnCreateFile(
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

        File expected = new File(resource.getFile(), "b");
        assertTrue(expected.mkdir());

        ResourceStatus status = actual.readStatus(false);
        assertEquals(expected.canRead(), status.isReadable());
        assertEquals(expected.canWrite(), status.isWritable());
        assertEquals(expected.canExecute(), status.isExecutable());
        assertEquals(
                mapPermissions(lstat(expected.getPath()).getMode()),
                status.getPermissions()
        );
    }

    public void test_createDirectory_AccessException() throws Exception {
        resource.setPermissions(Collections.<Permission>emptySet());
        Resource dir = resource.resolve("a");
        expectOnCreateDirectory(AccessException.class, dir);
    }

    public void test_createDirectory_ExistsException() throws Exception {
        expectOnCreateDirectory(ExistsException.class, resource);
    }

    public void test_createDirectory_NotFoundException() throws Exception {
        Resource dir = resource.resolve("a/b");
        expectOnCreateDirectory(NotExistException.class, dir);
    }

    public void test_createDirectory_NotDirectoryException() throws Exception {
        Resource child = createChildWithNonDirectoryParent();
        expectOnCreateDirectory(NotDirectoryException.class, child);
    }

    private static void expectOnCreateDirectory(
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

    private static void expectOnCreateDirectories(
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
        resource.setPermissions(Collections.<Permission>emptySet());
        Resource link = resource.resolve("a");
        expectOnCreateSymbolicLink(AccessException.class, link, resource);
    }

    public void test_createSymbolicLink_ExistsException() throws Exception {
        Resource link = resource.resolve("a");
        link.createFile();
        expectOnCreateSymbolicLink(ExistsException.class, link, resource);
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

    private static void expectOnCreateSymbolicLink(
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
        resource.setPermissions(Collections.<Permission>emptySet());
        Resource link = resource.resolve("a");
        expectOnReadSymbolicLink(AccessException.class, link);
    }

    public void test_readSymbolicLink_InvalidException() throws Exception {
        Resource notLink = resource.resolve("notLink");
        notLink.createFile();
        expectOnReadSymbolicLink(InvalidException.class, notLink);
    }

    public void test_readSymbolicLink_NotExistException() throws Exception {
        Resource link = resource.resolve("a");
        expectOnReadSymbolicLink(NotExistException.class, link);
    }

    public void test_readSymbolicLink_NotDirectoryException() throws Exception {
        Resource parent = resource.resolve("parent");
        Resource link = parent.resolve("link");
        parent.createFile();
        expectOnReadSymbolicLink(NotDirectoryException.class, link);
    }

    private static void expectOnReadSymbolicLink(
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
        resource.setPermissions(Collections.<Permission>emptySet());
        Resource child = resource.resolve("a");
        expectOnReadStatus(AccessException.class, child);
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

    private static void expectOnReadStatus(
            final Class<? extends Exception> clazz,
            final Resource resource) throws Exception {
        expect(clazz, new Code() {
            @Override
            public void run() throws Exception {
                resource.readStatus(false);
            }
        });
    }

    public void test_renameTo_fileToExistingFileWillOverride() throws Exception {
        LocalResource src = resource.resolve("src");
        LocalResource dst = resource.resolve("dst");
        write("src", src.getFile(), UTF_8);
        write("dst", dst.getFile(), UTF_8);
        src.renameTo(dst);
        assertFalse(src.exists(NOFOLLOW));
        assertTrue(dst.exists(NOFOLLOW));
        assertEquals("src", com.google.common.io.Files.toString(dst.getFile(), UTF_8));
    }

    public void test_renameTo_fileToNonExistingFile() throws Exception {
        LocalResource src = resource.resolve("src");
        LocalResource dst = resource.resolve("dst");
        write("src", src.getFile(), UTF_8);
        src.renameTo(dst);
        assertFalse(src.exists(NOFOLLOW));
        assertTrue(dst.exists(NOFOLLOW));
        assertEquals("src", com.google.common.io.Files.toString(dst.getFile(), UTF_8));
    }

    public void test_renameTo_directoryToNonExistingDirectory() throws Exception {
        LocalResource src = resource.resolve("src");
        LocalResource dst = resource.resolve("dst");
        src.resolve("a").createDirectories();
        src.renameTo(dst);
        assertFalse(src.exists(NOFOLLOW));
        assertTrue(dst.exists(NOFOLLOW));
        assertTrue(dst.resolve("a").exists(NOFOLLOW));
    }

    public void test_renameTo_directoryToExistingEmptyDirectoryWillOverride() throws Exception {
        LocalResource src = resource.resolve("src");
        LocalResource dst = resource.resolve("dst");
        dst.createDirectory();
        src.resolve("a").createDirectories();
        src.renameTo(dst);
        assertFalse(src.exists(NOFOLLOW));
        assertTrue(dst.exists(NOFOLLOW));
        assertTrue(dst.resolve("a").exists(NOFOLLOW));
    }

    public void test_renameTo_NotEmptyException() throws Exception {
        LocalResource src = resource.resolve("src");
        LocalResource dst = resource.resolve("dst");
        dst.resolve("a").createDirectories();
        src.createDirectory();
        expectOnRenameTo(NotEmptyException.class, src, dst);
    }

    public void test_renameTo_IsDirectoryException() throws Exception {
        Resource src = resource.resolve("src");
        Resource dst = resource.resolve("dst");
        src.createFile();
        dst.createDirectories();
        expectOnRenameTo(IsDirectoryException.class, src, dst);
    }

    public void test_renameTo_NotDirectoryException() throws Exception {
        Resource src = resource.resolve("src");
        Resource dst = resource.resolve("dst");
        src.createDirectory();
        dst.createFile();
        expectOnRenameTo(NotDirectoryException.class, src, dst);
    }

    public void test_renameTo_symbolicLinkToExistingFileWillOverride() throws Exception {
        Resource src = resource.resolve("src");
        Resource dst = resource.resolve("dst");
        src.createSymbolicLink(dst);
        dst.createFile();
        src.renameTo(dst);
    }

    public void test_renameTo_AccessException() throws Exception {
        LocalResource src = resource.resolve("src");
        LocalResource dst = resource.resolve("dst");
        src.createFile();
        dst.createDirectory();
        assertTrue(dst.getFile().setWritable(false));
        expectOnRenameTo(AccessException.class, src, dst.resolve("a"));
    }

    public void test_renameTo_NotExistException() throws Exception {
        Resource src = resource.resolve("src");
        Resource dst = resource.resolve("dst");
        expectOnRenameTo(NotExistException.class, src, dst);
    }

    public void test_renameTo_InvalidException() throws Exception {
        Resource parent = resource.resolve("parent");
        Resource child = parent.resolve("child");
        child.createDirectories();
        expectOnRenameTo(InvalidException.class, parent, child);
    }

    public void test_renameTo_CrossDeviceException() throws Exception {
        /*
         * This test assumes:
         *
         *  - /storage/emulated/0
         *  - /storage/emulated/legacy
         *
         * exist on the devices and are mounted on different file systems.
         *
         * Meaning this test will fail if that's no true, such as when testing
         * on the emulator.
         */
        String srcPath = "/storage/emulated/0/test-" + nanoTime();
        String dstPath = "/storage/emulated/legacy/test2-" + nanoTime();
        Resource src = LocalResource.create(new File(srcPath));
        Resource dst = LocalResource.create(new File(dstPath));
        try {

            src.createFile();
            expectOnRenameTo(CrossDeviceException.class, src, dst);

        } finally {
            if (src.exists(NOFOLLOW)) {
                src.delete();
            }
            if (dst.exists(NOFOLLOW)) {
                dst.delete();
            }
        }
    }

    private static void expectOnRenameTo(
            final Class<? extends Exception> clazz,
            final Resource src,
            final Resource dst) throws Exception {
        expect(clazz, new Code() {
            @Override
            public void run() throws Exception {
                src.renameTo(dst);
            }
        });
    }

    public void test_delete_symbolicLink() throws Exception {
        Resource link = resource.resolve("link");
        link.createSymbolicLink(resource);
        assertTrue(link.exists(NOFOLLOW));
        link.delete();
        assertFalse(link.exists(NOFOLLOW));
    }

    public void test_delete_file() throws Exception {
        Resource file = resource.resolve("file");
        file.createFile();
        assertTrue(file.exists(NOFOLLOW));
        file.delete();
        assertFalse(file.exists(NOFOLLOW));
    }

    public void test_delete_emptyDirectory() throws Exception {
        Resource directory = resource.resolve("directory");
        directory.createDirectory();
        assertTrue(directory.exists(NOFOLLOW));
        directory.delete();
        assertFalse(directory.exists(NOFOLLOW));
    }

    public void test_delete_AccessException() throws Exception {
        Resource file = resource.resolve("a");
        file.createFile();
        resource.setPermissions(Collections.<Permission>emptySet());
        expectOnDelete(AccessException.class, file);
    }

    public void test_delete_NotExistException() throws Exception {
        expectOnDelete(NotExistException.class, resource.resolve("a"));
    }

    public void test_delete_NotDirectoryException() throws Exception {
        Resource child = createChildWithNonDirectoryParent();
        expectOnDelete(NotDirectoryException.class, child);
    }

    public void test_delete_NotEmptyException() throws Exception {
        resource.resolve("a").createDirectory();
        expectOnDelete(NotEmptyException.class, resource);
    }

    private static void expectOnDelete(
            final Class<? extends Exception> clazz,
            final Resource resource) throws Exception {
        expect(clazz, new Code() {
            @Override
            public void run() throws Exception {
                resource.delete();
            }
        });
    }

    public void test_setModificationTime() throws Exception {
        Instant old = getModificationTime(resource);
        Instant expect = Instant.of(old.getSeconds() + 101, old.getNanos() - 1);
        resource.setModificationTime(expect);
        Instant actual = getModificationTime(resource);
        assertEquals(expect, actual);
    }

    public void test_setModificationTime_doesNotAffectAccessTime() throws Exception {
        Instant atime = getAccessTime(resource);
        Instant mtime = Instant.of(1, 2);
        sleep(3);
        resource.setModificationTime(mtime);
        assertNotEqual(atime, mtime);
        assertEquals(mtime, getModificationTime(resource));
        assertEquals(atime, getAccessTime(resource));
    }

    public void test_setModificationTime_doesNotAffectSymbolicLinkTarget() throws Exception {
        Resource link = resource.resolve("link");
        link.createSymbolicLink(resource);

        Instant targetTime = getModificationTime(resource);
        Instant linkTime = Instant.of(123, 456);

        link.setModificationTime(linkTime);

        assertEquals(linkTime, getModificationTime(link));
        assertEquals(targetTime, getModificationTime(resource));
        assertNotEqual(targetTime, linkTime);
    }

    public void test_setModificationTime_NotExistException() throws Exception {
        Resource doesNotExist = resource.resolve("doesNotExist");
        expectOnSetModificationTime(NotExistException.class, doesNotExist, EPOCH);
    }

    private Instant getModificationTime(Resource resource) throws IOException {
        return resource.readStatus(false).getModificationTime();
    }

    private static void expectOnSetModificationTime(
            final Class<? extends Exception> clazz,
            final Resource resource,
            final Instant instant) throws Exception {
        expect(clazz, new Code() {
            @Override
            public void run() throws Exception {
                resource.setModificationTime(instant);
            }
        });
    }

    public void test_setAccessTime() throws Exception {
        Instant old = getAccessTime(resource);
        Instant expect = Instant.of(old.getSeconds() + 101, old.getNanos() - 1);
        resource.setAccessTime(expect);
        Instant actual = getAccessTime(resource);
        assertEquals(expect, actual);
    }

    public void test_setAccessTime_doesNotAffectModificationTime() throws Exception {
        Instant mtime = getModificationTime(resource);
        Instant atime = Instant.of(1, 2);
        sleep(3);
        resource.setAccessTime(atime);
        assertNotEqual(mtime, atime);
        assertEquals(atime, getAccessTime(resource));
        assertEquals(mtime, getModificationTime(resource));
    }

    public void test_setAccessTime_doesNotAffectSymbolicLinkTarget() throws Exception {
        Resource link = resource.resolve("link");
        link.createSymbolicLink(resource);

        Instant targetTime = getAccessTime(resource);
        Instant linkTime = Instant.of(123, 456);

        link.setAccessTime(linkTime);

        assertEquals(linkTime, getAccessTime(link));
        assertEquals(targetTime, getAccessTime(resource));
        assertNotEqual(targetTime, linkTime);
    }

    public void test_setAccessTime_NotExistException() throws Exception {
        Resource doesNotExist = resource.resolve("doesNotExist");
        expectOnSetAccessTime(NotExistException.class, doesNotExist, EPOCH);
    }

    private Instant getAccessTime(Resource resource) throws IOException {
        return resource.readStatus(false).getAccessTime();
    }

    private static void expectOnSetAccessTime(
            final Class<? extends Exception> clazz,
            final Resource resource,
            final Instant instant) throws Exception {
        expect(clazz, new Code() {
            @Override
            public void run() throws Exception {
                resource.setAccessTime(instant);
            }
        });
    }

    public void test_setPermissions() throws Exception {
        Set<Permission> permissions = EnumSet.allOf(Permission.class);
        for (Set<Permission> expected : powerSet(permissions)) {
            resource.setPermissions(expected);
            Set<Permission> actual = resource.readStatus(false).getPermissions();
            assertEquals(expected, actual);
        }
    }

    public void test_setPermissions_rawBits() throws Exception {
        int expected = Os.stat(resource.getPath()).st_mode;
        resource.setPermissions(resource.readStatus(false).getPermissions());
        int actual = Os.stat(resource.getPath()).st_mode;
        assertEquals(expected, actual);
    }

    private static void expect(
            Class<? extends Exception> clazz,
            Code code) throws Exception {

        try {
            code.run();
            fail();
        } catch (Exception e) {
            if (!clazz.isInstance(e)) {
                throw e;
            }
        }

    }

    private interface Code {
        void run() throws Exception;
    }

    private Resource createChildWithNonDirectoryParent() throws IOException {
        Resource parent = resource.resolve("parent");
        Resource child = parent.resolve("child");
        parent.createFile();
        return child;
    }

}
