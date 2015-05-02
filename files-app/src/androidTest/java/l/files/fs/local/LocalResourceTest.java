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
import java.util.HashSet;
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
import static com.google.common.io.ByteStreams.toByteArray;
import static java.lang.System.nanoTime;
import static java.lang.Thread.sleep;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableSet;
import static java.util.EnumSet.allOf;
import static l.files.fs.Instant.EPOCH;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Permission.OWNER_READ;
import static l.files.fs.ResourceVisitor.Result.CONTINUE;
import static l.files.fs.ResourceVisitor.Result.TERMINATE;
import static l.files.fs.local.LocalResource.mapPermissions;
import static l.files.fs.local.Stat.lstat;

public final class LocalResourceTest extends ResourceBaseTest
{
    public void test_isReadable_true() throws Exception
    {
        assertTrue(dir1().isReadable());
    }

    public void test_isReadable_false() throws Exception
    {
        dir1().removePermissions(Permission.allRead());
        assertFalse(dir1().isReadable());
    }

    public void test_isWritable_true() throws Exception
    {
        assertTrue(dir1().isWritable());
    }

    public void test_isWritable_false() throws Exception
    {
        dir1().removePermissions(Permission.allWrite());
        assertFalse(dir1().isWritable());
    }

    public void test_isExecutable_true() throws Exception
    {
        assertTrue(dir1().isExecutable());
    }

    public void test_isExecutable_false() throws Exception
    {
        dir1().removePermissions(Permission.allExecute());
        assertFalse(dir1().isExecutable());
    }

    public void test_readStatus_permissions_unmodifiable() throws Exception
    {
        final Set<Permission> perms = dir1().readStatus(NOFOLLOW).getPermissions();
        try
        {
            perms.clear();
            fail();
        }
        catch (final UnsupportedOperationException e)
        {
            // Pass
        }
    }

    public void test_readStatus_symbolicLink() throws Exception
    {
        final Resource file = dir1().resolve("file").createFile();
        final Resource link = dir1().resolve("link").createSymbolicLink(file);
        assertFalse(file.readStatus(NOFOLLOW).isSymbolicLink());
        assertFalse(link.readStatus(FOLLOW).isSymbolicLink());
        assertTrue(link.readStatus(NOFOLLOW).isSymbolicLink());
        assertEquals(file.readStatus(NOFOLLOW), link.readStatus(FOLLOW));
    }

    public void test_readStatus_modificationTime() throws Exception
    {
        final ResourceStatus status = dir1().readStatus(NOFOLLOW);
        final long actual = status.getModificationTime().getSeconds();
        final long expected = Os.stat(dir1().getPath()).st_atime;
        assertEquals(expected, actual);
    }

    public void test_readStatus_accessTime() throws Exception
    {
        final ResourceStatus actual = dir1().readStatus(NOFOLLOW);
        final StructStat expected = Os.stat(dir1().getPath());
        assertEquals(expected.st_atime, actual.getAccessTime().getSeconds());
    }

    public void test_readStatus_size() throws Exception
    {
        final Resource file = dir1().resolve("file").createFile();
        file.writeString(NOFOLLOW, UTF_8, "hello world");
        final long expected = Os.stat(file.getPath()).st_size;
        final long actual = file.readStatus(NOFOLLOW).getSize();
        assertEquals(expected, actual);
    }

    public void test_readStatus_isDirectory() throws Exception
    {
        assertTrue(dir1().readStatus(NOFOLLOW).isDirectory());
    }

    public void test_readStatus_isRegularFile() throws Exception
    {
        final Resource dir = dir1().resolve("dir").createFile();
        assertTrue(dir.readStatus(NOFOLLOW).isRegularFile());
    }

    public void test_getHierarchy_single() throws Exception
    {
        final Resource a = LocalResource.create(new File("/"));
        assertEquals(singletonList(a), a.getHierarchy());
    }

    public void test_getHierarchy_multi() throws Exception
    {
        final Resource a = LocalResource.create(new File("/a/b"));
        final List<Resource> expected = Arrays.<Resource>asList(
                LocalResource.create(new File("/")),
                LocalResource.create(new File("/a")),
                LocalResource.create(new File("/a/b"))
        );
        assertEquals(expected, a.getHierarchy());
    }

