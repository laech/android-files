package l.files.fs.local;

import org.junit.Test;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import l.files.base.io.Closer;
import l.files.fs.DirectoryNotEmpty;
import l.files.fs.File;
import l.files.fs.Instant;
import l.files.fs.LinkOption;
import l.files.fs.Permission;
import l.files.fs.Stat;
import l.files.fs.Stream;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.test.MoreAsserts.assertNotEqual;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static l.files.fs.File.ISO_8859_1;
import static l.files.fs.File.UTF_8;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Permission.OWNER_READ;
import static l.files.fs.local.LocalFile.permissionsFromMode;
import static l.files.fs.local.Stat.lstat;
import static l.files.fs.local.Stat.stat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public final class LocalFileTest extends FileBaseTest {

    private static final Random random = new Random();

    @Test
    public void can_handle_non_utf_8_path() throws Exception {

        byte[] bytes = {-19, -96, -67, -19, -80, -117};
        assertFalse(Arrays.equals(bytes.clone(), new String(bytes, UTF_8).getBytes(UTF_8)));

        LocalFile dir = dir1().resolve(bytes.clone());
        LocalFile file = dir.resolve("a");
        dir.createDir();
        file.createFile();

        assertTrue(dir.exists(NOFOLLOW));
        assertTrue(file.exists(NOFOLLOW));
        assertEquals(singleton(file), dir.list(FOLLOW).to(new HashSet<>()));

        assertArrayEquals(bytes.clone(), dir.path().name().bytes());
        assertEquals(new String(bytes.clone(), UTF_8), dir.path().name().toString());
        assertFalse(Arrays.equals(bytes.clone(), dir.path().name().toString().getBytes(UTF_8)));
    }

    @Test
    public void isReadable_true() throws Exception {
        assertTrue(dir1().isReadable());
    }

    @Test
    public void isReadable_false() throws Exception {
        dir1().removePermissions(Permission.read());
        assertFalse(dir1().isReadable());
    }

    @Test
    public void isWritable_true() throws Exception {
        assertTrue(dir1().isWritable());
    }

    @Test
    public void isWritable_false() throws Exception {
        dir1().removePermissions(Permission.write());
        assertFalse(dir1().isWritable());
    }

    @Test
    public void isExecutable_true() throws Exception {
        assertTrue(dir1().isExecutable());
    }

    @Test
    public void isExecutable_false() throws Exception {
        dir1().removePermissions(Permission.execute());
        assertFalse(dir1().isExecutable());
    }

    @Test
    public void stat_symbolicLink() throws Exception {
        File file = dir1().resolve("file").createFile();
        File link = dir1().resolve("link").createLink(file);
        assertFalse(file.stat(NOFOLLOW).isSymbolicLink());
        assertFalse(link.stat(FOLLOW).isSymbolicLink());
        assertTrue(link.stat(NOFOLLOW).isSymbolicLink());
        assertEquals(file.stat(NOFOLLOW), link.stat(FOLLOW));
    }

    @Test
    public void stat_modificationTime() throws Exception {
        Stat stat = dir1().stat(NOFOLLOW);
        long actual = stat.lastModifiedTime().seconds();
        long expected = stat(dir1().path().bytes()).mtime();
        assertEquals(expected, actual);
    }

    @Test
    public void stat_size() throws Exception {
        LocalFile file = dir1().resolve("file").createFile();
        file.appendUtf8("hello world");
        long expected = stat(file.path().bytes()).size();
        long actual = file.stat(NOFOLLOW).size();
        assertEquals(expected, actual);
    }

    @Test
    public void stat_isDirectory() throws Exception {
        assertTrue(dir1().stat(NOFOLLOW).isDirectory());
    }

    @Test
    public void stat_isRegularFile() throws Exception {
        File dir = dir1().resolve("dir").createFile();
        assertTrue(dir.stat(NOFOLLOW).isRegularFile());
    }

    @Test
    public void getHierarchy_single() throws Exception {
        File a = LocalFile.of("/");
        assertEquals(singletonList(a), a.hierarchy());
    }

    @Test
    public void getHierarchy_multi() throws Exception {
        File a = LocalFile.of("/a/b");
        List<File> expected = Arrays.<File>asList(
                LocalFile.of("/"),
                LocalFile.of("/a"),
                LocalFile.of("/a/b")
        );
        assertEquals(expected, a.hierarchy());
    }

    @Test
    public void list_linkFollowSuccess() throws Exception {
        File dir = dir1().resolve("dir").createDir();
        File a = dir.resolve("a").createFile();
        File b = dir.resolve("b").createDir();
        File c = dir.resolve("c").createLink(a);
        File link = dir1().resolve("link").createLink(dir);

        List<File> expected = asList(
                a.rebase(dir, link),
                b.rebase(dir, link),
                c.rebase(dir, link)
        );

        Closer closer = Closer.create();
        try {
            Stream<File> actual = closer.register(link.list(FOLLOW));
            assertEquals(expected, sortByName(actual));
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Test
    public void list() throws Exception {
        File a = dir1().resolve("a").createFile();
        File b = dir1().resolve("b").createDir();
        List<?> expected = asList(a, b);
        Closer closer = Closer.create();
        try {
            Stream<File> actual = closer.register(dir1().list(NOFOLLOW));
            assertEquals(expected, sortByName(actual));
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Test
    public void listDir_linkFollowSuccess() throws Exception {
        File dir = dir1().resolve("dir").createDir();
        File a = dir.resolve("a").createFile();
        dir.resolve("b").createDir();
        dir.resolve("c").createLink(a);

        File link = dir1().resolve("link").createLink(dir);
        List<File> expected = singletonList(link.resolve("b"));

        Closer closer = Closer.create();
        try {
            Stream<File> actual = closer.register(link.listDirs(FOLLOW));
            assertEquals(expected, sortByName(actual));
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Test
    public void listDir() throws Exception {
        dir1().resolve("a").createFile();
        dir1().resolve("b").createDir();
        dir1().resolve("c").createFile();
        List<?> expected = singletonList(dir1().resolve("b"));
        Closer closer = Closer.create();
        try {
            Stream<File> actual = closer.register(dir1().listDirs(NOFOLLOW));
            assertEquals(expected, sortByName(actual));
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Test
    public void output_created_file_has_correct_permissions() throws Exception {
        File a = dir1().resolve("a");
        File b = dir1().resolve("b");

        new FileOutputStream(a.path().toString()).close();
        b.newOutputStream().close();

        assertEquals(
                a.stat(NOFOLLOW).permissions(),
                b.stat(NOFOLLOW).permissions()
        );
    }

    @Test
    public void output_append_defaultFalse() throws Exception {
        test_output("a", "b", "b", new OutputProvider() {
            @Override
            public OutputStream open(File file) throws IOException {
                return file.newOutputStream();
            }
        });
    }

    @Test
    public void output_append_false() throws Exception {
        test_output("a", "b", "b", new OutputProvider() {
            @Override
            public OutputStream open(File file) throws IOException {
                return file.newOutputStream(false);
            }
        });
    }

    @Test
    public void output_append_true() throws Exception {
        test_output("a", "b", "ab", new OutputProvider() {
            @Override
            public OutputStream open(File file) throws IOException {
                return file.newOutputStream(true);
            }
        });
    }

    private void test_output(
            String initial,
            String write,
            String result,
            OutputProvider provider) throws Exception {

        File file = dir1().resolve("file").createFile();
        file.appendUtf8(initial);
        Closer closer = Closer.create();
        try {
            OutputStream out = closer.register(provider.open(file));
            out.write(write.getBytes(UTF_8));
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
        assertEquals(result, file.readAllUtf8());
    }

    private interface OutputProvider {
        OutputStream open(File file) throws IOException;
    }

    @Test
    public void output_createWithCorrectPermission()
            throws Exception {
        LocalFile expected = dir1().resolve("expected");
        LocalFile actual = dir1().resolve("actual");

        assertTrue(new java.io.File(expected.uri()).createNewFile());
        actual.newOutputStream(false).close();

        assertEquals(
                stat(expected.path().bytes()).mode(),
                stat(actual.path().bytes()).mode()
        );
    }

    @Test
    public void input() throws Exception {
        File file = dir1().resolve("a").createFile();
        String expected = "hello\nworld\n";
        file.appendUtf8(expected);
        assertEquals(expected, file.readAllUtf8());
    }

    @Test
    public void input_linkFollowSuccess() throws Exception {
        File target = dir1().resolve("target").createFile();
        File link = dir1().resolve("link").createLink(target);
        link.newInputStream().close();
    }

    @Test
    public void input_cannotUseAfterClose() throws Exception {
        File file = dir1().resolve("a").createFile();
        Closer closer = Closer.create();
        try {
            InputStream in = closer.register(file.newInputStream());
            FileDescriptor fd = ((FileInputStream) in).getFD();

            //noinspection ResultOfMethodCallIgnored
            in.read();
            in.close();
            try {
                //noinspection ResultOfMethodCallIgnored
                new FileInputStream(fd).read();
                fail();
            } catch (IOException e) {
                // Pass
            }
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Test
    public void exists_true() throws Exception {
        assertTrue(dir1().exists(NOFOLLOW));
    }

    @Test
    public void exists_false() throws Exception {
        assertFalse(dir1().resolve("a").exists(NOFOLLOW));
    }

    @Test
    public void exists_checkLinkNotTarget() throws Exception {
        File target = dir1().resolve("target");
        File link = dir1().resolve("link").createLink(target);
        assertFalse(target.exists(NOFOLLOW));
        assertFalse(link.exists(FOLLOW));
        assertTrue(link.exists(NOFOLLOW));
    }

    @Test
    public void readString() throws Exception {
        File file = dir1().resolve("file").createFile();
        String expected = "a\nb\tc";
        file.appendUtf8(expected);
        assertEquals(expected, file.readAllUtf8());
    }

    @Test
    public void createFile() throws Exception {
        File file = dir1().resolve("a");
        file.createFile();
        assertTrue(file.stat(NOFOLLOW).isRegularFile());
    }

    @Test
    public void createFile_correctPermissions() throws Exception {
        File actual = dir1().resolve("a");
        actual.createFile();

        java.io.File expected = new java.io.File(dir1().path().toString(), "b");
        assertTrue(expected.createNewFile());

        assertEquals(expected.canRead(), actual.isReadable());
        assertEquals(expected.canWrite(), actual.isWritable());
        assertEquals(expected.canExecute(), actual.isExecutable());
        assertEquals(
                permissionsFromMode(lstat(expected.getPath().getBytes(UTF_8)).mode()),
                actual.stat(NOFOLLOW).permissions()
        );
    }

    @Test
    public void createDirectory() throws Exception {
        File dir = dir1().resolve("a");
        dir.createDir();
        assertTrue(dir.stat(NOFOLLOW).isDirectory());
    }

    @Test
    public void createDirectory_correctPermissions() throws Exception {
        File actual = dir1().resolve("a");
        actual.createDir();

        java.io.File expected = new java.io.File(dir1().path().toString(), "b");
        assertTrue(expected.mkdir());

        assertEquals(expected.canRead(), actual.isReadable());
        assertEquals(expected.canWrite(), actual.isWritable());
        assertEquals(expected.canExecute(), actual.isExecutable());
        assertEquals(
                permissionsFromMode(lstat(expected.getPath().getBytes(UTF_8)).mode()),
                actual.stat(NOFOLLOW).permissions()
        );
    }

    @Test
    public void createDirectories() throws Exception {
        dir1().resolve("a/b/c").createDirs();
        assertTrue(dir1().resolve("a/b/c").stat(NOFOLLOW).isDirectory());
        assertTrue(dir1().resolve("a/b").stat(NOFOLLOW).isDirectory());
        assertTrue(dir1().resolve("a/").stat(NOFOLLOW).isDirectory());
    }

    @Test
    public void createSymbolicLink() throws Exception {
        File link = dir1().resolve("link").createLink(dir1());
        assertTrue(link.stat(NOFOLLOW).isSymbolicLink());
        assertEquals(dir1(), link.readLink());
    }

    @Test
    public void stat_followLink() throws Exception {
        File child = dir1().resolve("a").createLink(dir1());
        Stat expected = dir1().stat(NOFOLLOW);
        Stat actual = child.stat(FOLLOW);
        assertTrue(actual.isDirectory());
        assertFalse(actual.isSymbolicLink());
        assertEquals(expected, actual);
    }

    @Test
    public void stat_noFollowLink() throws Exception {
        File child = dir1().resolve("a").createLink(dir1());
        Stat actual = child.stat(NOFOLLOW);
        assertTrue(actual.isSymbolicLink());
        assertFalse(actual.isDirectory());
        assertNotEqual(dir1().stat(NOFOLLOW), actual);
    }

    @Test
    public void moveTo_moveLinkNotTarget() throws Exception {
        File target = dir1().resolve("target").createFile();
        File src = dir1().resolve("src").createLink(target);
        File dst = dir1().resolve("dst");
        src.moveTo(dst);
        assertFalse(src.exists(NOFOLLOW));
        assertTrue(dst.exists(NOFOLLOW));
        assertTrue(target.exists(NOFOLLOW));
        assertEquals(target, dst.readLink());
    }

    @Test
    public void moveTo_fileToNonExistingFile() throws Exception {
        File src = dir1().resolve("src");
        File dst = dir1().resolve("dst");
        src.appendUtf8("src");
        src.moveTo(dst);
        assertFalse(src.exists(NOFOLLOW));
        assertTrue(dst.exists(NOFOLLOW));
        assertEquals("src", dst.readAllUtf8());
    }

    @Test
    public void moveTo_directoryToNonExistingDirectory() throws Exception {
        File src = dir1().resolve("src");
        File dst = dir1().resolve("dst");
        src.resolve("a").createDirs();
        src.moveTo(dst);
        assertFalse(src.exists(NOFOLLOW));
        assertTrue(dst.exists(NOFOLLOW));
        assertTrue(dst.resolve("a").exists(NOFOLLOW));
    }

    @Test
    public void delete_symbolicLink() throws Exception {
        File link = dir1().resolve("link");
        link.createLink(dir1());
        assertTrue(link.exists(NOFOLLOW));
        link.delete();
        assertFalse(link.exists(NOFOLLOW));
    }

    @Test
    public void delete_file() throws Exception {
        File file = dir1().resolve("file");
        file.createFile();
        assertTrue(file.exists(NOFOLLOW));
        file.delete();
        assertFalse(file.exists(NOFOLLOW));
    }

    @Test
    public void delete_emptyDirectory() throws Exception {
        File directory = dir1().resolve("directory");
        directory.createDir();
        assertTrue(directory.exists(NOFOLLOW));
        directory.delete();
        assertFalse(directory.exists(NOFOLLOW));
    }

    @Test
    public void deleteRecursive_symbolicLink() throws Exception {
        File dir = dir1().resolve("dir").createDir();
        File a = dir.resolve("a").createFile();
        File link = dir1().resolve("link").createLink(dir);
        assertTrue(link.exists(NOFOLLOW));
        link.deleteRecursive();
        assertFalse(link.exists(NOFOLLOW));
        assertTrue(dir.exists(NOFOLLOW));
        assertTrue(a.exists(NOFOLLOW));
    }

    @Test
    public void deleteRecursive_file() throws Exception {
        File file = dir1().resolve("file").createFile();
        assertTrue(file.exists(NOFOLLOW));
        file.deleteRecursive();
        assertFalse(file.exists(NOFOLLOW));
    }

    @Test
    public void deleteRecursive_emptyDirectory() throws Exception {
        File dir = dir1().resolve("dir").createDir();
        assertTrue(dir.exists(NOFOLLOW));
        dir.delete();
        assertFalse(dir.exists(NOFOLLOW));
    }

    @Test
    public void deleteRecursive_nonEmptyDirectory() throws Exception {
        File dir = dir1().resolve("dir").createDir();
        File sub = dir.resolve("sub").createDir();
        File a = dir.resolve("a").createFile();
        File b = sub.resolve("b").createFile();
        assertTrue(dir.exists(NOFOLLOW));
        assertTrue(sub.exists(NOFOLLOW));
        assertTrue(a.exists(NOFOLLOW));
        assertTrue(b.exists(NOFOLLOW));
        dir.deleteRecursive();
        assertFalse(dir.exists(NOFOLLOW));
    }

    @Test
    public void deleteIfExists_nonExist_willIgnore() throws Exception {
        dir1().resolve("a").deleteIfExists();
    }

    @Test
    public void deleteIfExists_fileExist_willDelete() throws Exception {
        File file = dir1().resolve("a").createFile();
        assertTrue(file.exists(NOFOLLOW));
        file.deleteIfExists();
        assertFalse(file.exists(NOFOLLOW));
    }

    @Test
    public void deleteIfExists_emptyDirExist_willDelete() throws Exception {
        File dir = dir1().resolve("a").createDir();
        assertTrue(dir.exists(NOFOLLOW));
        dir.deleteIfExists();
        assertFalse(dir.exists(NOFOLLOW));
    }

    @Test
    public void deleteIfExists_nonEmptyDirExist_willError() throws Exception {
        File dir = dir1().resolve("a").createDir();
        File file = dir.resolve("1").createFile();
        assertTrue(dir.exists(NOFOLLOW));
        try {
            dir.deleteIfExists();
        } catch (DirectoryNotEmpty ignore) {
        }
        assertTrue(dir.exists(NOFOLLOW));
        assertTrue(file.exists(NOFOLLOW));
    }

    @Test
    public void deleteRecursiveIfExists_nonExistWillIgnore() throws Exception {
        dir1().resolve("nonExist").deleteRecursiveIfExists();
    }

    @Test
    public void deleteRecursiveIfExists_emptyDirWillDelete() throws Exception {

        File dir = dir1().resolve("dir").createDir();
        assertTrue(dir.exists(NOFOLLOW));

        dir.deleteRecursiveIfExists();
        assertFalse(dir.exists(NOFOLLOW));
    }

    @Test
    public void deleteRecursiveIfExists_nonEmptyDirWillDelete() throws Exception {

        File dir = dir1().resolve("dir").createDir();
        File file = dir.resolve("child").createFile();
        assertTrue(dir.exists(NOFOLLOW));
        assertTrue(file.exists(NOFOLLOW));

        dir.deleteRecursiveIfExists();
        assertFalse(dir.exists(NOFOLLOW));
    }

    @Test
    public void deleteRecursiveIfExists_fileWillDelete() throws Exception {

        File file = dir1().resolve("file").createFile();
        assertTrue(file.exists(NOFOLLOW));

        file.deleteRecursiveIfExists();
        assertFalse(file.exists(NOFOLLOW));
    }

    @Test
    public void deleteRecursiveIfExists_linkToFileWillDeleteNoFollow() throws Exception {

        File file = dir1().resolve("file").createFile();
        File link = dir1().resolve("link").createLink(file);
        assertTrue(file.exists(NOFOLLOW));
        assertTrue(link.exists(NOFOLLOW));

        link.deleteRecursiveIfExists();
        assertTrue(file.exists(NOFOLLOW));
        assertFalse(link.exists(NOFOLLOW));
    }

    @Test
    public void deleteRecursiveIfExists_linkToDirWillDeleteNoFollow() throws Exception {

        File dir = dir1().resolve("dir").createDir();
        File file = dir.resolve("file").createFile();
        File link = dir1().resolve("link").createLink(dir);
        assertTrue(dir.exists(NOFOLLOW));
        assertTrue(file.exists(NOFOLLOW));
        assertTrue(link.exists(NOFOLLOW));

        link.deleteRecursiveIfExists();
        assertTrue(dir.exists(NOFOLLOW));
        assertTrue(file.exists(NOFOLLOW));
        assertFalse(link.exists(NOFOLLOW));
    }

    @Test
    public void setModificationTime() throws Exception {
        Instant expect = newInstant();
        dir1().setLastModifiedTime(NOFOLLOW, expect);
        Instant actual = getModificationTime(dir1(), NOFOLLOW);
        assertEquals(expect, actual);
    }

    @Test
    public void setModificationTime_linkFollow() throws Exception {
        File file = dir1().resolve("file").createFile();
        File link = dir1().resolve("link").createLink(file);

        Instant fileTime = newInstant();
        Instant linkTime = getModificationTime(link, NOFOLLOW);
        link.setLastModifiedTime(FOLLOW, fileTime);

        assertEquals(fileTime, getModificationTime(file, NOFOLLOW));
        assertEquals(linkTime, getModificationTime(link, NOFOLLOW));
        assertNotEqual(fileTime, linkTime);
    }

    @Test
    public void setModificationTime_linkNoFollow() throws Exception {
        File file = dir1().resolve("file").createFile();
        File link = dir1().resolve("link").createLink(file);

        Instant fileTime = getModificationTime(file, NOFOLLOW);
        Instant linkTime = newInstant();

        link.setLastModifiedTime(NOFOLLOW, linkTime);

        assertEquals(linkTime, getModificationTime(link, NOFOLLOW));
        assertEquals(fileTime, getModificationTime(file, NOFOLLOW));
        assertNotEqual(fileTime, linkTime);
    }

    private Instant newInstant() {
        if (SDK_INT >= LOLLIPOP) {
            return Instant.of(random.nextInt(1_000_000), random.nextInt(999_999) + 1);
        } else {
            return Instant.of(random.nextInt(1_000_000), 0); // Nanos not supported
        }
    }

    private Instant getModificationTime(
            File file,
            LinkOption option) throws IOException {
        return file.stat(option).lastModifiedTime();
    }

    @Test
    public void setPermissions() throws Exception {
        List<Set<Permission>> permissions = asList(
                Permission.all(),
                Permission.read(),
                Permission.write(),
                Permission.execute());
        for (Set<Permission> expected : permissions) {
            dir1().setPermissions(expected);
            assertEquals(expected, dir1().stat(NOFOLLOW).permissions());
        }
    }

    @Test
    public void setPermissions_rawBits() throws Exception {
        int expected = stat(dir1().path().bytes()).mode();
        dir1().setPermissions(dir1().stat(NOFOLLOW).permissions());
        int actual = stat(dir1().path().bytes()).mode();
        assertEquals(expected, actual);
    }

    @Test
    public void removePermissions() throws Exception {
        List<Set<Permission>> combinations = asList(
                Permission.all(),
                Permission.read(),
                Permission.write(),
                Permission.execute());
        for (Set<Permission> permissions : combinations) {
            dir1().setPermissions(Permission.all());
            dir1().removePermissions(permissions);

            Set<Permission> actual = dir1().stat(FOLLOW).permissions();
            Set<Permission> expected = new HashSet<>(Permission.all());
            expected.removeAll(permissions);
            assertEquals(expected, actual);
        }
    }

    @Test
    public void removePermissions_changeTargetNotLink() throws Exception {
        Permission perm = OWNER_READ;
        File link = dir1().resolve("link").createLink(dir1());
        assertTrue(link.stat(FOLLOW).permissions().contains(perm));
        assertTrue(link.stat(NOFOLLOW).permissions().contains(perm));

        link.removePermissions(singleton(perm));

        assertFalse(link.stat(FOLLOW).permissions().contains(perm));
        assertTrue(link.stat(NOFOLLOW).permissions().contains(perm));
    }

    @Test
    public void readDetectingCharset_utf8() throws Exception {
        File file = dir1().resolve("a").createFile();
        file.writeAllUtf8("你好");
        assertEquals("", file.readDetectingCharset(0));
        assertEquals("你", file.readDetectingCharset(1));
        assertEquals("你好", file.readDetectingCharset(2));
        assertEquals("你好", file.readDetectingCharset(3));
    }

    @Test
    public void readDetectingCharset_iso88591() throws Exception {
        File file = dir1().resolve("a").createFile();
        file.writeAll("hello world", ISO_8859_1);
        assertEquals("", file.readDetectingCharset(0));
        assertEquals("h", file.readDetectingCharset(1));
        assertEquals("he", file.readDetectingCharset(2));
        assertEquals("hel", file.readDetectingCharset(3));
        assertEquals("hello world", file.readDetectingCharset(100));
    }

    private List<File> sortByName(Stream<File> actual) throws IOException {
        List<File> files = new ArrayList<>();
        actual.to(files);
        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File a, File b) {
                return a.name().toString().compareTo(b.name().toString());
            }
        });
        return files;
    }

}
