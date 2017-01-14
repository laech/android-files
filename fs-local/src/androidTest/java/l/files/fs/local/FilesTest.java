package l.files.fs.local;

import android.test.MoreAsserts;

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
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import l.files.fs.DirectoryNotEmpty;
import l.files.fs.Instant;
import l.files.fs.LinkOption;
import l.files.fs.Path;
import l.files.fs.Permission;
import l.files.fs.Stat;
import l.files.testing.fs.PathBaseTest;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.test.MoreAsserts.assertNotEqual;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Permission.OWNER_EXECUTE;
import static l.files.fs.Permission.OWNER_READ;
import static l.files.fs.Permission.OWNER_WRITE;
import static l.files.fs.local.LocalFileSystem.permissionsFromMode;
import static l.files.testing.fs.ExtendedFileSystem.UTF_8;

public final class FilesTest extends PathBaseTest {

    // TODO turn this into FileSystemTest

    private static final Random random = new Random();

    public FilesTest() {
        super(LocalFileSystem.INSTANCE);
    }

    public void test_can_handle_invalid_utf_8_path() throws Exception {

        byte[] bytes = {-19, -96, -67, -19, -80, -117};
        assertFalse(Arrays.equals(bytes.clone(), new String(bytes, UTF_8).getBytes(UTF_8)));

        Path dir = dir1().concat(bytes.clone());
        Path file = dir.concat("a");
        fs.createDir(dir);
        fs.createFile(file);

        assertTrue(fs.exists(dir, NOFOLLOW));
        assertTrue(fs.exists(file, NOFOLLOW));
        assertEquals(singleton(file), fs.list(dir, FOLLOW, new HashSet<>()));

        MoreAsserts.assertEquals(bytes.clone(), dir.name().toByteArray());
        assertEquals(new String(bytes.clone(), UTF_8), dir.name().toString());
        assertFalse(Arrays.equals(bytes.clone(), dir.name().toString().getBytes(UTF_8)));
    }

    public void test_isReadable_true() throws Exception {
        assertTrue(fs.isReadable(dir1()));
    }

    public void test_isReadable_false() throws Exception {
        fs.removePermissions(dir1(), Permission.read());
        assertFalse(fs.isReadable(dir1()));
    }

    public void test_isWritable_true() throws Exception {
        assertTrue(fs.isWritable(dir1()));
    }

    public void test_isWritable_false() throws Exception {
        fs.removePermissions(dir1(), Permission.write());
        assertFalse(fs.isWritable(dir1()));
    }

    public void test_isExecutable_true() throws Exception {
        assertTrue(fs.isExecutable(dir1()));
    }

    public void test_isExecutable_false() throws Exception {
        fs.removePermissions(dir1(), Permission.execute());
        assertFalse(fs.isExecutable(dir1()));
    }

    public void test_stat_symbolicLink() throws Exception {
        Path file = fs.createFile(dir1().concat("file"));
        Path link = fs.createSymbolicLink(dir1().concat("link"), file);
        assertFalse(fs.stat(file, NOFOLLOW).isSymbolicLink());
        assertFalse(fs.stat(link, FOLLOW).isSymbolicLink());
        assertTrue(fs.stat(link, NOFOLLOW).isSymbolicLink());
        assertEquals(fs.stat(file, NOFOLLOW), fs.stat(link, FOLLOW));
    }

    public void test_stat_modificationTime() throws Exception {
        linux.Stat stat = new linux.Stat();
        linux.Stat.stat(dir1().toByteArray(), stat);
        long expected = stat.st_mtime;
        long actual = fs.stat(dir1(), NOFOLLOW).lastModifiedTime().seconds();
        assertEquals(expected, actual);
    }

    public void test_stat_size() throws Exception {
        Path file = fs.createFile(dir1().concat("file"));
        fs.appendUtf8(file, "hello world");
        linux.Stat stat = new linux.Stat();
        linux.Stat.stat(file.toByteArray(), stat);
        long expected = stat.st_size;
        long actual = fs.stat(file, NOFOLLOW).size();
        assertEquals(expected, actual);
    }

    public void test_stat_isDirectory() throws Exception {
        assertTrue(fs.stat(dir1(), NOFOLLOW).isDirectory());
    }