    public void test_list_earlyTermination() throws Exception
    {
        final Resource a = dir1().resolve("a").createFile();
        final Resource b = dir1().resolve("b").createFile();
        dir1().resolve("c").createFile();
        dir1().resolve("d").createFile();
        final List<Resource> result = new ArrayList<>();
        dir1().list(NOFOLLOW, new ResourceVisitor()
        {
            @Override
            public Result accept(final Resource resource) throws IOException
            {
                result.add(resource);
                return resource.equals(b) ? TERMINATE : CONTINUE;
            }
        });
        assertEquals(asList(a, b), result);
    }

    public void test_list_AccessException() throws Exception
    {
        final Resource dir = dir1().resolve("dir").createDirectory();
        dir1().setPermissions(Collections.<Permission>emptySet());
        expectOnList(AccessException.class, dir, NOFOLLOW);
    }

    public void test_list_NotExistsException() throws Exception
    {
        final Resource dir = dir1().resolve("dir");
        expectOnList(NotExistException.class, dir, NOFOLLOW);
    }

    public void test_list_NotDirectoryException_file() throws Exception
    {
        final Resource file = dir1().resolve("file").createFile();
        expectOnList(NotDirectoryException.class, file, NOFOLLOW);
    }

    public void test_list_NotDirectoryException_link() throws Exception
    {
        final Resource dir = dir1().resolve("dir").createDirectory();
        final Resource link = dir1().resolve("link").createSymbolicLink(dir);
        expectOnList(NotDirectoryException.class, link, NOFOLLOW);
    }

    public void test_list_linkFollowSuccess() throws Exception
    {
        final Resource dir = dir1().resolve("dir").createDirectory();
        final Resource a = dir.resolve("a").createFile();
        final Resource b = dir.resolve("b").createDirectory();
        final Resource c = dir.resolve("c").createSymbolicLink(a);
        final Resource link = dir1().resolve("link").createSymbolicLink(dir);

        final List<Resource> expected = asList(
                a.resolveParent(dir, link),
                b.resolveParent(dir, link),
                c.resolveParent(dir, link)
        );
        final List<Resource> actual = link.list(FOLLOW);

        assertEquals(expected, actual);
    }

    public void test_list() throws Exception
    {
        final Resource a = dir1().resolve("a").createFile();
        final Resource b = dir1().resolve("b").createDirectory();
        final List<?> expected = asList(a, b);
        final List<?> actual = dir1().list(NOFOLLOW);
        assertEquals(expected, actual);
    }

    public void test_list_propagatesException() throws Exception
    {
        dir1().resolve("a").createFile();
        dir1().resolve("b").createDirectory();
        try
        {
            dir1().list(NOFOLLOW, new ResourceVisitor()
            {
                @Override
                public Result accept(final Resource res) throws IOException
                {
                    throw new IOException("TEST");
                }
            });
            fail();
        }
        catch (final IOException e)
        {
            if (!"TEST".equals(e.getMessage()))
            {
                throw e;
            }
        }
    }

    private static void expectOnList(
            final Class<? extends Exception> clazz,
            final Resource resource,
            final LinkOption option) throws Exception
    {
        expect(clazz, new Code()
        {
            @Override
            public void run() throws Exception
            {
                resource.list(option);
            }
        });
    }

    public void test_openOutputStream_append_defaultFalse() throws Exception
    {
        test_openOutputStream("a", "b", "b", new OutputStreamProvider()
        {
            @Override
            public OutputStream open(final Resource res) throws IOException
            {
                return res.openOutputStream(NOFOLLOW);
            }
        });
    }

    public void test_openOutputStream_append_false() throws Exception
    {
        test_openOutputStream("a", "b", "b", new OutputStreamProvider()
        {
            @Override
            public OutputStream open(final Resource res) throws IOException
            {
                return res.openOutputStream(NOFOLLOW, false);
            }
        });
    }

    public void test_openOutputStream_append_true() throws Exception
    {
        test_openOutputStream("a", "b", "ab", new OutputStreamProvider()
        {
            @Override
            public OutputStream open(final Resource res) throws IOException
            {
                return res.openOutputStream(NOFOLLOW, true);
            }
        });
    }

    private void test_openOutputStream(
            final String initial,
            final String write,
            final String result,
            final OutputStreamProvider provider) throws Exception
    {

        final Resource file = dir1().resolve("file").createFile();
        file.writeString(NOFOLLOW, UTF_8, initial);
        try (OutputStream out = provider.open(file))
        {
            out.write(write.getBytes(UTF_8));
        }
        assertEquals(result, file.readString(NOFOLLOW, UTF_8));
    }

    private interface OutputStreamProvider
    {
        OutputStream open(Resource resource) throws IOException;
    }

