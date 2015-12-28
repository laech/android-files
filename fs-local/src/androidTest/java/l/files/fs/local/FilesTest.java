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
import l.files.fs.Files;
import l.files.fs.Instant;
import l.files.fs.LinkOption;
import l.files.fs.Path;
import l.files.fs.Paths;
import l.files.fs.Permission;
import l.files.fs.Stat;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.test.MoreAsserts.assertNotEqual;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static l.files.fs.Files.ISO_8859_1;
import static l.files.fs.Files.UTF_8;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Permission.OWNER_READ;
import static l.files.fs.local.LocalFileSystem.permissionsFromMode;
import static l.files.fs.local.Stat.lstat;
import static l.files.fs.local.Stat.stat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public final class FilesTest extends PathBaseTest {

    private static final Random random = new Random();

    @Test
    public void can_handle_non_utf_8_path() throws Exception {

        byte[] bytes = {-19, -96, -67, -19, -80, -117};
        assertFalse(Arrays.equals(bytes.clone(), new String(bytes, UTF_8).getBytes(UTF_8)));

        Path dir = dir1().resolve(bytes.clone());
        Path file = dir.resolve("a");
        Files.createDir(dir);
        Files.createFile(file);

        assertTrue(Files.exists(dir, NOFOLLOW));
        assertTrue(Files.exists(file, NOFOLLOW));
        assertEquals(singleton(file), Files.list(dir, FOLLOW, new HashSet<>()));

        assertArrayEquals(bytes.clone(), dir.name().toByteArray());
        assertEquals(new String(bytes.clone(), UTF_8), dir.name().toString());
        assertFalse(Arrays.equals(bytes.clone(), dir.name().toString().getBytes(UTF_8)));
    }

    @Test
    public void isReadable_true() throws Exception {
        assertTrue(Files.isReadable(dir1()));
    }

    @Test
    public void isReadable_false() throws Exception {
        Files.removePermissions(dir1(), Permission.read());
        assertFalse(Files.isReadable(dir1()));
    }

    @Test
    public void isWritable_true() throws Exception {
        assertTrue(Files.isWritable(dir1()));
    }

    @Test
    public void isWritable_false() throws Exception {
        Files.removePermissions(dir1(), Permission.write());
        assertFalse(Files.isWritable(dir1()));
    }

    @Test
    public void isExecutable_true() throws Exception {
        assertTrue(Files.isExecutable(dir1()));
    }

    @Test
    public void isExecutable_false() throws Exception {
        Files.removePermissions(dir1(), Permission.execute());
        assertFalse(Files.isExecutable(dir1()));
    }

    @Test
    public void stat_symbolicLink() throws Exception {
        Path file = Files.createFile(dir1().resolve("file"));
        Path link = Files.createLink(dir1().resolve("link"), file);
        assertFalse(Files.stat(file, NOFOLLOW).isSymbolicLink());
        assertFalse(Files.stat(link, FOLLOW).isSymbolicLink());
        assertTrue(Files.stat(link, NOFOLLOW).isSymbolicLink());
        assertEquals(Files.stat(file, NOFOLLOW), Files.stat(link, FOLLOW));
    }

    @Test
    public void stat_modificationTime() throws Exception {
        Stat stat = Files.stat(dir1(), NOFOLLOW);
        long actual = stat.lastModifiedTime().seconds();
        long expected = stat(dir1().toByteArray()).mtime();
        assertEquals(expected, actual);
    }

    @Test
    public void stat_size() throws Exception {
        Path file = Files.createFile(dir1().resolve("file"));
        Files.appendUtf8(file, "hello world");
        long expected = stat(file.toByteArray()).size();
        long actual = Files.stat(file, NOFOLLOW).size();
        assertEquals(expected, actual);
    }

    @Test
    public void stat_isDirectory() throws Exception {
        assertTrue(Files.stat(dir1(), NOFOLLOW).isDirectory());
    }

    @Test
    public void stat_isRegularFile() throws Exception {
        Path dir = Files.createFile(dir1().resolve("dir"));
        assertTrue(Files.stat(dir, NOFOLLOW).isRegularFile());
    }

    @Test
    public void getHierarchy_single() throws Exception {
        Path a = Paths.get("/");
        assertEquals(singletonList(a), Files.hierarchy(a));
    }

    @Test
    public void getHierarchy_multi() throws Exception {
        Path a = Paths.get("/a/b");
        List<Path> expected = asList(
                Paths.get("/"),
                Paths.get("/a"),
                Paths.get("/a/b")
        );
        assertEquals(expected, Files.hierarchy(a));
    }

    @Test
    public void list_linkFollowSuccess() throws Exception {
        Path dir = Files.createDir(dir1().resolve("dir"));
        Path a = Files.createFile(dir.resolve("a"));
        Path b = Files.createDir(dir.resolve("b"));
        Path c = Files.createLink(dir.resolve("c"), a);
        Path link = Files.createLink(dir1().resolve("link"), dir);

        List<Path> expected = asList(
                a.rebase(dir, link),
                b.rebase(dir, link),
                c.rebase(dir, link)
        );

        List<Path> actual = sortByName(Files.list(link, FOLLOW, new ArrayList<Path>()));
        assertEquals(expected, actual);
    }

    @Test
    public void list() throws Exception {
        Path a = Files.createFile(dir1().resolve("a"));
        Path b = Files.createDir(dir1().resolve("b"));
        List<Path> expected = asList(a, b);
        List<Path> actual = sortByName(Files.list(dir1(), NOFOLLOW, new ArrayList<Path>()));
        assertEquals(expected, actual);
    }

    @Test
    public void listDir_linkFollowSuccess() throws Exception {
        Path dir = Files.createDir(dir1().resolve("dir"));
        Path a = Files.createFile(dir.resolve("a"));
        Files.createDir(dir.resolve("b"));
        Files.createLink(dir.resolve("c"), a);

        Path link = Files.createLink(dir1().resolve("link"), dir);
        List<Path> expected = singletonList(link.resolve("b"));
        List<Path> actual = sortByName(Files.listDirs(link, FOLLOW, new ArrayList<Path>()));
        assertEquals(expected, actual);
    }

    @Test
    public void listDir() throws Exception {
        Files.createFile(dir1().resolve("a"));
        Files.createDir(dir1().resolve("b"));
        Files.createFile(dir1().resolve("c"));
        List<?> expected = singletonList(dir1().resolve("b"));
        List<?> actual = sortByName(Files.listDirs(dir1(), NOFOLLOW, new ArrayList<Path>()));
        assertEquals(expected, actual);
    }

    @Test
    public void output_created_file_has_correct_permissions() throws Exception {
        Path a = dir1().resolve("a");
        Path b = dir1().resolve("b");

        new FileOutputStream(a.toString()).close();
        Files.newOutputStream(b).close();

        assertEquals(
                Files.stat(a, NOFOLLOW).permissions(),
                Files.stat(b, NOFOLLOW).permissions()
        );
    }

    @Test
    public void output_append_defaultFalse() throws Exception {
        test_output("a", "b", "b", new OutputProvider() {
            @Override
            public OutputStream open(Path file) throws IOException {
                return Files.newOutputStream(file);
            }
        });
    }

    @Test
    public void output_append_false() throws Exception {
        test_output("a", "b", "b", new OutputProvider() {
            @Override
            public OutputStream open(Path file) throws IOException {
                return Files.newOutputStream(file, false);
            }
        });
    }

    @Test
    public void output_append_true() throws Exception {
        test_output("a", "b", "ab", new OutputProvider() {
            @Override
            public OutputStream open(Path file) throws IOException {
                return Files.newOutputStream(file, true);
            }
        });
    }

    private void test_output(
            String initial,
            String write,
            String result,
            OutputProvider provider) throws Exception {

        Path file = Files.createFile(dir1().resolve("file"));
        Files.appendUtf8(file, initial);
        Closer closer = Closer.create();
        try {
            OutputStream out = closer.register(provider.open(file));
            out.write(write.getBytes(UTF_8));
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
        assertEquals(result, Files.readAllUtf8(file));
    }

    private interface OutputProvider {
        OutputStream open(Path file) throws IOException;
    }

    @Test
    public void output_createWithCorrectPermission()
            throws Exception {
        Path expected = dir1().resolve("expected");
        Path actual = dir1().resolve("actual");

        assertTrue(new java.io.File(expected.toUri()).createNewFile());
        Files.newOutputStream(actual, false).close();

        assertEquals(
                stat(expected.toByteArray()).mode(),
                stat(actual.toByteArray()).mode()
        );
    }

    @Test
    public void input() throws Exception {
        Path file = Files.createFile(dir1().resolve("a"));
        String expected = "hello\nworld\n";
        Files.appendUtf8(file, expected);
        assertEquals(expected, Files.readAllUtf8(file));
    }

    @Test
    public void input_linkFollowSuccess() throws Exception {
        Path target = Files.createFile(dir1().resolve("target"));
        Path link = Files.createLink(dir1().resolve("link"), target);
        Files.newInputStream(link).close();
    }

    @Test
    public void input_cannotUseAfterClose() throws Exception {
        Path file = Files.createFile(dir1().resolve("a"));
        Closer closer = Closer.create();
        try {
            InputStream in = closer.register(Files.newInputStream(file));
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
        assertTrue(Files.exists(dir1(), NOFOLLOW));
    }

    @Test
    public void exists_false() throws Exception {
        assertFalse(Files.exists(dir1().resolve("a"), NOFOLLOW));
    }

    @Test
    public void exists_checkLinkNotTarget() throws Exception {
        Path target = dir1().resolve("target");
        Path link = Files.createLink(dir1().resolve("link"), target);
        assertFalse(Files.exists(target, NOFOLLOW));
        assertFalse(Files.exists(link, FOLLOW));
        assertTrue(Files.exists(link, NOFOLLOW));
    }

    @Test
    public void readString() throws Exception {
        Path file = Files.createFile(dir1().resolve("file"));
        String expected = "a\nb\tc";
        Files.appendUtf8(file, expected);
        assertEquals(expected, Files.readAllUtf8(file));
    }

    @Test
    public void createFile() throws Exception {
        Path file = dir1().resolve("a");
        Files.createFile(file);
        assertTrue(Files.stat(file, NOFOLLOW).isRegularFile());
    }

    @Test
    public void createFile_correctPermissions() throws Exception {
        Path actual = dir1().resolve("a");
        Files.createFile(actual);

        java.io.File expected = new java.io.File(dir1().toString(), "b");
        assertTrue(expected.createNewFile());

        assertEquals(expected.canRead(), Files.isReadable(actual));
        assertEquals(expected.canWrite(), Files.isWritable(actual));
        assertEquals(expected.canExecute(), Files.isExecutable(actual));
        assertEquals(
                permissionsFromMode(lstat(expected.getPath().getBytes(UTF_8)).mode()),
                Files.stat(actual, NOFOLLOW).permissions()
        );
    }

    @Test
    public void createDirectory() throws Exception {
        Path dir = dir1().resolve("a");
        Files.createDir(dir);
        assertTrue(Files.stat(dir, NOFOLLOW).isDirectory());
    }

    @Test
    public void createDirectory_correctPermissions() throws Exception {
        Path actual = dir1().resolve("a");
        Files.createDir(actual);

        java.io.File expected = new java.io.File(dir1().toString(), "b");
        assertTrue(expected.mkdir());

        assertEquals(expected.canRead(), Files.isReadable(actual));
        assertEquals(expected.canWrite(), Files.isWritable(actual));
        assertEquals(expected.canExecute(), Files.isExecutable(actual));
        assertEquals(
                permissionsFromMode(lstat(expected.getPath().getBytes(UTF_8)).mode()),
                Files.stat(actual, NOFOLLOW).permissions()
        );
    }

    @Test
    public void createDirectories() throws Exception {
        Files.createDirs(dir1().resolve("a/b/c"));
        assertTrue(Files.stat(dir1().resolve("a/b/c"), NOFOLLOW).isDirectory());
        assertTrue(Files.stat(dir1().resolve("a/b"), NOFOLLOW).isDirectory());
        assertTrue(Files.stat(dir1().resolve("a/"), NOFOLLOW).isDirectory());
    }

    @Test
    public void createSymbolicLink() throws Exception {
        Path link = Files.createLink(dir1().resolve("link"), dir1());
        assertTrue(Files.stat(link, NOFOLLOW).isSymbolicLink());
        assertEquals(dir1(), Files.readLink(link));
    }

    @Test
    public void stat_followLink() throws Exception {
        Path child = Files.createLink(dir1().resolve("a"), dir1());
        Stat expected = Files.stat(dir1(), NOFOLLOW);
        Stat actual = Files.stat(child, FOLLOW);
        assertTrue(actual.isDirectory());
        assertFalse(actual.isSymbolicLink());
        assertEquals(expected, actual);
    }

    @Test
    public void stat_noFollowLink() throws Exception {
        Path child = Files.createLink(dir1().resolve("a"), dir1());
        Stat actual = Files.stat(child, NOFOLLOW);
        assertTrue(actual.isSymbolicLink());
        assertFalse(actual.isDirectory());
        assertNotEqual(Files.stat(dir1(), NOFOLLOW), actual);
    }

    @Test
    public void moveTo_moveLinkNotTarget() throws Exception {
        Path target = Files.createFile(dir1().resolve("target"));
        Path src = Files.createLink(dir1().resolve("src"), target);
        Path dst = dir1().resolve("dst");
        Files.move(src, dst);
        assertFalse(Files.exists(src, NOFOLLOW));
        assertTrue(Files.exists(dst, NOFOLLOW));
        assertTrue(Files.exists(target, NOFOLLOW));
        assertEquals(target, Files.readLink(dst));
    }

    @Test
    public void moveTo_fileToNonExistingFile() throws Exception {
        Path src = dir1().resolve("src");
        Path dst = dir1().resolve("dst");
        Files.appendUtf8(src, "src");
        Files.move(src, dst);
        assertFalse(Files.exists(src, NOFOLLOW));
        assertTrue(Files.exists(dst, NOFOLLOW));
        assertEquals("src", Files.readAllUtf8(dst));
    }

    @Test
    public void moveTo_directoryToNonExistingDirectory() throws Exception {
        Path src = dir1().resolve("src");
        Path dst = dir1().resolve("dst");
        Files.createDirs(src.resolve("a"));
        Files.move(src, dst);
        assertFalse(Files.exists(src, NOFOLLOW));
        assertTrue(Files.exists(dst, NOFOLLOW));
        assertTrue(Files.exists(dst.resolve("a"), NOFOLLOW));
    }

    @Test
    public void delete_symbolicLink() throws Exception {
        Path link = dir1().resolve("link");
        Files.createLink(link, dir1());
        assertTrue(Files.exists(link, NOFOLLOW));
        Files.delete(link);
        assertFalse(Files.exists(link, NOFOLLOW));
    }

    @Test
    public void delete_file() throws Exception {
        Path file = dir1().resolve("file");
        Files.createFile(file);
        assertTrue(Files.exists(file, NOFOLLOW));
        Files.delete(file);
        assertFalse(Files.exists(file, NOFOLLOW));
    }

    @Test
    public void delete_emptyDirectory() throws Exception {
        Path directory = dir1().resolve("directory");
        Files.createDir(directory);
        assertTrue(Files.exists(directory, NOFOLLOW));
        Files.delete(directory);
        assertFalse(Files.exists(directory, NOFOLLOW));
    }

    @Test
    public void deleteRecursive_symbolicLink() throws Exception {
        Path dir = Files.createDir(dir1().resolve("dir"));
        Path a = Files.createFile(dir.resolve("a"));
        Path link = Files.createLink(dir1().resolve("link"), dir);
        assertTrue(Files.exists(link, NOFOLLOW));
        Files.deleteRecursive(link);
        assertFalse(Files.exists(link, NOFOLLOW));
        assertTrue(Files.exists(dir, NOFOLLOW));
        assertTrue(Files.exists(a, NOFOLLOW));
    }

    @Test
    public void deleteRecursive_file() throws Exception {
        Path file = Files.createFile(dir1().resolve("file"));
        assertTrue(Files.exists(file, NOFOLLOW));
        Files.deleteRecursive(file);
        assertFalse(Files.exists(file, NOFOLLOW));
    }

    @Test
    public void deleteRecursive_emptyDirectory() throws Exception {
        Path dir = Files.createDir(dir1().resolve("dir"));
        assertTrue(Files.exists(dir, NOFOLLOW));
        Files.delete(dir);
        assertFalse(Files.exists(dir, NOFOLLOW));
    }

    @Test
    public void deleteRecursive_nonEmptyDirectory() throws Exception {
        Path dir = Files.createDir(dir1().resolve("dir"));
        Path sub = Files.createDir(dir.resolve("sub"));
        Path a = Files.createFile(dir.resolve("a"));
        Path b = Files.createFile(sub.resolve("b"));
        assertTrue(Files.exists(dir, NOFOLLOW));
        assertTrue(Files.exists(sub, NOFOLLOW));
        assertTrue(Files.exists(a, NOFOLLOW));
        assertTrue(Files.exists(b, NOFOLLOW));
        Files.deleteRecursive(dir);
        assertFalse(Files.exists(dir, NOFOLLOW));
    }

    @Test
    public void deleteIfExists_nonExist_willIgnore() throws Exception {
        Files.deleteIfExists(dir1().resolve("a"));
    }

    @Test
    public void deleteIfExists_fileExist_willDelete() throws Exception {
        Path file = Files.createFile(dir1().resolve("a"));
        assertTrue(Files.exists(file, NOFOLLOW));
        Files.deleteIfExists(file);
        assertFalse(Files.exists(file, NOFOLLOW));
    }

    @Test
    public void deleteIfExists_emptyDirExist_willDelete() throws Exception {
        Path dir = Files.createDir(dir1().resolve("a"));
        assertTrue(Files.exists(dir, NOFOLLOW));
        Files.deleteIfExists(dir);
        assertFalse(Files.exists(dir, NOFOLLOW));
    }

    @Test
    public void deleteIfExists_nonEmptyDirExist_willError() throws Exception {
        Path dir = Files.createDir(dir1().resolve("a"));
        Path file = Files.createFile(dir.resolve("1"));
        assertTrue(Files.exists(dir, NOFOLLOW));
        try {
            Files.deleteIfExists(dir);
        } catch (DirectoryNotEmpty ignore) {
        }
        assertTrue(Files.exists(dir, NOFOLLOW));
        assertTrue(Files.exists(file, NOFOLLOW));
    }

    @Test
    public void deleteRecursiveIfExists_nonExistWillIgnore() throws Exception {
        Files.deleteRecursiveIfExists(dir1().resolve("nonExist"));
    }

    @Test
    public void deleteRecursiveIfExists_emptyDirWillDelete() throws Exception {

        Path dir = Files.createDir(dir1().resolve("dir"));
        assertTrue(Files.exists(dir, NOFOLLOW));

        Files.deleteRecursiveIfExists(dir);
        assertFalse(Files.exists(dir, NOFOLLOW));
    }

    @Test
    public void deleteRecursiveIfExists_nonEmptyDirWillDelete() throws Exception {

        Path dir = Files.createDir(dir1().resolve("dir"));
        Path file = Files.createFile(dir.resolve("child"));
        assertTrue(Files.exists(dir, NOFOLLOW));
        assertTrue(Files.exists(file, NOFOLLOW));

        Files.deleteRecursiveIfExists(dir);
        assertFalse(Files.exists(dir, NOFOLLOW));
    }

    @Test
    public void deleteRecursiveIfExists_fileWillDelete() throws Exception {

        Path file = Files.createFile(dir1().resolve("file"));
        assertTrue(Files.exists(file, NOFOLLOW));

        Files.deleteRecursiveIfExists(file);
        assertFalse(Files.exists(file, NOFOLLOW));
    }

    @Test
    public void deleteRecursiveIfExists_linkToFileWillDeleteNoFollow() throws Exception {

        Path file = Files.createFile(dir1().resolve("file"));
        Path link = Files.createLink(dir1().resolve("link"), file);
        assertTrue(Files.exists(file, NOFOLLOW));
        assertTrue(Files.exists(link, NOFOLLOW));

        Files.deleteRecursiveIfExists(link);
        assertTrue(Files.exists(file, NOFOLLOW));
        assertFalse(Files.exists(link, NOFOLLOW));
    }

    @Test
    public void deleteRecursiveIfExists_linkToDirWillDeleteNoFollow() throws Exception {

        Path dir = Files.createDir(dir1().resolve("dir"));
        Path file = Files.createFile(dir.resolve("file"));
        Path link = Files.createLink(dir1().resolve("link"), dir);
        assertTrue(Files.exists(dir, NOFOLLOW));
        assertTrue(Files.exists(file, NOFOLLOW));
        assertTrue(Files.exists(link, NOFOLLOW));

        Files.deleteRecursiveIfExists(link);
        assertTrue(Files.exists(dir, NOFOLLOW));
        assertTrue(Files.exists(file, NOFOLLOW));
        assertFalse(Files.exists(link, NOFOLLOW));
    }

    @Test
    public void setModificationTime() throws Exception {
        Instant expect = newInstant();
        Files.setLastModifiedTime(dir1(), NOFOLLOW, expect);
        Instant actual = getModificationTime(dir1(), NOFOLLOW);
        assertEquals(expect, actual);
    }

    @Test
    public void setModificationTime_linkFollow() throws Exception {
        Path file = Files.createFile(dir1().resolve("file"));
        Path link = Files.createLink(dir1().resolve("link"), file);

        Instant fileTime = newInstant();
        Instant linkTime = getModificationTime(link, NOFOLLOW);
        Files.setLastModifiedTime(link, FOLLOW, fileTime);

        assertEquals(fileTime, getModificationTime(file, NOFOLLOW));
        assertEquals(linkTime, getModificationTime(link, NOFOLLOW));
        assertNotEqual(fileTime, linkTime);
    }

    @Test
    public void setModificationTime_linkNoFollow() throws Exception {
        Path file = Files.createFile(dir1().resolve("file"));
        Path link = Files.createLink(dir1().resolve("link"), file);

        Instant fileTime = getModificationTime(file, NOFOLLOW);
        Instant linkTime = newInstant();

        Files.setLastModifiedTime(link, NOFOLLOW, linkTime);

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
            Path file,
            LinkOption option) throws IOException {
        return Files.stat(file, option).lastModifiedTime();
    }

    @Test
    public void setPermissions() throws Exception {
        List<Set<Permission>> permissions = asList(
                Permission.all(),
                Permission.read(),
                Permission.write(),
                Permission.execute());
        for (Set<Permission> expected : permissions) {
            Files.setPermissions(dir1(), expected);
            assertEquals(expected, Files.stat(dir1(), NOFOLLOW).permissions());
        }
    }

    @Test
    public void setPermissions_rawBits() throws Exception {
        int expected = stat(dir1().toByteArray()).mode();
        Files.setPermissions(dir1(), Files.stat(dir1(), NOFOLLOW).permissions());
        int actual = stat(dir1().toByteArray()).mode();
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
            Files.setPermissions(dir1(), Permission.all());
            Files.removePermissions(dir1(), permissions);

            Set<Permission> actual = Files.stat(dir1(), FOLLOW).permissions();
            Set<Permission> expected = new HashSet<>(Permission.all());
            expected.removeAll(permissions);
            assertEquals(expected, actual);
        }
    }

    @Test
    public void removePermissions_changeTargetNotLink() throws Exception {
        Permission perm = OWNER_READ;
        Path link = Files.createLink(dir1().resolve("link"), dir1());
        assertTrue(Files.stat(link, FOLLOW).permissions().contains(perm));
        assertTrue(Files.stat(link, NOFOLLOW).permissions().contains(perm));

        Files.removePermissions(link, singleton(perm));

        assertFalse(Files.stat(link, FOLLOW).permissions().contains(perm));
        assertTrue(Files.stat(link, NOFOLLOW).permissions().contains(perm));
    }

    @Test
    public void readDetectingCharset_utf8() throws Exception {
        Path file = Files.createFile(dir1().resolve("a"));
        Files.writeUtf8(file, "你好");
        assertEquals("", Files.readDetectingCharset(file, 0));
        assertEquals("你", Files.readDetectingCharset(file, 1));
        assertEquals("你好", Files.readDetectingCharset(file, 2));
        assertEquals("你好", Files.readDetectingCharset(file, 3));
    }

    @Test
    public void readDetectingCharset_iso88591() throws Exception {
        Path file = Files.createFile(dir1().resolve("a"));
        Files.write(file, "hello world", ISO_8859_1);
        assertEquals("", Files.readDetectingCharset(file, 0));
        assertEquals("h", Files.readDetectingCharset(file, 1));
        assertEquals("he", Files.readDetectingCharset(file, 2));
        assertEquals("hel", Files.readDetectingCharset(file, 3));
        assertEquals("hello world", Files.readDetectingCharset(file, 100));
    }

    private List<Path> sortByName(List<Path> files) throws IOException {
        Collections.sort(files, new Comparator<Path>() {
            @Override
            public int compare(Path a, Path b) {
                return a.name().toString().compareTo(b.name().toString());
            }
        });
        return files;
    }

}