    public void test_stat_isRegularFile() throws Exception {
        Path dir = fs.createFile(dir1().concat("dir"));
        assertTrue(fs.stat(dir, NOFOLLOW).isRegularFile());
    }

    public void test_getHierarchy_single() throws Exception {
        Path a = Path.fromString("/");
        assertEquals(singletonList(a), a.hierarchy());
    }

    public void test_getHierarchy_multi() throws Exception {
        Path a = Path.fromString("/a/b");
        List<Path> expected = asList(
                Path.fromString("/"),
                Path.fromString("/a"),
                Path.fromString("/a/b")
        );
        assertEquals(expected, a.hierarchy());
    }

    public void test_list_linkFollowSuccess() throws Exception {
        Path dir = fs.createDir(dir1().concat("dir"));
        Path a = fs.createFile(dir.concat("a"));
        Path b = fs.createDir(dir.concat("b"));
        Path c = fs.createSymbolicLink(dir.concat("c"), a);
        Path link = fs.createSymbolicLink(dir1().concat("link"), dir);

        List<Path> expected = asList(
                a.rebase(dir, link),
                b.rebase(dir, link),
                c.rebase(dir, link)
        );

        List<Path> actual = sortByName(fs.list(link, FOLLOW, new ArrayList<Path>()));
        assertEquals(expected, actual);
    }

    public void test_list() throws Exception {
        Path a = fs.createFile(dir1().concat("a"));
        Path b = fs.createDir(dir1().concat("b"));
        List<Path> expected = asList(a, b);
        List<Path> actual = sortByName(fs.list(dir1(), NOFOLLOW, new ArrayList<Path>()));
        assertEquals(expected, actual);
    }

    public void test_listDir_linkFollowSuccess() throws Exception {
        Path dir = fs.createDir(dir1().concat("dir"));
        Path a = fs.createFile(dir.concat("a"));
        fs.createDir(dir.concat("b"));
        fs.createSymbolicLink(dir.concat("c"), a);

        Path link = fs.createSymbolicLink(dir1().concat("link"), dir);
        List<Path> expected = singletonList(link.concat("b"));
        List<Path> actual = sortByName(fs.listDirs(link, FOLLOW, new ArrayList<Path>()));
        assertEquals(expected, actual);
    }

    public void test_listDir() throws Exception {
        fs.createFile(dir1().concat("a"));
        fs.createDir(dir1().concat("b"));
        fs.createFile(dir1().concat("c"));
        List<?> expected = singletonList(dir1().concat("b"));
        List<?> actual = sortByName(fs.listDirs(dir1(), NOFOLLOW, new ArrayList<Path>()));
        assertEquals(expected, actual);
    }

    public void test_output_created_file_has_correct_permissions() throws Exception {
        Path a = dir1().concat("a");
        Path b = dir1().concat("b");

        new FileOutputStream(a.toString()).close();
        fs.newOutputStream(b, false).close();

        assertEquals(
                fs.stat(a, NOFOLLOW).permissions(),
                fs.stat(b, NOFOLLOW).permissions()
        );
    }

    public void test_output_append_false() throws Exception {
        test_output("a", "b", "b", new OutputProvider() {
            @Override
            public OutputStream open(Path file) throws IOException {
                return fs.newOutputStream(file, false);
            }
        });
    }

    public void test_output_append_true() throws Exception {
        test_output("a", "b", "ab", new OutputProvider() {
            @Override
            public OutputStream open(Path file) throws IOException {
                return fs.newOutputStream(file, true);
            }
        });
    }

    private void test_output(
            String initial,
            String write,
            String result,
            OutputProvider provider) throws Exception {

        Path file = fs.createFile(dir1().concat("file"));
        fs.appendUtf8(file, initial);
        OutputStream out = provider.open(file);
        try {
            out.write(write.getBytes(UTF_8));
        } finally {
            out.close();
        }
        assertEquals(result, fs.readAllUtf8(file));
    }

    private interface OutputProvider {
        OutputStream open(Path file) throws IOException;
    }

    public void test_output_createWithCorrectPermission() throws Exception {

        Path expected = dir1().concat("expected");
        Path actual = dir1().concat("actual");

        assertTrue(expected.toFile().createNewFile());
        fs.newOutputStream(actual, false).close();

        linux.Stat expectedStat = new linux.Stat();
        linux.Stat.stat(expected.toByteArray(), expectedStat);

        linux.Stat actualStat = new linux.Stat();
        linux.Stat.stat(actual.toByteArray(), actualStat);

        assertEquals(expectedStat.st_mode, actualStat.st_mode);
    }