    public void test_openOutputStream_createWithCorrectPermission()
            throws Exception
    {
        final Resource expected = dir1().resolve("expected");
        final Resource actual = dir1().resolve("actual");

        assertTrue(new File(expected.getUri()).createNewFile());
        actual.openOutputStream(NOFOLLOW, false).close();

        assertEquals(
                Os.stat(expected.getPath()).st_mode,
                Os.stat(actual.getPath()).st_mode
        );
    }

    public void test_openOutputStream_AccessException() throws Exception
    {
        final Resource file = dir1().resolve("file").createFile();
        dir1().setPermissions(Collections.<Permission>emptySet());
        expectOnOpenOutputStream(AccessException.class, file, NOFOLLOW, false);
    }

    public void test_openOutputStream_NotExistException() throws Exception
    {
        final Resource file = dir1().resolve("parent/file");
        expectOnOpenOutputStream(
                NotExistException.class, file, NOFOLLOW, false);
    }

    public void test_openOutputStream_NotFileException_directory()
            throws Exception
    {
        expectOnOpenOutputStream(
                NotFileException.class, dir1(), NOFOLLOW, false);
    }

    public void test_openOutputStream_NotFileException_link() throws Exception
    {
        final Resource target = dir1().resolve("target").createFile();
        final Resource link = dir1().resolve("link").createSymbolicLink(target);
        expectOnOpenOutputStream(NotFileException.class, link, NOFOLLOW, false);
    }

    private static void expectOnOpenOutputStream(
            final Class<? extends Exception> clazz,
            final Resource resource,
            final LinkOption option,
            final boolean append) throws Exception
    {
        expect(clazz, new Code()
        {
            @Override
            public void run() throws Exception
            {
                resource.openOutputStream(option, append).close();
            }
        });
    }

    public void test_openInputStream() throws Exception
    {
        final Resource file = dir1().resolve("a").createFile();
        final String expected = "hello\nworld\n";
        file.writeString(NOFOLLOW, UTF_8, expected);
        try (final InputStream in = file.openInputStream(NOFOLLOW))
        {
            final String actual = new String(toByteArray(in), UTF_8);
            assertEquals(expected, actual);
        }
    }

    public void test_openInputStream_linkFollowSuccess() throws Exception
    {
        final Resource target = dir1().resolve("target").createFile();
        final Resource link = dir1().resolve("link").createSymbolicLink(target);
        link.openInputStream(FOLLOW).close();
    }

    public void test_openInputStream_NotFileException_link() throws Exception
    {
        final Resource target = dir1().resolve("target").createFile();
        final Resource link = dir1().resolve("link").createSymbolicLink(target);
        expectOnOpenInputStream(NotFileException.class, link, NOFOLLOW);
    }

    public void test_openInputStream_NotFileException_directory()
            throws Exception
    {
        expectOnOpenInputStream(NotFileException.class, dir1(), NOFOLLOW);
    }

    public void test_openInputStream_AccessException() throws Exception
    {
        final Resource file = dir1().resolve("a").createFile();
        dir1().setPermissions(Collections.<Permission>emptySet());
        expectOnOpenInputStream(AccessException.class, file, NOFOLLOW);
    }

    public void test_openInputStream_NotExistException() throws Exception
    {
        final Resource file = dir1().resolve("a");
        expectOnOpenInputStream(NotExistException.class, file, NOFOLLOW);
    }

    private static void expectOnOpenInputStream(
            final Class<? extends Exception> clazz,
            final Resource resource,
            final LinkOption option) throws Exception
    {
        expect(clazz, new Code()
        {
            @Override
            public void run() throws Exception
            {
                resource.openInputStream(option).close();
            }
        });
    }

    public void test_exists_true() throws Exception
    {
        assertTrue(dir1().exists(NOFOLLOW));
    }

    public void test_exists_false() throws Exception
    {
        assertFalse(dir1().resolve("a").exists(NOFOLLOW));
    }

    public void test_exists_checkLinkNotTarget() throws Exception
    {
        final Resource target = dir1().resolve("target");
        final Resource link = dir1().resolve("link").createSymbolicLink(target);
        assertFalse(target.exists(NOFOLLOW));
        assertFalse(link.exists(FOLLOW));
        assertTrue(link.exists(NOFOLLOW));
    }

    public void test_readString() throws Exception
    {
        final Resource file = dir1().resolve("file").createFile();
        final String expected = "a\nb\tc";
        file.writeString(NOFOLLOW, UTF_8, expected);
        assertEquals(expected, file.readString(NOFOLLOW, UTF_8));
    }

