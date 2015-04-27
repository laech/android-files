package l.files.fs.local;

import android.system.Os;
import android.system.StructStat;

import com.google.common.io.ByteStreams;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import l.files.fs.AccessException;
import l.files.fs.CrossDeviceException;
import l.files.fs.ExistsException;
import l.files.fs.Instant;
import l.files.fs.InvalidException;
import l.files.fs.LinkOption;
import l.files.fs.NotDirectoryException;
import l.files.fs.NotEmptyException;
import l.files.fs.NotExistException;
import l.files.fs.NotFileException;
import l.files.fs.Permission;
import l.files.fs.Resource;
import l.files.fs.ResourceStatus;
import l.files.fs.ResourceVisitor;

import static android.test.MoreAsserts.assertNotEqual;
import static com.google.common.collect.Sets.powerSet;
import static java.lang.System.nanoTime;
import static java.lang.Thread.sleep;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static l.files.fs.Instant.EPOCH;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.ResourceVisitor.Result.CONTINUE;
import static l.files.fs.ResourceVisitor.Result.TERMINATE;
import static l.files.fs.local.LocalResource.mapPermissions;
import static l.files.fs.local.Stat.lstat;

public final class LocalResourceTest extends ResourceBaseTest {

    private Resource resource;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        resource = dir1();
    }

    public void test_isReadable_true() throws Exception {
        assertTrue(dir1().isReadable());
    }

    public void test_isReadable_false() throws Exception {
        dir1().setPermissions(Collections.<Permission>emptySet());
        assertTrue(dir1().isReadable());
    }

    public void test_isWritable_true() throws Exception {
        assertTrue(dir1().isWritable());
    }

    public void test_isWritable_false() throws Exception {
        dir1().setPermissions(Collections.<Permission>emptySet());
        assertFalse(dir1().isWritable());
    }

    public void test_isExecutable_true() throws Exception {
        assertTrue(dir1().isExecutable());
    }

    public void test_isExecutable_false() throws Exception {
        dir1().setPermissions(Collections.<Permission>emptySet());
        assertFalse(dir1().isExecutable());
    }

    public void test_readStatus_symbolicLink() throws Exception {
        Resource file = dir1().resolve("file").createFile();
        Resource link = dir1().resolve("link").createSymbolicLink(file);
        assertFalse(file.readStatus(NOFOLLOW).isSymbolicLink());
        assertFalse(link.readStatus(FOLLOW).isSymbolicLink());
        assertTrue(link.readStatus(NOFOLLOW).isSymbolicLink());
        assertEquals(file.readStatus(NOFOLLOW), link.readStatus(FOLLOW));
    }

    public void test_readStatus_modificationTime() throws Exception {
        ResourceStatus actual = dir1().readStatus(NOFOLLOW);
        StructStat expected = Os.stat(dir1().getPath());
        assertEquals(expected.st_mtime, actual.getModificationTime().getSeconds());
    }

    public void test_readStatus_accessTime() throws Exception {
        ResourceStatus actual = dir1().readStatus(NOFOLLOW);
        StructStat expected = Os.stat(dir1().getPath());
        assertEquals(expected.st_atime, actual.getAccessTime().getSeconds());
    }

    public void test_readStatus_size() throws Exception {
        Resource file = dir1().resolve("file").createFile();
        file.writeString(NOFOLLOW, UTF_8, "hello world");
        long expected = Os.stat(file.getPath()).st_size;
        long actual = file.readStatus(NOFOLLOW).getSize();
        assertEquals(expected, actual);
    }

    public void test_readStatus_isDirectory() throws Exception {
        assertTrue(dir1().readStatus(NOFOLLOW).isDirectory());
    }

    public void test_readStatus_isRegularFile() throws Exception {
        Resource dir = dir1().resolve("dir").createFile();
        assertTrue(dir.readStatus(NOFOLLOW).isRegularFile());
    }

    public void test_getHierarchy_single() throws Exception {
        Resource a = LocalResource.create(new File("/"));
        assertEquals(singletonList(a), a.getHierarchy());
    }

    public void test_getHierarchy_multi() throws Exception {
        Resource a = LocalResource.create(new File("/a/b"));
        List<Resource> expected = Arrays.<Resource>asList(
                LocalResource.create(new File("/")),
                LocalResource.create(new File("/a")),
                LocalResource.create(new File("/a/b"))
        );
        assertEquals(expected, a.getHierarchy());
    }

    public void test_list_earlyTermination() throws Exception {
        final Resource a = dir1().resolve("a").createFile();
        final Resource b = dir1().resolve("b").createFile();
        dir1().resolve("c").createFile();
        dir1().resolve("d").createFile();
        final List<Resource> result = new ArrayList<>();
        dir1().list(NOFOLLOW, new ResourceVisitor() {
            @Override
            public Result accept(Resource resource) throws IOException {
                result.add(resource);
                return resource.equals(b) ? TERMINATE : CONTINUE;
            }
        });
        assertEquals(asList(a, b), result);
    }

    public void test_list_AccessException() throws Exception {
        Resource dir = dir1().resolve("dir").createDirectory();
        dir1().setPermissions(Collections.<Permission>emptySet());
        expectOnList(AccessException.class, dir, NOFOLLOW);
    }

    public void test_list_NotExistsException() throws Exception {
        Resource dir = dir1().resolve("dir");
        expectOnList(NotExistException.class, dir, NOFOLLOW);
    }

    public void test_list_NotDirectoryException_file() throws Exception {
        Resource file = dir1().resolve("file").createFile();
        expectOnList(NotDirectoryException.class, file, NOFOLLOW);
    }

    public void test_list_NotDirectoryException_link() throws Exception {
        Resource dir = dir1().resolve("dir").createDirectory();
        Resource link = dir1().resolve("link").createSymbolicLink(dir);
        expectOnList(NotDirectoryException.class, link, NOFOLLOW);
    }

    public void test_list_linkFollowSuccess() throws Exception {
        Resource dir = dir1().resolve("dir").createDirectory();
        Resource a = dir.resolve("a").createFile();
        Resource b = dir.resolve("b").createDirectory();
        Resource c = dir.resolve("c").createSymbolicLink(a);
        Resource link = dir1().resolve("link").createSymbolicLink(dir);
        assertEquals(
                asList(
                        a.resolveParent(dir, link),
                        b.resolveParent(dir, link),
                        c.resolveParent(dir, link)
                ),
                link.list(FOLLOW));
    }

    public void test_list() throws Exception {
        Resource a = dir1().resolve("a").createFile();
        Resource b = dir1().resolve("b").createDirectory();
        List<?> expected = asList(a, b);
        List<?> actual = dir1().list(NOFOLLOW);
        assertEquals(expected, actual);
    }

    public void test_list_propagatesException() throws Exception {
        dir1().resolve("a").createFile();
        dir1().resolve("b").createDirectory();
        try {
            dir1().list(NOFOLLOW, new ResourceVisitor() {
                @Override
                public Result accept(Resource resource) throws IOException {
                    throw new IOException("TEST");
                }
            });
            fail();
        } catch (IOException e) {
            if (!"TEST".equals(e.getMessage())) {
                throw e;
            }
        }
    }

    private static void expectOnList(
            final Class<? extends Exception> clazz,
            final Resource resource,
            final LinkOption option) throws Exception {
        expect(clazz, new Code() {
            @Override
            public void run() throws Exception {
                resource.list(option);
            }
        });
    }

    public void test_openOutputStream_append_defaultFalse() throws Exception {
        test_openOutputStream("a", "b", "b", new OutputStreamProvider() {
            @Override
            public OutputStream open(Resource resource) throws IOException {
                return resource.openOutputStream(NOFOLLOW);
            }
        });
    }

    public void test_openOutputStream_append_false() throws Exception {
        test_openOutputStream("a", "b", "b", new OutputStreamProvider() {
            @Override
            public OutputStream open(Resource resource) throws IOException {
                return resource.openOutputStream(NOFOLLOW, false);
            }
        });
    }

    public void test_openOutputStream_append_true() throws Exception {
        test_openOutputStream("a", "b", "ab", new OutputStreamProvider() {
            @Override
            public OutputStream open(Resource resource) throws IOException {
                return resource.openOutputStream(NOFOLLOW, true);
            }
        });
    }

    private void test_openOutputStream(
            String initial,
            String write,
            String result,
            OutputStreamProvider provider) throws Exception {

        Resource file = dir1().resolve("file").createFile();
        file.writeString(NOFOLLOW, UTF_8, initial);
        try (OutputStream out = provider.open(file)) {
            out.write(write.getBytes(UTF_8));
        }
        assertEquals(result, file.readString(NOFOLLOW, UTF_8));
    }

    private interface OutputStreamProvider {
        OutputStream open(Resource resource) throws IOException;
    }

    public void test_openOutputStream_createWithCorrectPermission() throws Exception {
        Resource expected = dir1().resolve("expected");
        Resource actual = dir1().resolve("actual");

        assertTrue(new File(expected.getUri()).createNewFile());
        actual.openOutputStream(NOFOLLOW, false).close();

        assertEquals(
                Os.stat(expected.getPath()).st_mode,
                Os.stat(actual.getPath()).st_mode
        );
    }

    public void test_openOutputStream_AccessException() throws Exception {
        Resource file = dir1().resolve("file").createFile();
        dir1().setPermissions(Collections.<Permission>emptySet());
        expectOnOpenOutputStream(AccessException.class, file, NOFOLLOW, false);
    }

    public void test_openOutputStream_NotExistException() throws Exception {
        Resource file = dir1().resolve("parent/file");
        expectOnOpenOutputStream(NotExistException.class, file, NOFOLLOW, false);
    }

    public void test_openOutputStream_NotFileException_directory() throws Exception {
        expectOnOpenOutputStream(NotFileException.class, dir1(), NOFOLLOW, false);
    }

    public void test_openOutputStream_NotFileException_link() throws Exception {
        Resource target = dir1().resolve("target").createFile();
        Resource link = dir1().resolve("link").createSymbolicLink(target);
        expectOnOpenOutputStream(NotFileException.class, link, NOFOLLOW, false);
    }

    private static void expectOnOpenOutputStream(
            final Class<? extends Exception> clazz,
            final Resource resource,
            final LinkOption option,
            final boolean append) throws Exception {
        expect(clazz, new Code() {
            @Override
            public void run() throws Exception {
                resource.openOutputStream(option, append).close();
            }
        });
    }

    public void test_openInputStream() throws Exception {
        Resource file = dir1().resolve("a").createFile();
        String expected = "hello\nworld\n";
        file.writeString(NOFOLLOW, UTF_8, expected);
        try (InputStream in = file.openInputStream(NOFOLLOW)) {
            String actual = new String(ByteStreams.toByteArray(in), UTF_8);
            assertEquals(expected, actual);
        }
    }

    public void test_openInputStream_linkFollowSuccess() throws Exception {
        Resource target = dir1().resolve("target").createFile();
        Resource link = dir1().resolve("link").createSymbolicLink(target);
        link.openInputStream(FOLLOW).close();
    }

    public void test_openInputStream_NotFileException_link() throws Exception {
        final Resource target = dir1().resolve("target").createFile();
        final Resource link = dir1().resolve("link").createSymbolicLink(target);
        expectOnOpenInputStream(NotFileException.class, link, NOFOLLOW);
    }

    public void test_openInputStream_NotFileException_directory() throws Exception {
        expectOnOpenInputStream(NotFileException.class, dir1(), NOFOLLOW);
    }

    public void test_openInputStream_AccessException() throws Exception {
        Resource file = dir1().resolve("a").createFile();
        dir1().setPermissions(Collections.<Permission>emptySet());
        expectOnOpenInputStream(AccessException.class, file, NOFOLLOW);
    }

    public void test_openInputStream_NotExistException() throws Exception {
        Resource file = dir1().resolve("a");
        expectOnOpenInputStream(NotExistException.class, file, NOFOLLOW);
    }

    private static void expectOnOpenInputStream(
            final Class<? extends Exception> clazz,
            final Resource resource,
            final LinkOption option) throws Exception {
        expect(clazz, new Code() {
            @Override
            public void run() throws Exception {
                resource.openInputStream(option).close();
            }
        });
    }

    public void test_exists_true() throws Exception {
        assertTrue(dir1().exists(NOFOLLOW));
    }

    public void test_exists_false() throws Exception {
        assertFalse(dir1().resolve("a").exists(NOFOLLOW));
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
        file.writeString(NOFOLLOW, UTF_8, expected);
        assertEquals(expected, file.readString(NOFOLLOW, UTF_8));
    }

    public void test_createFile() throws Exception {
        Resource file = resource.resolve("a");
        file.createFile();
        assertTrue(file.readStatus(NOFOLLOW).isRegularFile());
    }

    public void test_createFile_correctPermissions() throws Exception {
        Resource actual = resource.resolve("a");
        actual.createFile();

        File expected = new File(resource.getPath(), "b");
        assertTrue(expected.createNewFile());

        assertEquals(expected.canRead(), actual.isReadable());
        assertEquals(expected.canWrite(), actual.isWritable());
        assertEquals(expected.canExecute(), actual.isExecutable());
        assertEquals(
                mapPermissions(lstat(expected.getPath()).getMode()),
                actual.readStatus(NOFOLLOW).getPermissions()
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
        assertTrue(dir.readStatus(NOFOLLOW).isDirectory());
    }

    public void test_createDirectory_correctPermissions() throws Exception {
        Resource actual = resource.resolve("a");
        actual.createDirectory();

        File expected = new File(resource.getPath(), "b");
        assertTrue(expected.mkdir());

        assertEquals(expected.canRead(), actual.isReadable());
        assertEquals(expected.canWrite(), actual.isWritable());
        assertEquals(expected.canExecute(), actual.isExecutable());
        assertEquals(
                mapPermissions(lstat(expected.getPath()).getMode()),
                actual.readStatus(NOFOLLOW).getPermissions()
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
        assertTrue(resource.resolve("a/b/c").readStatus(NOFOLLOW).isDirectory());
        assertTrue(resource.resolve("a/b").readStatus(NOFOLLOW).isDirectory());
        assertTrue(resource.resolve("a/").readStatus(NOFOLLOW).isDirectory());
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
        assertTrue(link.readStatus(NOFOLLOW).isSymbolicLink());
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
        Resource child = resource.resolve("a").createSymbolicLink(resource);
        ResourceStatus expected = resource.readStatus(NOFOLLOW);
        ResourceStatus actual = child.readStatus(FOLLOW);
        assertTrue(actual.isDirectory());
        assertFalse(actual.isSymbolicLink());
        assertEquals(expected, actual);
    }

    public void test_readStatus_noFollowLink() throws Exception {
        Resource child = resource.resolve("a").createSymbolicLink(resource);
        ResourceStatus actual = child.readStatus(NOFOLLOW);
        assertTrue(actual.isSymbolicLink());
        assertFalse(actual.isDirectory());
        assertNotEqual(resource.readStatus(NOFOLLOW), actual);
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
                resource.readStatus(NOFOLLOW);
            }
        });
    }

    public void test_moveTo_ExistsException() throws Exception {
        Resource src = resource.resolve("src");
        Resource dst = resource.resolve("dst");
        src.writeString(NOFOLLOW, UTF_8, "src");
        dst.writeString(NOFOLLOW, UTF_8, "dst");
        expectOnMoveTo(ExistsException.class, src, dst);
        assertTrue(src.exists(NOFOLLOW));
        assertTrue(dst.exists(NOFOLLOW));
        assertEquals("src", src.readString(NOFOLLOW, UTF_8));
        assertEquals("dst", dst.readString(NOFOLLOW, UTF_8));
    }

    public void test_moveTo_moveLinkNotTarget() throws Exception {
        Resource target = resource.resolve("target").createFile();
        Resource src = resource.resolve("src").createSymbolicLink(target);
        Resource dst = resource.resolve("dst");
        src.moveTo(dst);
        assertFalse(src.exists(NOFOLLOW));
        assertTrue(dst.exists(NOFOLLOW));
        assertTrue(target.exists(NOFOLLOW));
        assertEquals(target, dst.readSymbolicLink());
    }

    public void test_moveTo_fileToNonExistingFile() throws Exception {
        Resource src = resource.resolve("src");
        Resource dst = resource.resolve("dst");
        src.writeString(NOFOLLOW, UTF_8, "src");
        src.moveTo(dst);
        assertFalse(src.exists(NOFOLLOW));
        assertTrue(dst.exists(NOFOLLOW));
        assertEquals("src", dst.readString(NOFOLLOW, UTF_8));
    }

    public void test_moveTo_directoryToNonExistingDirectory() throws Exception {
        Resource src = resource.resolve("src");
        Resource dst = resource.resolve("dst");
        src.resolve("a").createDirectories();
        src.moveTo(dst);
        assertFalse(src.exists(NOFOLLOW));
        assertTrue(dst.exists(NOFOLLOW));
        assertTrue(dst.resolve("a").exists(NOFOLLOW));
    }

    public void test_moveTo_AccessException() throws Exception {
        Resource src = resource.resolve("src").createFile();
        Resource dst = resource.resolve("dst").createDirectory();
        dst.setPermissions(Collections.<Permission>emptySet());
        expectOnMoveTo(AccessException.class, src, dst.resolve("a"));
    }

    public void test_moveTo_NotExistException() throws Exception {
        Resource src = resource.resolve("src");
        Resource dst = resource.resolve("dst");
        expectOnMoveTo(NotExistException.class, src, dst);
    }

    public void test_moveTo_InvalidException() throws Exception {
        Resource parent = resource.resolve("parent").createDirectory();
        Resource child = parent.resolve("child");
        expectOnMoveTo(InvalidException.class, parent, child);
    }

    public void test_moveTo_CrossDeviceException() throws Exception {
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
            expectOnMoveTo(CrossDeviceException.class, src, dst);

        } finally {
            if (src.exists(NOFOLLOW)) {
                src.delete();
            }
            if (dst.exists(NOFOLLOW)) {
                dst.delete();
            }
        }
    }

    private static void expectOnMoveTo(
            final Class<? extends Exception> clazz,
            final Resource src,
            final Resource dst) throws Exception {
        expect(clazz, new Code() {
            @Override
            public void run() throws Exception {
                src.moveTo(dst);
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
        Instant old = getModificationTime(resource, NOFOLLOW);
        Instant expect = Instant.of(old.getSeconds() + 101, old.getNanos() - 1);
        resource.setModificationTime(NOFOLLOW, expect);
        Instant actual = getModificationTime(resource, NOFOLLOW);
        assertEquals(expect, actual);
    }

    public void test_setModificationTime_doesNotAffectAccessTime() throws Exception {
        Instant atime = getAccessTime(resource, NOFOLLOW);
        Instant mtime = Instant.of(1, 2);
        sleep(3);
        resource.setModificationTime(NOFOLLOW, mtime);
        assertNotEqual(atime, mtime);
        assertEquals(mtime, getModificationTime(resource, NOFOLLOW));
        assertEquals(atime, getAccessTime(resource, NOFOLLOW));
    }

    public void test_setModificationTime_linkFollow() throws Exception {
        Resource file = resource.resolve("file").createFile();
        Resource link = resource.resolve("link").createSymbolicLink(file);

        Instant fileTime = Instant.of(123, 456);
        Instant linkTime = getModificationTime(link, NOFOLLOW);
        link.setModificationTime(FOLLOW, fileTime);

        assertEquals(fileTime, getModificationTime(file, NOFOLLOW));
        assertEquals(linkTime, getModificationTime(link, NOFOLLOW));
        assertNotEqual(fileTime, linkTime);
    }

    public void test_setModificationTime_linkNoFollow() throws Exception {
        Resource file = resource.resolve("file").createFile();
        Resource link = resource.resolve("link").createSymbolicLink(file);

        Instant fileTime = getModificationTime(file, NOFOLLOW);
        Instant linkTime = Instant.of(123, 456);

        link.setModificationTime(NOFOLLOW, linkTime);

        assertEquals(linkTime, getModificationTime(link, NOFOLLOW));
        assertEquals(fileTime, getModificationTime(file, NOFOLLOW));
        assertNotEqual(fileTime, linkTime);
    }

    public void test_setModificationTime_NotExistException() throws Exception {
        Resource doesNotExist = resource.resolve("doesNotExist");
        expectOnSetModificationTime(NotExistException.class, doesNotExist, NOFOLLOW, EPOCH);
    }

    private Instant getModificationTime(
            Resource resource,
            LinkOption option) throws IOException {
        return resource.readStatus(option).getModificationTime();
    }

    private static void expectOnSetModificationTime(
            final Class<? extends Exception> clazz,
            final Resource resource,
            final LinkOption option,
            final Instant instant) throws Exception {
        expect(clazz, new Code() {
            @Override
            public void run() throws Exception {
                resource.setModificationTime(option, instant);
            }
        });
    }

    public void test_setAccessTime() throws Exception {
        Instant old = getAccessTime(resource, NOFOLLOW);
        Instant expect = Instant.of(old.getSeconds() + 101, old.getNanos() - 1);
        resource.setAccessTime(NOFOLLOW, expect);
        Instant actual = getAccessTime(resource, NOFOLLOW);
        assertEquals(expect, actual);
    }

    public void test_setAccessTime_doesNotAffectModificationTime() throws Exception {
        Instant mtime = getModificationTime(resource, NOFOLLOW);
        Instant atime = Instant.of(1, 2);
        sleep(3);
        resource.setAccessTime(NOFOLLOW, atime);
        assertNotEqual(mtime, atime);
        assertEquals(atime, getAccessTime(resource, NOFOLLOW));
        assertEquals(mtime, getModificationTime(resource, NOFOLLOW));
    }

    public void test_setAccessTime_linkNoFollow() throws Exception {
        Resource link = dir1().resolve("link").createSymbolicLink(dir1());

        Instant targetTime = getAccessTime(dir1(), NOFOLLOW);
        Instant linkTime = Instant.of(123, 456);

        link.setAccessTime(NOFOLLOW, linkTime);

        assertEquals(linkTime, getAccessTime(link, NOFOLLOW));
        assertEquals(targetTime, getAccessTime(dir1(), NOFOLLOW));
        assertNotEqual(targetTime, linkTime);
    }

    public void test_setAccessTime_linkFollow() throws Exception {
        Resource link = dir1().resolve("link").createSymbolicLink(dir1());

        Instant linkTime = getAccessTime(dir1(), NOFOLLOW);
        Instant fileTime = Instant.of(123, 456);

        link.setAccessTime(FOLLOW, fileTime);

        assertEquals(linkTime, getAccessTime(link, NOFOLLOW));
        assertEquals(fileTime, getAccessTime(dir1(), NOFOLLOW));
        assertNotEqual(fileTime, linkTime);
    }

    public void test_setAccessTime_NotExistException() throws Exception {
        Resource doesNotExist = resource.resolve("doesNotExist");
        expectOnSetAccessTime(NotExistException.class, doesNotExist, NOFOLLOW, EPOCH);
    }

    private Instant getAccessTime(
            Resource resource,
            LinkOption option) throws IOException {
        return resource.readStatus(option).getAccessTime();
    }

    private static void expectOnSetAccessTime(
            final Class<? extends Exception> clazz,
            final Resource resource,
            final LinkOption option,
            final Instant instant) throws Exception {
        expect(clazz, new Code() {
            @Override
            public void run() throws Exception {
                resource.setAccessTime(option, instant);
            }
        });
    }

    public void test_setPermissions() throws Exception {
        Set<Permission> permissions = EnumSet.allOf(Permission.class);
        for (Set<Permission> expected : powerSet(permissions)) {
            resource.setPermissions(expected);
            Set<Permission> actual = resource.readStatus(NOFOLLOW).getPermissions();
            assertEquals(expected, actual);
        }
    }

    public void test_setPermissions_rawBits() throws Exception {
        int expected = Os.stat(resource.getPath()).st_mode;
        resource.setPermissions(resource.readStatus(NOFOLLOW).getPermissions());
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