    public void test_input() throws Exception {
        Path file = fs.createFile(dir1().concat("a"));
        String expected = "hello\nworld\n";
        fs.appendUtf8(file, expected);
        assertEquals(expected, fs.readAllUtf8(file));
    }

    public void test_input_linkFollowSuccess() throws Exception {
        Path target = fs.createFile(dir1().concat("target"));
        Path link = fs.createSymbolicLink(dir1().concat("link"), target);
        fs.newInputStream(link).close();
    }

    public void test_input_cannotUseAfterClose() throws Exception {
        Path file = fs.createFile(dir1().concat("a"));
        InputStream in = fs.newInputStream(file);
        try {
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
        } finally {
            in.close();
        }
    }

    public void test_exists_true() throws Exception {
        assertTrue(fs.exists(dir1(), NOFOLLOW));
    }

    public void test_exists_false() throws Exception {
        assertFalse(fs.exists(dir1().concat("a"), NOFOLLOW));
    }

    public void test_exists_checkLinkNotTarget() throws Exception {
        Path target = dir1().concat("target");
        Path link = fs.createSymbolicLink(dir1().concat("link"), target);
        assertFalse(fs.exists(target, NOFOLLOW));
        assertFalse(fs.exists(link, FOLLOW));
        assertTrue(fs.exists(link, NOFOLLOW));
    }

    public void test_readString() throws Exception {
        Path file = fs.createFile(dir1().concat("file"));
        String expected = "a\nb\tc";
        fs.appendUtf8(file, expected);
        assertEquals(expected, fs.readAllUtf8(file));
    }

    public void test_createFile() throws Exception {
        Path file = dir1().concat("a");
        fs.createFile(file);
        assertTrue(fs.stat(file, NOFOLLOW).isRegularFile());
    }

    public void test_createFile_correctPermissions() throws Exception {
        Path actual = dir1().concat("a");
        fs.createFile(actual);

        java.io.File expected = new java.io.File(dir1().toString(), "b");
        assertTrue(expected.createNewFile());

        linux.Stat stat = new linux.Stat();
        linux.Stat.lstat(expected.getPath().getBytes(), stat);

        assertEquals(expected.canRead(), fs.isReadable(actual));
        assertEquals(expected.canWrite(), fs.isWritable(actual));
        assertEquals(expected.canExecute(), fs.isExecutable(actual));
        assertEquals(
                permissionsFromMode(stat.st_mode),
                fs.stat(actual, NOFOLLOW).permissions()
        );
    }

    public void test_createDirectory() throws Exception {
        Path dir = dir1().concat("a");
        fs.createDir(dir);
        assertTrue(fs.stat(dir, NOFOLLOW).isDirectory());
    }

    public void test_createDirectory_correctDefaultPermissions() throws Exception {
        Path actual = dir1().concat("a");
        fs.createDir(actual);

        java.io.File expected = new java.io.File(dir1().toString(), "b");
        assertTrue(expected.mkdir());

        linux.Stat stat = new linux.Stat();
        linux.Stat.lstat(expected.getPath().getBytes(), stat);

        assertEquals(expected.canRead(), fs.isReadable(actual));
        assertEquals(expected.canWrite(), fs.isWritable(actual));
        assertEquals(expected.canExecute(), fs.isExecutable(actual));
        assertEquals(
                permissionsFromMode(stat.st_mode),
                fs.stat(actual, NOFOLLOW).permissions()
        );
    }

    public void test_createDirectory_withSpecifiedPermissions() throws Exception {

        for (Set<Permission> permissions : asList(
                Permission.none(),
                EnumSet.of(OWNER_READ),
                EnumSet.of(OWNER_WRITE),
                EnumSet.of(OWNER_EXECUTE),
                EnumSet.of(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE))) {

            String name = String.valueOf(Math.random());
            Path dir = fs.createDir(dir1().concat(name), permissions);
            Stat stat = fs.stat(dir, NOFOLLOW);
            assertEquals(permissions, stat.permissions());
        }
    }