    public void test_createFile() throws Exception
    {
        final Resource file = dir1().resolve("a");
        file.createFile();
        assertTrue(file.readStatus(NOFOLLOW).isRegularFile());
    }

    public void test_createFile_correctPermissions() throws Exception
    {
        final Resource actual = dir1().resolve("a");
        actual.createFile();

        final File expected = new File(dir1().getPath(), "b");
        assertTrue(expected.createNewFile());

        assertEquals(expected.canRead(), actual.isReadable());
        assertEquals(expected.canWrite(), actual.isWritable());
        assertEquals(expected.canExecute(), actual.isExecutable());
        assertEquals(
                mapPermissions(lstat(expected.getPath()).getMode()),
                actual.readStatus(NOFOLLOW).getPermissions()
        );
    }

    public void test_createFile_AccessException() throws Exception
    {
        dir1().setPermissions(Collections.<Permission>emptySet());
        expectOnCreateFile(AccessException.class, dir1().resolve("a"));
    }

    public void test_createFile_ExistsException() throws Exception
    {
        final Resource child = dir1().resolve("a");
        child.createFile();
        expectOnCreateFile(ExistsException.class, child);
    }

    public void test_createFile_NotExistException() throws Exception
    {
        expectOnCreateFile(NotExistException.class, dir1().resolve("a/b"));
    }

    public void test_createFile_NotDirectoryException() throws Exception
    {
        final Resource child = createChildWithNonDirectoryParent();
        expectOnCreateFile(NotDirectoryException.class, child);
    }

    private static void expectOnCreateFile(
            final Class<? extends Exception> clazz,
            final Resource resource) throws Exception
    {
        expect(clazz, new Code()
        {
            @Override
            public void run() throws Exception
            {
                resource.createFile();
            }
        });
    }

    public void test_createDirectory() throws Exception
    {
        final Resource dir = dir1().resolve("a");
        dir.createDirectory();
        assertTrue(dir.readStatus(NOFOLLOW).isDirectory());
    }

    public void test_createDirectory_correctPermissions() throws Exception
    {
        final Resource actual = dir1().resolve("a");
        actual.createDirectory();

        final File expected = new File(dir1().getPath(), "b");
        assertTrue(expected.mkdir());

        assertEquals(expected.canRead(), actual.isReadable());
        assertEquals(expected.canWrite(), actual.isWritable());
        assertEquals(expected.canExecute(), actual.isExecutable());
        assertEquals(
                mapPermissions(lstat(expected.getPath()).getMode()),
                actual.readStatus(NOFOLLOW).getPermissions()
        );
    }

    public void test_createDirectory_AccessException() throws Exception
    {
        dir1().setPermissions(Collections.<Permission>emptySet());
        final Resource dir = dir1().resolve("a");
        expectOnCreateDirectory(AccessException.class, dir);
    }

    public void test_createDirectory_ExistsException() throws Exception
    {
        expectOnCreateDirectory(ExistsException.class, dir1());
    }

    public void test_createDirectory_NotFoundException() throws Exception
    {
        final Resource dir = dir1().resolve("a/b");
        expectOnCreateDirectory(NotExistException.class, dir);
    }

    public void test_createDirectory_NotDirectoryException() throws Exception
    {
        final Resource child = createChildWithNonDirectoryParent();
        expectOnCreateDirectory(NotDirectoryException.class, child);
    }

    private static void expectOnCreateDirectory(
            final Class<? extends Exception> clazz,
            final Resource resource) throws Exception
    {
        expect(clazz, new Code()
        {
            @Override
            public void run() throws Exception
            {
                resource.createDirectory();
            }
        });
    }

    public void test_createDirectories() throws Exception
    {
        dir1().resolve("a/b/c").createDirectories();
        assertTrue(dir1().resolve("a/b/c").readStatus(NOFOLLOW).isDirectory());
        assertTrue(dir1().resolve("a/b").readStatus(NOFOLLOW).isDirectory());
        assertTrue(dir1().resolve("a/").readStatus(NOFOLLOW).isDirectory());
    }

    public void test_createDirectories_NotDirectoryException() throws Exception
    {
        final Resource parent = dir1().resolve("a");
        final Resource child = parent.resolve("b");
        parent.createFile();
        expectOnCreateDirectories(NotDirectoryException.class, child);
    }

    private static void expectOnCreateDirectories(
            final Class<? extends Exception> clazz,
            final Resource resource) throws Exception
    {
        expect(clazz, new Code()
        {
            @Override
            public void run() throws Exception
            {
                resource.createDirectories();
            }
        });
    }

