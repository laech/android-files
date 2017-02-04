package l.files.fs;

import android.test.MoreAsserts;

import org.junit.Test;

import java.io.File;
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

import l.files.testing.fs.PathBaseTest;
import l.files.testing.fs.Paths;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.test.MoreAsserts.assertNotEqual;
import static com.google.common.base.Charsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Permission.OWNER_EXECUTE;
import static l.files.fs.Permission.OWNER_READ;
import static l.files.fs.Permission.OWNER_WRITE;
import static l.files.fs.Stat.lstat;
import static l.files.fs.Stat.stat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public final class FilesTest extends PathBaseTest {

    // TODO turn this into PathTest

    private static final Random random = new Random();

    @Test
    public void can_handle_invalid_utf_8_path() throws Exception {

        byte[] bytes = {-19, -96, -67, -19, -80, -117};
        assertFalse(Arrays.equals(bytes.clone(), new String(bytes, UTF_8).getBytes(UTF_8)));

        Path dir = dir1().concat(bytes.clone());
        Path file = dir.concat("a");
        dir.createDirectory();
        file.createFile();

        assertTrue(dir.exists(NOFOLLOW));
        assertTrue(file.exists(NOFOLLOW));
        assertEquals(singleton(file), dir.list(FOLLOW, new HashSet<>()));

        MoreAsserts.assertEquals(bytes.clone(), dir.name().toByteArray());
        assertEquals(new String(bytes.clone(), UTF_8), dir.name().toString());
        assertFalse(Arrays.equals(bytes.clone(), dir.name().toString().getBytes(UTF_8)));
    }

    @Test
    public void isReadable_true() throws Exception {
        assertTrue(dir1().isReadable());
    }

    @Test
    public void isReadable_false() throws Exception {
        Paths.removePermissions(dir1(), Permission.read());
        assertFalse(dir1().isReadable());
    }

    @Test
    public void isWritable_true() throws Exception {
        assertTrue(dir1().isWritable());
    }


    @Test
    public void isWritable_false() throws Exception {
        Paths.removePermissions(dir1(), Permission.write());
        assertFalse(dir1().isWritable());
    }

    @Test
    public void isExecutable_true() throws Exception {
        assertTrue(dir1().isExecutable());
    }

    @Test
    public void isExecutable_false() throws Exception {
        Paths.removePermissions(dir1(), Permission.execute());
        assertFalse(dir1().isExecutable());
    }

    @Test
    public void stat_symbolicLink() throws Exception {
        Path file = dir1().concat("file").createFile();
        Path link = dir1().concat("link").createSymbolicLink(file);
        assertFalse(file.stat(NOFOLLOW).isSymbolicLink());
        assertFalse(link.stat(FOLLOW).isSymbolicLink());
        assertTrue(link.stat(NOFOLLOW).isSymbolicLink());
        assertEquals(file.stat(NOFOLLOW), link.stat(FOLLOW));
    }

    @Test
    public void stat_modificationTime() throws Exception {
        Stat stat = stat(dir1().toByteArray());
        long expected = stat.lastModifiedEpochSecond();
        long actual = dir1().stat(NOFOLLOW).lastModifiedTime().seconds();
        assertEquals(expected, actual);
    }

    @Test
    public void stat_size() throws Exception {
        Path file = dir1().concat("file").createFile();
        Paths.appendUtf8(file, "hello world");
        Stat stat = stat(file.toByteArray());
        long expected = stat.size();
        long actual = file.stat(NOFOLLOW).size();
        assertEquals(expected, actual);
    }

    @Test
    public void stat_isDirectory() throws Exception {
        assertTrue(dir1().stat(NOFOLLOW).isDirectory());
    }

    @Test
    public void stat_isRegularFile() throws Exception {
        Path dir = dir1().concat("dir").createFile();
        assertTrue(dir.stat(NOFOLLOW).isRegularFile());
    }

    @Test
    public void list_linkFollowSuccess() throws Exception {
        Path dir = dir1().concat("dir").createDirectory();
        Path a = dir.concat("a").createFile();
        Path b = dir.concat("b").createDirectory();
        Path c = dir.concat("c").createSymbolicLink(a);
        Path link = dir1().concat("link").createSymbolicLink(dir);

        List<Path> expected = asList(
                a.rebase(dir, link),
                b.rebase(dir, link),
                c.rebase(dir, link)
        );

        List<Path> actual = sortByName(link.list(FOLLOW, new ArrayList<Path>()));
        assertEquals(expected, actual);
    }

    @Test
    public void list() throws Exception {
        Path a = dir1().concat("a").createFile();
        Path b = dir1().concat("b").createDirectory();
        List<Path> expected = asList(a, b);
        List<Path> actual = sortByName(dir1().list(NOFOLLOW, new ArrayList<Path>()));
        assertEquals(expected, actual);
    }

    @Test
    public void listDir_linkFollowSuccess() throws Exception {
        Path dir = dir1().concat("dir").createDirectory();
        Path a = dir.concat("a").createFile();
        dir.concat("b").createDirectory();
        dir.concat("c").createSymbolicLink(a);

        Path link = dir1().concat("link").createSymbolicLink(dir);
        List<Path> expected = singletonList(link.concat("b"));
        List<Path> actual = sortByName(Paths.listDirectories(link, FOLLOW, new ArrayList<Path>()));
        assertEquals(expected, actual);
    }

    @Test
    public void listDir() throws Exception {
        dir1().concat("a").createFile();
        dir1().concat("b").createDirectory();
        dir1().concat("c").createFile();
        List<?> expected = singletonList(dir1().concat("b"));
        List<?> actual = sortByName(Paths.listDirectories(dir1(), NOFOLLOW, new ArrayList<Path>()));
        assertEquals(expected, actual);
    }

    @Test
    public void output_created_file_has_correct_permissions() throws Exception {
        Path a = dir1().concat("a");
        Path b = dir1().concat("b");

        new FileOutputStream(a.toString()).close();
        b.newOutputStream(false).close();

        assertEquals(
                a.stat(NOFOLLOW).permissions(),
                b.stat(NOFOLLOW).permissions()
        );
    }

    @Test
    public void output_append_false() throws Exception {
        test_output("a", "b", "b", new OutputProvider() {
            @Override
            public OutputStream open(Path file) throws IOException {
                return file.newOutputStream(false);
            }
        });
    }

    @Test
    public void output_append_true() throws Exception {
        test_output("a", "b", "ab", new OutputProvider() {
            @Override
            public OutputStream open(Path file) throws IOException {
                return file.newOutputStream(true);
            }
        });
    }

    private void test_output(
            String initial,
            String write,
            String result,
            OutputProvider provider) throws Exception {

        Path file = dir1().concat("file").createFile();
        Paths.appendUtf8(file, initial);
        OutputStream out = provider.open(file);
        try {
            out.write(write.getBytes(UTF_8));
        } finally {
            out.close();
        }
        assertEquals(result, Paths.readAllUtf8(file));
    }

    private interface OutputProvider {
        OutputStream open(Path file) throws IOException;
    }

    @Test
    public void output_createWithCorrectPermission() throws Exception {

        Path expected = dir1().concat("expected");
        Path actual = dir1().concat("actual");

        assertTrue(new File(expected.toString()).createNewFile());
        actual.newOutputStream(false).close();

        Stat expectedStat = stat(expected.toByteArray());
        Stat actualStat = stat(actual.toByteArray());
        assertEquals(expectedStat.mode(), actualStat.mode());
    }

    @Test
    public void input() throws Exception {
        Path file = dir1().concat("a").createFile();
        String expected = "hello\nworld\n";
        Paths.appendUtf8(file, expected);
        assertEquals(expected, Paths.readAllUtf8(file));
    }

    @Test
    public void input_linkFollowSuccess() throws Exception {
        Path target = dir1().concat("target").createFile();
        Path link = dir1().concat("link").createSymbolicLink(target);
        link.newInputStream().close();
    }

    @Test
    public void input_cannotUseAfterClose() throws Exception {
        Path file = dir1().concat("a").createFile();
        InputStream in = file.newInputStream();
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

    @Test
    public void exists_true() throws Exception {
        assertTrue(dir1().exists(NOFOLLOW));
    }

    @Test
    public void exists_false() throws Exception {
        assertFalse(dir1().concat("a").exists(NOFOLLOW));
    }

    @Test
    public void exists_checkLinkNotTarget() throws Exception {
        Path target = dir1().concat("target");
        Path link = dir1().concat("link").createSymbolicLink(target);
        assertFalse(target.exists(NOFOLLOW));
        assertFalse(link.exists(FOLLOW));
        assertTrue(link.exists(NOFOLLOW));
    }

    @Test
    public void readString() throws Exception {
        Path file = dir1().concat("file").createFile();
        String expected = "a\nb\tc";
        Paths.appendUtf8(file, expected);
        assertEquals(expected, Paths.readAllUtf8(file));
    }

    @Test
    public void createFile() throws Exception {
        Path file = dir1().concat("a");
        file.createFile();
        assertTrue(file.stat(NOFOLLOW).isRegularFile());
    }

    @Test
    public void createFile_correctPermissions() throws Exception {
        Path actual = dir1().concat("a");
        actual.createFile();

        File expected = new File(dir1().toString(), "b");
        assertTrue(expected.createNewFile());

        Stat stat = lstat(expected.getPath().getBytes());

        assertEquals(expected.canRead(), actual.isReadable());
        assertEquals(expected.canWrite(), actual.isWritable());
        assertEquals(expected.canExecute(), actual.isExecutable());
        assertEquals(stat.permissions(), actual.stat(NOFOLLOW).permissions());
    }

    @Test
    public void createDirectory() throws Exception {
        Path dir = dir1().concat("a");
        dir.createDirectory();
        assertTrue(dir.stat(NOFOLLOW).isDirectory());
    }

    @Test
    public void createDirectory_correctDefaultPermissions() throws Exception {
        Path actual = dir1().concat("a");
        actual.createDirectory();

        File expected = new File(dir1().toString(), "b");
        assertTrue(expected.mkdir());

        Stat stat = lstat(expected.getPath().getBytes());

        assertEquals(expected.canRead(), actual.isReadable());
        assertEquals(expected.canWrite(), actual.isWritable());
        assertEquals(expected.canExecute(), actual.isExecutable());
        assertEquals(stat.permissions(), actual.stat(NOFOLLOW).permissions());
    }

    @Test
    public void createDirectory_withSpecifiedPermissions() throws Exception {

        for (Set<Permission> permissions : asList(
                Permission.none(),
                EnumSet.of(OWNER_READ),
                EnumSet.of(OWNER_WRITE),
                EnumSet.of(OWNER_EXECUTE),
                EnumSet.of(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE))) {

            String name = String.valueOf(Math.random());
            Path dir = dir1().concat(name).createDirectory(permissions);
            Stat stat = dir.stat(NOFOLLOW);
            assertEquals(permissions, stat.permissions());
        }
    }

    @Test
    public void createDirectories() throws Exception {
        dir1().concat("a/b/c").createDirectories();
        assertTrue(dir1().concat("a/b/c").stat(NOFOLLOW).isDirectory());
        assertTrue(dir1().concat("a/b").stat(NOFOLLOW).isDirectory());
        assertTrue(dir1().concat("a/").stat(NOFOLLOW).isDirectory());
    }

    @Test
    public void createSymbolicLink() throws Exception {
        Path link = dir1().concat("link").createSymbolicLink(dir1());
        assertTrue(link.stat(NOFOLLOW).isSymbolicLink());
        assertEquals(dir1(), link.readSymbolicLink());
    }

    @Test
    public void stat_followLink() throws Exception {
        Path child = dir1().concat("a").createSymbolicLink(dir1());
        Stat expected = dir1().stat(NOFOLLOW);
        Stat actual = child.stat(FOLLOW);
        assertTrue(actual.isDirectory());
        assertFalse(actual.isSymbolicLink());
        assertEquals(expected, actual);
    }

    @Test
    public void stat_noFollowLink() throws Exception {
        Path child = dir1().concat("a").createSymbolicLink(dir1());
        Stat actual = child.stat(NOFOLLOW);
        assertTrue(actual.isSymbolicLink());
        assertFalse(actual.isDirectory());
        assertNotEqual(dir1().stat(NOFOLLOW), actual);
    }

    @Test
    public void moveTo_moveLinkNotTarget() throws Exception {
        Path target = dir1().concat("target").createFile();
        Path src = dir1().concat("src").createSymbolicLink(target);
        Path dst = dir1().concat("dst");
        src.move(dst);
        assertFalse(src.exists(NOFOLLOW));
        assertTrue(dst.exists(NOFOLLOW));
        assertTrue(target.exists(NOFOLLOW));
        assertEquals(target, dst.readSymbolicLink());
    }

    @Test
    public void moveTo_fileToNonExistingFile() throws Exception {
        Path src = dir1().concat("src");
        Path dst = dir1().concat("dst");
        Paths.appendUtf8(src, "src");
        src.move(dst);
        assertFalse(src.exists(NOFOLLOW));
        assertTrue(dst.exists(NOFOLLOW));
        assertEquals("src", Paths.readAllUtf8(dst));
    }

    @Test
    public void moveTo_directoryToNonExistingDirectory() throws Exception {
        Path src = dir1().concat("src");
        Path dst = dir1().concat("dst");
        src.concat("a").createDirectories();
        src.move(dst);
        assertFalse(src.exists(NOFOLLOW));
        assertTrue(dst.exists(NOFOLLOW));
        assertTrue(dst.concat("a").exists(NOFOLLOW));
    }

    @Test
    public void delete_symbolicLink() throws Exception {
        Path link = dir1().concat("link");
        link.createSymbolicLink(dir1());
        assertTrue(link.exists(NOFOLLOW));
        link.delete();
        assertFalse(link.exists(NOFOLLOW));
    }

    @Test
    public void delete_file() throws Exception {
        Path file = dir1().concat("file");
        file.createFile();
        assertTrue(file.exists(NOFOLLOW));
        file.delete();
        assertFalse(file.exists(NOFOLLOW));
    }

    @Test
    public void delete_emptyDirectory() throws Exception {
        Path directory = dir1().concat("directory");
        directory.createDirectory();
        assertTrue(directory.exists(NOFOLLOW));
        directory.delete();
        assertFalse(directory.exists(NOFOLLOW));
    }

    @Test
    public void deleteRecursive_symbolicLink() throws Exception {
        Path dir = dir1().concat("dir").createDirectory();
        Path a = dir.concat("a").createFile();
        Path link = dir1().concat("link").createSymbolicLink(dir);
        assertTrue(link.exists(NOFOLLOW));
        Paths.deleteRecursive(link);
        assertFalse(link.exists(NOFOLLOW));
        assertTrue(dir.exists(NOFOLLOW));
        assertTrue(a.exists(NOFOLLOW));
    }

    @Test
    public void deleteRecursive_file() throws Exception {
        Path file = dir1().concat("file").createFile();
        assertTrue(file.exists(NOFOLLOW));
        Paths.deleteRecursive(file);
        assertFalse(file.exists(NOFOLLOW));
    }

    @Test
    public void deleteRecursive_emptyDirectory() throws Exception {
        Path dir = dir1().concat("dir").createDirectory();
        assertTrue(dir.exists(NOFOLLOW));
        dir.delete();
        assertFalse(dir.exists(NOFOLLOW));
    }

    public void test_deleteRecursive_nonEmptyDirectory() throws Exception {

    @Test
    public void deleteRecursive_nonEmptyDirectory() throws Exception {
        Path dir = dir1().concat("dir").createDirectory();
        Path sub = dir.concat("sub").createDirectory();
        Path a = dir.concat("a").createFile();
        Path b = sub.concat("b").createFile();
        assertTrue(dir.exists(NOFOLLOW));
        assertTrue(sub.exists(NOFOLLOW));
        assertTrue(a.exists(NOFOLLOW));
        assertTrue(b.exists(NOFOLLOW));
        Paths.deleteRecursive(dir);
        assertFalse(dir.exists(NOFOLLOW));
    }

    @Test
    public void deleteIfExists_nonExist_willIgnore() throws Exception {
        Paths.deleteIfExists(dir1().concat("a"));
    }

    @Test
    public void deleteIfExists_fileExist_willDelete() throws Exception {
        Path file = dir1().concat("a").createFile();
        assertTrue(file.exists(NOFOLLOW));
        Paths.deleteIfExists(file);
        assertFalse(file.exists(NOFOLLOW));
    }

    @Test
    public void deleteIfExists_emptyDirExist_willDelete() throws Exception {
        Path dir = dir1().concat("a").createDirectory();
        assertTrue(dir.exists(NOFOLLOW));
        Paths.deleteIfExists(dir);
        assertFalse(dir.exists(NOFOLLOW));
    }

    @Test
    public void deleteIfExists_nonEmptyDirExist_willError() throws Exception {
        Path dir = dir1().concat("a").createDirectory();
        Path file = dir.concat("1").createFile();
        assertTrue(dir.exists(NOFOLLOW));
        try {
            Paths.deleteIfExists(dir);
        } catch (DirectoryNotEmpty ignore) {
        }
        assertTrue(dir.exists(NOFOLLOW));
        assertTrue(file.exists(NOFOLLOW));
    }

    @Test
    public void deleteRecursiveIfExists_nonExistWillIgnore() throws Exception {
        Paths.deleteRecursiveIfExists(dir1().concat("nonExist"));
    }

    @Test
    public void deleteRecursiveIfExists_emptyDirWillDelete() throws Exception {

        Path dir = dir1().concat("dir").createDirectory();
        assertTrue(dir.exists(NOFOLLOW));

        Paths.deleteRecursiveIfExists(dir);
        assertFalse(dir.exists(NOFOLLOW));
    }

    @Test
    public void deleteRecursiveIfExists_nonEmptyDirWillDelete() throws Exception {

        Path dir = dir1().concat("dir").createDirectory();
        Path file = dir.concat("child").createFile();
        assertTrue(dir.exists(NOFOLLOW));
        assertTrue(file.exists(NOFOLLOW));

        Paths.deleteRecursiveIfExists(dir);
        assertFalse(dir.exists(NOFOLLOW));
    }

    @Test
    public void deleteRecursiveIfExists_fileWillDelete() throws Exception {

        Path file = dir1().concat("file").createFile();
        assertTrue(file.exists(NOFOLLOW));

        Paths.deleteRecursiveIfExists(file);
        assertFalse(file.exists(NOFOLLOW));
    }

    @Test
    public void deleteRecursiveIfExists_linkToFileWillDeleteNoFollow() throws Exception {

        Path file = dir1().concat("file").createFile();
        Path link = dir1().concat("link").createSymbolicLink(file);
        assertTrue(file.exists(NOFOLLOW));
        assertTrue(link.exists(NOFOLLOW));

        Paths.deleteRecursiveIfExists(link);
        assertTrue(file.exists(NOFOLLOW));
        assertFalse(link.exists(NOFOLLOW));
    }

    @Test
    public void deleteRecursiveIfExists_linkToDirWillDeleteNoFollow() throws Exception {

        Path dir = dir1().concat("dir").createDirectory();
        Path file = dir.concat("file").createFile();
        Path link = dir1().concat("link").createSymbolicLink(dir);
        assertTrue(dir.exists(NOFOLLOW));
        assertTrue(file.exists(NOFOLLOW));
        assertTrue(link.exists(NOFOLLOW));

        Paths.deleteRecursiveIfExists(link);
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
        Path file = dir1().concat("file").createFile();
        Path link = dir1().concat("link").createSymbolicLink(file);

        Instant fileTime = newInstant();
        Instant linkTime = getModificationTime(link, NOFOLLOW);
        link.setLastModifiedTime(FOLLOW, fileTime);

        assertEquals(fileTime, getModificationTime(file, NOFOLLOW));
        assertEquals(linkTime, getModificationTime(link, NOFOLLOW));
        assertNotEqual(fileTime, linkTime);
    }

    @Test
    public void setModificationTime_linkNoFollow() throws Exception {
        Path file = dir1().concat("file").createFile();
        Path link = dir1().concat("link").createSymbolicLink(file);

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
            Path file,
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
        int expected = stat(dir1().toByteArray()).mode();
        dir1().setPermissions(dir1().stat(NOFOLLOW).permissions());
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
            dir1().setPermissions(Permission.all());
            Paths.removePermissions(dir1(), permissions);

            Set<Permission> actual = dir1().stat(FOLLOW).permissions();
            Set<Permission> expected = new HashSet<>(Permission.all());
            expected.removeAll(permissions);
            assertEquals(expected, actual);
        }
    }

    @Test
    public void removePermissions_changeTargetNotLink() throws Exception {
        Permission perm = OWNER_READ;
        Path link = dir1().concat("link").createSymbolicLink(dir1());
        assertTrue(link.stat(FOLLOW).permissions().contains(perm));
        assertTrue(link.stat(NOFOLLOW).permissions().contains(perm));

        Paths.removePermissions(link, singleton(perm));

        assertFalse(link.stat(FOLLOW).permissions().contains(perm));
        assertTrue(link.stat(NOFOLLOW).permissions().contains(perm));
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