    public void test_createDirectories() throws Exception {
        fs.createDirs(dir1().concat("a/b/c"));
        assertTrue(fs.stat(dir1().concat("a/b/c"), NOFOLLOW).isDirectory());
        assertTrue(fs.stat(dir1().concat("a/b"), NOFOLLOW).isDirectory());
        assertTrue(fs.stat(dir1().concat("a/"), NOFOLLOW).isDirectory());
    }

    public void test_createSymbolicLink() throws Exception {
        Path link = fs.createSymbolicLink(dir1().concat("link"), dir1());
        assertTrue(fs.stat(link, NOFOLLOW).isSymbolicLink());
        assertEquals(dir1(), fs.readSymbolicLink(link));
    }

    public void test_stat_followLink() throws Exception {
        Path child = fs.createSymbolicLink(dir1().concat("a"), dir1());
        Stat expected = fs.stat(dir1(), NOFOLLOW);
        Stat actual = fs.stat(child, FOLLOW);
        assertTrue(actual.isDirectory());
        assertFalse(actual.isSymbolicLink());
        assertEquals(expected, actual);
    }

    public void test_stat_noFollowLink() throws Exception {
        Path child = fs.createSymbolicLink(dir1().concat("a"), dir1());
        Stat actual = fs.stat(child, NOFOLLOW);
        assertTrue(actual.isSymbolicLink());
        assertFalse(actual.isDirectory());
        assertNotEqual(fs.stat(dir1(), NOFOLLOW), actual);
    }

    public void test_moveTo_moveLinkNotTarget() throws Exception {
        Path target = fs.createFile(dir1().concat("target"));
        Path src = fs.createSymbolicLink(dir1().concat("src"), target);
        Path dst = dir1().concat("dst");
        fs.move(src, dst);
        assertFalse(fs.exists(src, NOFOLLOW));
        assertTrue(fs.exists(dst, NOFOLLOW));
        assertTrue(fs.exists(target, NOFOLLOW));
        assertEquals(target, fs.readSymbolicLink(dst));
    }

    public void test_moveTo_fileToNonExistingFile() throws Exception {
        Path src = dir1().concat("src");
        Path dst = dir1().concat("dst");
        fs.appendUtf8(src, "src");
        fs.move(src, dst);
        assertFalse(fs.exists(src, NOFOLLOW));
        assertTrue(fs.exists(dst, NOFOLLOW));
        assertEquals("src", fs.readAllUtf8(dst));
    }

    public void test_moveTo_directoryToNonExistingDirectory() throws Exception {
        Path src = dir1().concat("src");
        Path dst = dir1().concat("dst");
        fs.createDirs(src.concat("a"));
        fs.move(src, dst);
        assertFalse(fs.exists(src, NOFOLLOW));
        assertTrue(fs.exists(dst, NOFOLLOW));
        assertTrue(fs.exists(dst.concat("a"), NOFOLLOW));
    }

    public void test_delete_symbolicLink() throws Exception {
        Path link = dir1().concat("link");
        fs.createSymbolicLink(link, dir1());
        assertTrue(fs.exists(link, NOFOLLOW));
        fs.delete(link);
        assertFalse(fs.exists(link, NOFOLLOW));
    }

    public void test_delete_file() throws Exception {
        Path file = dir1().concat("file");
        fs.createFile(file);
        assertTrue(fs.exists(file, NOFOLLOW));
        fs.delete(file);
        assertFalse(fs.exists(file, NOFOLLOW));
    }

    public void test_delete_emptyDirectory() throws Exception {
        Path directory = dir1().concat("directory");
        fs.createDir(directory);
        assertTrue(fs.exists(directory, NOFOLLOW));
        fs.delete(directory);
        assertFalse(fs.exists(directory, NOFOLLOW));
    }

    public void test_deleteRecursive_symbolicLink() throws Exception {
        Path dir = fs.createDir(dir1().concat("dir"));
        Path a = fs.createFile(dir.concat("a"));
        Path link = fs.createSymbolicLink(dir1().concat("link"), dir);
        assertTrue(fs.exists(link, NOFOLLOW));
        fs.deleteRecursive(link);
        assertFalse(fs.exists(link, NOFOLLOW));
        assertTrue(fs.exists(dir, NOFOLLOW));
        assertTrue(fs.exists(a, NOFOLLOW));
    }