    public void test_createSymbolicLink() throws Exception
    {
        final Resource link = dir1().resolve("link");
        link.createSymbolicLink(dir1());
        assertTrue(link.readStatus(NOFOLLOW).isSymbolicLink());
        assertEquals(dir1(), link.readSymbolicLink());
    }

    public void test_createSymbolicLink_AccessException() throws Exception
    {
        dir1().setPermissions(Collections.<Permission>emptySet());
        final Resource link = dir1().resolve("a");
        expectOnCreateSymbolicLink(AccessException.class, link, dir1());
    }

    public void test_createSymbolicLink_ExistsException() throws Exception
    {
        final Resource link = dir1().resolve("a");
        link.createFile();
        expectOnCreateSymbolicLink(ExistsException.class, link, dir1());
    }

    public void test_createSymbolicLink_NotExistException() throws Exception
    {
        final Resource link = dir1().resolve("a/b");
        expectOnCreateSymbolicLink(NotExistException.class, link, dir1());
    }

    public void test_createSymbolicLink_NotDirectoryException() throws Exception
    {
        final Resource parent = dir1().resolve("parent");
        final Resource link = parent.resolve("link");
        parent.createFile();
        expectOnCreateSymbolicLink(NotDirectoryException.class, link, dir1());
    }

    private static void expectOnCreateSymbolicLink(
            final Class<? extends Exception> clazz,
            final Resource link,
            final Resource target) throws Exception
    {
        expect(clazz, new Code()
        {
            @Override
            public void run() throws Exception
            {
                link.createSymbolicLink(target);
            }
        });
    }

    public void test_readSymbolicLink_AccessException() throws Exception
    {
        dir1().setPermissions(Collections.<Permission>emptySet());
        final Resource link = dir1().resolve("a");
        expectOnReadSymbolicLink(AccessException.class, link);
    }

    public void test_readSymbolicLink_InvalidException() throws Exception
    {
        final Resource notLink = dir1().resolve("notLink");
        notLink.createFile();
        expectOnReadSymbolicLink(InvalidException.class, notLink);
    }

    public void test_readSymbolicLink_NotExistException() throws Exception
    {
        final Resource link = dir1().resolve("a");
        expectOnReadSymbolicLink(NotExistException.class, link);
    }

    public void test_readSymbolicLink_NotDirectoryException() throws Exception
    {
        final Resource parent = dir1().resolve("parent");
        final Resource link = parent.resolve("link");
        parent.createFile();
        expectOnReadSymbolicLink(NotDirectoryException.class, link);
    }

    private static void expectOnReadSymbolicLink(
            final Class<? extends Exception> clazz,
            final Resource link) throws Exception
    {
        expect(clazz, new Code()
        {
            @Override
            public void run() throws Exception
            {
                link.readSymbolicLink();
            }
        });
    }

    public void test_readStatus_followLink() throws Exception
    {
        final Resource child = dir1().resolve("a").createSymbolicLink(dir1());
        final ResourceStatus expected = dir1().readStatus(NOFOLLOW);
        final ResourceStatus actual = child.readStatus(FOLLOW);
        assertTrue(actual.isDirectory());
        assertFalse(actual.isSymbolicLink());
        assertEquals(expected, actual);
    }

    public void test_readStatus_noFollowLink() throws Exception
    {
        final Resource child = dir1().resolve("a").createSymbolicLink(dir1());
        final ResourceStatus actual = child.readStatus(NOFOLLOW);
        assertTrue(actual.isSymbolicLink());
        assertFalse(actual.isDirectory());
        assertNotEqual(dir1().readStatus(NOFOLLOW), actual);
    }

    public void test_readStatus_AccessException() throws Exception
    {
        dir1().setPermissions(Collections.<Permission>emptySet());
        final Resource child = dir1().resolve("a");
        expectOnReadStatus(AccessException.class, child);
    }

    public void test_readStatus_NotExistException() throws Exception
    {
        final Resource child = dir1().resolve("a/b");
        expectOnReadStatus(NotExistException.class, child);
    }

    public void test_readStatus_NotDirectoryException() throws Exception
    {
        final Resource parent = dir1().resolve("a");
        final Resource child = parent.resolve("b");
        parent.createFile();
        expectOnReadStatus(NotDirectoryException.class, child);
    }

    private static void expectOnReadStatus(
            final Class<? extends Exception> clazz,
            final Resource resource) throws Exception
    {
        expect(clazz, new Code()
        {
            @Override
            public void run() throws Exception
            {
                resource.readStatus(NOFOLLOW);
            }
        });
    }