    public void test_deleteRecursive_file() throws Exception {
        Path file = fs.createFile(dir1().concat("file"));
        assertTrue(fs.exists(file, NOFOLLOW));
        fs.deleteRecursive(file);
        assertFalse(fs.exists(file, NOFOLLOW));
    }

    public void test_deleteRecursive_emptyDirectory() throws Exception {
        Path dir = fs.createDir(dir1().concat("dir"));
        assertTrue(fs.exists(dir, NOFOLLOW));
        fs.delete(dir);
        assertFalse(fs.exists(dir, NOFOLLOW));
    }

    public void test_deleteRecursive_nonEmptyDirectory() throws Exception {
        Path dir = fs.createDir(dir1().concat("dir"));
        Path sub = fs.createDir(dir.concat("sub"));
        Path a = fs.createFile(dir.concat("a"));
        Path b = fs.createFile(sub.concat("b"));
        assertTrue(fs.exists(dir, NOFOLLOW));
        assertTrue(fs.exists(sub, NOFOLLOW));
        assertTrue(fs.exists(a, NOFOLLOW));
        assertTrue(fs.exists(b, NOFOLLOW));
        fs.deleteRecursive(dir);
        assertFalse(fs.exists(dir, NOFOLLOW));
    }

    public void test_deleteIfExists_nonExist_willIgnore() throws Exception {
        fs.deleteIfExists(dir1().concat("a"));
    }

    public void test_deleteIfExists_fileExist_willDelete() throws Exception {
        Path file = fs.createFile(dir1().concat("a"));
        assertTrue(fs.exists(file, NOFOLLOW));
        fs.deleteIfExists(file);
        assertFalse(fs.exists(file, NOFOLLOW));
    }

    public void test_deleteIfExists_emptyDirExist_willDelete() throws Exception {
        Path dir = fs.createDir(dir1().concat("a"));
        assertTrue(fs.exists(dir, NOFOLLOW));
        fs.deleteIfExists(dir);
        assertFalse(fs.exists(dir, NOFOLLOW));
    }

    public void test_deleteIfExists_nonEmptyDirExist_willError() throws Exception {
        Path dir = fs.createDir(dir1().concat("a"));
        Path file = fs.createFile(dir.concat("1"));
        assertTrue(fs.exists(dir, NOFOLLOW));
        try {
            fs.deleteIfExists(dir);
        } catch (DirectoryNotEmpty ignore) {
        }
        assertTrue(fs.exists(dir, NOFOLLOW));
        assertTrue(fs.exists(file, NOFOLLOW));
    }

    public void test_deleteRecursiveIfExists_nonExistWillIgnore() throws Exception {
        fs.deleteRecursiveIfExists(dir1().concat("nonExist"));
    }

    public void test_deleteRecursiveIfExists_emptyDirWillDelete() throws Exception {

        Path dir = fs.createDir(dir1().concat("dir"));
        assertTrue(fs.exists(dir, NOFOLLOW));

        fs.deleteRecursiveIfExists(dir);
        assertFalse(fs.exists(dir, NOFOLLOW));
    }

    public void test_deleteRecursiveIfExists_nonEmptyDirWillDelete() throws Exception {

        Path dir = fs.createDir(dir1().concat("dir"));
        Path file = fs.createFile(dir.concat("child"));
        assertTrue(fs.exists(dir, NOFOLLOW));
        assertTrue(fs.exists(file, NOFOLLOW));

        fs.deleteRecursiveIfExists(dir);
        assertFalse(fs.exists(dir, NOFOLLOW));
    }

    public void test_deleteRecursiveIfExists_fileWillDelete() throws Exception {

        Path file = fs.createFile(dir1().concat("file"));
        assertTrue(fs.exists(file, NOFOLLOW));

        fs.deleteRecursiveIfExists(file);
        assertFalse(fs.exists(file, NOFOLLOW));
    }

    public void test_deleteRecursiveIfExists_linkToFileWillDeleteNoFollow() throws Exception {

        Path file = fs.createFile(dir1().concat("file"));
        Path link = fs.createSymbolicLink(dir1().concat("link"), file);
        assertTrue(fs.exists(file, NOFOLLOW));
        assertTrue(fs.exists(link, NOFOLLOW));

        fs.deleteRecursiveIfExists(link);
        assertTrue(fs.exists(file, NOFOLLOW));
        assertFalse(fs.exists(link, NOFOLLOW));
    }