    public void test_moveTo_ExistsException() throws Exception
    {
        final Resource src = dir1().resolve("src");
        final Resource dst = dir1().resolve("dst");
        src.writeString(NOFOLLOW, UTF_8, "src");
        dst.writeString(NOFOLLOW, UTF_8, "dst");
        expectOnMoveTo(ExistsException.class, src, dst);
        assertTrue(src.exists(NOFOLLOW));
        assertTrue(dst.exists(NOFOLLOW));
        assertEquals("src", src.readString(NOFOLLOW, UTF_8));
        assertEquals("dst", dst.readString(NOFOLLOW, UTF_8));
    }

    public void test_moveTo_moveLinkNotTarget() throws Exception
    {
        final Resource target = dir1().resolve("target").createFile();
        final Resource src = dir1().resolve("src").createSymbolicLink(target);
        final Resource dst = dir1().resolve("dst");
        src.moveTo(dst);
        assertFalse(src.exists(NOFOLLOW));
        assertTrue(dst.exists(NOFOLLOW));
        assertTrue(target.exists(NOFOLLOW));
        assertEquals(target, dst.readSymbolicLink());
    }

    public void test_moveTo_fileToNonExistingFile() throws Exception
    {
        final Resource src = dir1().resolve("src");
        final Resource dst = dir1().resolve("dst");
        src.writeString(NOFOLLOW, UTF_8, "src");
        src.moveTo(dst);
        assertFalse(src.exists(NOFOLLOW));
        assertTrue(dst.exists(NOFOLLOW));
        assertEquals("src", dst.readString(NOFOLLOW, UTF_8));
    }

    public void test_moveTo_directoryToNonExistingDirectory() throws Exception
    {
        final Resource src = dir1().resolve("src");
        final Resource dst = dir1().resolve("dst");
        src.resolve("a").createDirectories();
        src.moveTo(dst);
        assertFalse(src.exists(NOFOLLOW));
        assertTrue(dst.exists(NOFOLLOW));
        assertTrue(dst.resolve("a").exists(NOFOLLOW));
    }

    public void test_moveTo_AccessException() throws Exception
    {
        final Resource src = dir1().resolve("src").createFile();
        final Resource dst = dir1().resolve("dst").createDirectory();
        dst.setPermissions(Collections.<Permission>emptySet());
        expectOnMoveTo(AccessException.class, src, dst.resolve("a"));
    }

    public void test_moveTo_NotExistException() throws Exception
    {
        final Resource src = dir1().resolve("src");
        final Resource dst = dir1().resolve("dst");
        expectOnMoveTo(NotExistException.class, src, dst);
    }

    public void test_moveTo_InvalidException() throws Exception
    {
        final Resource parent = dir1().resolve("parent").createDirectory();
        final Resource child = parent.resolve("child");
        expectOnMoveTo(InvalidException.class, parent, child);
    }

    public void test_moveTo_CrossDeviceException() throws Exception
    {
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
        final String srcPath = "/storage/emulated/0/test-" + nanoTime();
        final String dstPath = "/storage/emulated/legacy/test2-" + nanoTime();
        final Resource src = LocalResource.create(new File(srcPath));
        final Resource dst = LocalResource.create(new File(dstPath));
        try
        {
            src.createFile();
            expectOnMoveTo(CrossDeviceException.class, src, dst);
        }
        finally
        {
            try
            {
                if (src.exists(NOFOLLOW))
                {
                    src.delete();
                }
            }
            catch (final IOException e)
            {
                e.printStackTrace();
            }
            try
            {
                if (dst.exists(NOFOLLOW))
                {
                    dst.delete();
                }
            }
            catch (final IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private static void expectOnMoveTo(
            final Class<? extends Exception> clazz,
            final Resource src,
            final Resource dst) throws Exception
    {
        expect(clazz, new Code()
        {
            @Override
            public void run() throws Exception
            {
                src.moveTo(dst);
            }
        });
    }

    public void test_delete_symbolicLink() throws Exception
    {
        final Resource link = dir1().resolve("link");
        link.createSymbolicLink(dir1());
        assertTrue(link.exists(NOFOLLOW));
        link.delete();
        assertFalse(link.exists(NOFOLLOW));
    }

    public void test_delete_file() throws Exception
    {
        final Resource file = dir1().resolve("file");
        file.createFile();
        assertTrue(file.exists(NOFOLLOW));
        file.delete();
        assertFalse(file.exists(NOFOLLOW));
    }

    public void test_delete_emptyDirectory() throws Exception
    {
        final Resource directory = dir1().resolve("directory");
        directory.createDirectory();
        assertTrue(directory.exists(NOFOLLOW));
        directory.delete();
        assertFalse(directory.exists(NOFOLLOW));
    }

    public void test_delete_AccessException() throws Exception
    {
        final Resource file = dir1().resolve("a");
        file.createFile();
        dir1().setPermissions(Collections.<Permission>emptySet());
        expectOnDelete(AccessException.class, file);
    }

    public void test_delete_NotExistException() throws Exception
    {
        expectOnDelete(NotExistException.class, dir1().resolve("a"));
    }

    public void test_delete_NotDirectoryException() throws Exception
    {
        final Resource child = createChildWithNonDirectoryParent();
        expectOnDelete(NotDirectoryException.class, child);
    }

    public void test_delete_NotEmptyException() throws Exception
    {
        dir1().resolve("a").createDirectory();
        expectOnDelete(NotEmptyException.class, dir1());
    }

    private static void expectOnDelete(
            final Class<? extends Exception> clazz,
            final Resource resource) throws Exception
    {
        expect(clazz, new Code()
        {
            @Override
            public void run() throws Exception
            {
                resource.delete();
            }
        });
    }

    public void test_setModificationTime() throws Exception
    {
        final Instant old = getModificationTime(dir1(), NOFOLLOW);
        final Instant expect = Instant.of(old.getSeconds() + 101, old.getNanos() - 1);
        dir1().setModificationTime(NOFOLLOW, expect);
        final Instant actual = getModificationTime(dir1(), NOFOLLOW);
        assertEquals(expect, actual);
    }

    public void test_setModificationTime_doesNotAffectAccessTime()
            throws Exception
    {
        final Instant atime = getAccessTime(dir1(), NOFOLLOW);
        final Instant mtime = Instant.of(1, 2);
        sleep(3);
        dir1().setModificationTime(NOFOLLOW, mtime);
        assertNotEqual(atime, mtime);
        assertEquals(mtime, getModificationTime(dir1(), NOFOLLOW));
        assertEquals(atime, getAccessTime(dir1(), NOFOLLOW));
    }

    public void test_setModificationTime_linkFollow() throws Exception
    {
        final Resource file = dir1().resolve("file").createFile();
        final Resource link = dir1().resolve("link").createSymbolicLink(file);

        final Instant fileTime = Instant.of(123, 456);
        final Instant linkTime = getModificationTime(link, NOFOLLOW);
        link.setModificationTime(FOLLOW, fileTime);

        assertEquals(fileTime, getModificationTime(file, NOFOLLOW));
        assertEquals(linkTime, getModificationTime(link, NOFOLLOW));
        assertNotEqual(fileTime, linkTime);
    }

    public void test_setModificationTime_linkNoFollow() throws Exception
    {
        final Resource file = dir1().resolve("file").createFile();
        final Resource link = dir1().resolve("link").createSymbolicLink(file);

        final Instant fileTime = getModificationTime(file, NOFOLLOW);
        final Instant linkTime = Instant.of(123, 456);

        link.setModificationTime(NOFOLLOW, linkTime);

        assertEquals(linkTime, getModificationTime(link, NOFOLLOW));
        assertEquals(fileTime, getModificationTime(file, NOFOLLOW));
        assertNotEqual(fileTime, linkTime);
    }

    public void test_setModificationTime_NotExistException() throws Exception
    {
        final Resource doesNotExist = dir1().resolve("doesNotExist");
        final Class<NotExistException> expected = NotExistException.class;
        expectOnSetModificationTime(expected, doesNotExist, NOFOLLOW, EPOCH);
    }

    private Instant getModificationTime(
            final Resource resource,
            final LinkOption option) throws IOException
    {
        return resource.readStatus(option).getModificationTime();
    }

    private static void expectOnSetModificationTime(
            final Class<? extends Exception> clazz,
            final Resource resource,
            final LinkOption option,
            final Instant instant) throws Exception
    {
        expect(clazz, new Code()
        {
            @Override
            public void run() throws Exception
            {
                resource.setModificationTime(option, instant);
            }
        });
    }

    public void test_setAccessTime() throws Exception
    {
        final Instant old = getAccessTime(dir1(), NOFOLLOW);
        final Instant expect = Instant.of(old.getSeconds() + 101, old.getNanos() - 1);
        dir1().setAccessTime(NOFOLLOW, expect);
        final Instant actual = getAccessTime(dir1(), NOFOLLOW);
        assertEquals(expect, actual);
    }

    public void test_setAccessTime_doesNotAffectModificationTime()
            throws Exception
    {
        final Instant mtime = getModificationTime(dir1(), NOFOLLOW);
        final Instant atime = Instant.of(1, 2);
        sleep(3);
        dir1().setAccessTime(NOFOLLOW, atime);
        assertNotEqual(mtime, atime);
        assertEquals(atime, getAccessTime(dir1(), NOFOLLOW));
        assertEquals(mtime, getModificationTime(dir1(), NOFOLLOW));
    }

    public void test_setAccessTime_linkNoFollow() throws Exception
    {
        final Resource link = dir1().resolve("link").createSymbolicLink(dir1());

        final Instant targetTime = getAccessTime(dir1(), NOFOLLOW);
        final Instant linkTime = Instant.of(123, 456);

        link.setAccessTime(NOFOLLOW, linkTime);

        assertEquals(linkTime, getAccessTime(link, NOFOLLOW));
        assertEquals(targetTime, getAccessTime(dir1(), NOFOLLOW));
        assertNotEqual(targetTime, linkTime);
    }

    public void test_setAccessTime_linkFollow() throws Exception
    {
        final Resource link = dir1().resolve("link").createSymbolicLink(dir1());

        final Instant linkTime = getAccessTime(dir1(), NOFOLLOW);
        final Instant fileTime = Instant.of(123, 456);

        link.setAccessTime(FOLLOW, fileTime);

        assertEquals(linkTime, getAccessTime(link, NOFOLLOW));
        assertEquals(fileTime, getAccessTime(dir1(), NOFOLLOW));
        assertNotEqual(fileTime, linkTime);
    }

    public void test_setAccessTime_NotExistException() throws Exception
    {
        final Resource doesNotExist = dir1().resolve("doesNotExist");
        expectOnSetAccessTime(
                NotExistException.class, doesNotExist, NOFOLLOW, EPOCH);
    }

    private Instant getAccessTime(
            final Resource resource,
            final LinkOption option) throws IOException
    {
        return resource.readStatus(option).getAccessTime();
    }

    private static void expectOnSetAccessTime(
            final Class<? extends Exception> clazz,
            final Resource resource,
            final LinkOption option,
            final Instant instant) throws Exception
    {
        expect(clazz, new Code()
        {
            @Override
            public void run() throws Exception
            {
                resource.setAccessTime(option, instant);
            }
        });
    }

    public void test_setPermissions() throws Exception
    {
        for (final Set<Permission> expected : powerSet(allOf(Permission.class)))
        {
            dir1().setPermissions(expected);
            assertEquals(expected, dir1().readStatus(NOFOLLOW).getPermissions());
        }
    }

    public void test_setPermissions_rawBits() throws Exception
    {
        final int expected = Os.stat(dir1().getPath()).st_mode;
        dir1().setPermissions(dir1().readStatus(NOFOLLOW).getPermissions());
        final int actual = Os.stat(dir1().getPath()).st_mode;
        assertEquals(expected, actual);
    }

    public void test_removePermissions() throws Exception
    {
        final Set<Permission> all = unmodifiableSet(allOf(Permission.class));
        for (final Set<Permission> permissions : powerSet(all))
        {
            dir1().setPermissions(all);
            dir1().removePermissions(permissions);

            final Set<Permission> actual = dir1().readStatus(FOLLOW).getPermissions();
            final Set<Permission> expected = new HashSet<>(all);
            expected.removeAll(permissions);
            assertEquals(expected, actual);
        }
    }

    public void test_removePermissions_changeTargetNotLink() throws Exception
    {
        final Permission perm = OWNER_READ;
        final Resource link = dir1().resolve("link").createSymbolicLink(dir1());
        assertTrue(link.readStatus(FOLLOW).getPermissions().contains(perm));
        assertTrue(link.readStatus(NOFOLLOW).getPermissions().contains(perm));

        link.removePermissions(singleton(perm));

        assertFalse(link.readStatus(FOLLOW).getPermissions().contains(perm));
        assertTrue(link.readStatus(NOFOLLOW).getPermissions().contains(perm));
    }

    private static void expect(
            final Class<? extends Exception> clazz,
            final Code code) throws Exception
    {
        try
        {
            code.run();
            fail();
        }
        catch (final Exception e)
        {
            if (!clazz.isInstance(e))
            {
                throw e;
            }
        }
    }

    private interface Code
    {
        void run() throws Exception;
    }

    private Resource createChildWithNonDirectoryParent() throws IOException
    {
        final Resource parent = dir1().resolve("parent");
        final Resource child = parent.resolve("child");
        parent.createFile();
        return child;
    }

}