    public void test_deleteRecursiveIfExists_linkToDirWillDeleteNoFollow() throws Exception {

        Path dir = fs.createDir(dir1().concat("dir"));
        Path file = fs.createFile(dir.concat("file"));
        Path link = fs.createSymbolicLink(dir1().concat("link"), dir);
        assertTrue(fs.exists(dir, NOFOLLOW));
        assertTrue(fs.exists(file, NOFOLLOW));
        assertTrue(fs.exists(link, NOFOLLOW));

        fs.deleteRecursiveIfExists(link);
        assertTrue(fs.exists(dir, NOFOLLOW));
        assertTrue(fs.exists(file, NOFOLLOW));
        assertFalse(fs.exists(link, NOFOLLOW));
    }

    public void test_setModificationTime() throws Exception {
        Instant expect = newInstant();
        fs.setLastModifiedTime(dir1(), NOFOLLOW, expect);
        Instant actual = getModificationTime(dir1(), NOFOLLOW);
        assertEquals(expect, actual);
    }

    public void test_setModificationTime_linkFollow() throws Exception {
        Path file = fs.createFile(dir1().concat("file"));
        Path link = fs.createSymbolicLink(dir1().concat("link"), file);

        Instant fileTime = newInstant();
        Instant linkTime = getModificationTime(link, NOFOLLOW);
        fs.setLastModifiedTime(link, FOLLOW, fileTime);

        assertEquals(fileTime, getModificationTime(file, NOFOLLOW));
        assertEquals(linkTime, getModificationTime(link, NOFOLLOW));
        assertNotEqual(fileTime, linkTime);
    }

    public void test_setModificationTime_linkNoFollow() throws Exception {
        Path file = fs.createFile(dir1().concat("file"));
        Path link = fs.createSymbolicLink(dir1().concat("link"), file);

        Instant fileTime = getModificationTime(file, NOFOLLOW);
        Instant linkTime = newInstant();

        fs.setLastModifiedTime(link, NOFOLLOW, linkTime);

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
        return fs.stat(file, option).lastModifiedTime();
    }

    public void test_setPermissions() throws Exception {
        List<Set<Permission>> permissions = asList(
                Permission.all(),
                Permission.read(),
                Permission.write(),
                Permission.execute());
        for (Set<Permission> expected : permissions) {
            fs.setPermissions(dir1(), expected);
            assertEquals(expected, fs.stat(dir1(), NOFOLLOW).permissions());
        }
    }

    public void test_setPermissions_rawBits() throws Exception {

        linux.Stat stat = new linux.Stat();
        linux.Stat.stat(dir1().toByteArray(), stat);
        int expected = stat.st_mode;

        fs.setPermissions(dir1(), fs.stat(dir1(), NOFOLLOW).permissions());
        linux.Stat.stat(dir1().toByteArray(), stat);
        int actual = stat.st_mode;

        assertEquals(expected, actual);
    }

    public void test_removePermissions() throws Exception {
        List<Set<Permission>> combinations = asList(
                Permission.all(),
                Permission.read(),
                Permission.write(),
                Permission.execute());
        for (Set<Permission> permissions : combinations) {
            fs.setPermissions(dir1(), Permission.all());
            fs.removePermissions(dir1(), permissions);

            Set<Permission> actual = fs.stat(dir1(), FOLLOW).permissions();
            Set<Permission> expected = new HashSet<>(Permission.all());
            expected.removeAll(permissions);
            assertEquals(expected, actual);
        }
    }

    public void test_removePermissions_changeTargetNotLink() throws Exception {
        Permission perm = OWNER_READ;
        Path link = fs.createSymbolicLink(dir1().concat("link"), dir1());
        assertTrue(fs.stat(link, FOLLOW).permissions().contains(perm));
        assertTrue(fs.stat(link, NOFOLLOW).permissions().contains(perm));

        fs.removePermissions(link, singleton(perm));

        assertFalse(fs.stat(link, FOLLOW).permissions().contains(perm));
        assertTrue(fs.stat(link, NOFOLLOW).permissions().contains(perm));
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
