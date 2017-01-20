package l.files.fs;

import android.os.Parcel;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import l.files.testing.fs.PathBaseTest;
import linux.ErrnoException;

import static android.test.MoreAsserts.assertNotEqual;
import static com.google.common.io.Files.createTempDir;
import static java.io.File.createTempFile;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Stat.chmod;
import static l.files.fs.Stat.fstat;
import static l.files.fs.Stat.lstat;
import static l.files.fs.Stat.mkdir;
import static l.files.fs.Stat.stat;
import static linux.Errno.ENOENT;
import static linux.Fcntl.open;
import static linux.Unistd.close;
import static linux.Unistd.symlink;

public final class StatTest extends PathBaseTest {

    public void test_can_create_from_parcel() throws Exception {
        Parcel parcel = Parcel.obtain();
        try {
            Stat expected = dir1().stat(NOFOLLOW);
            expected.writeToParcel(parcel, 0);
            parcel.setDataPosition(0);
            Stat actual = Stat.CREATOR.createFromParcel(parcel);
            assertEquals(expected, actual);
        } finally {
            parcel.recycle();
        }
    }

    public void test_constants_are_initialized() throws Exception {
        Field[] fields = Stat.class.getDeclaredFields();
        Set<Integer> values = new HashSet<>();
        assertNotEqual(0, fields.length);
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()) &&
                    field.getType() == int.class) {
                field.setAccessible(true);
                int value = field.getInt(null);
                assertNotEqual(field.getName(), Stat.placeholder(), value);
                if (!values.add(value)) {
                    fail("Failed to add " + field.getName() + "=" + value);
                }
            }
        }
        assertNotEqual(0, values.size());
    }

    private static File createNonEmptyFile() throws IOException {

        File file = createTempFile(StatTest.class.getSimpleName(), null);
        file.deleteOnExit();

        OutputStream out = new FileOutputStream(file);
        try {
            out.write(new byte[1 << 16]);
        } finally {
            out.close();
        }

        return file;
    }

    public void test_fstat_returns_correct_information_for_file() throws Exception {

        File file = createNonEmptyFile();
        try {

            FileInputStream in = new FileInputStream(file);
            try {

                Field field = FileDescriptor.class.getDeclaredField("descriptor");
                field.setAccessible(true);
                int fd = field.getInt(in.getFD());

                Stat stat = fstat(fd);
                assertStat(file, stat);

            } finally {
                in.close();
            }

        } finally {
            assertTrue(file.delete());
        }
    }

    public void test_fstat_returns_correct_information_for_directory() throws Exception {

        File dir = createTempDir();
        try {
            int fd = open(dir.getPath().getBytes(), 0, 0);
            try {

                Stat stat = fstat(fd);
                assertStat(dir, stat);

            } finally {
                close(fd);
            }
        } finally {
            assertTrue(dir.delete() || !dir.exists());
        }
    }

    public void test_stat_throws_NullPointerException_on_null_path_arg() throws Exception {
        try {
            stat(null);
            fail();
        } catch (NullPointerException e) {
            // Pass
        }
    }

    public void test_stat_returns_correct_information_for_file() throws Exception {
        File file = createNonEmptyFile();
        try {
            assertStat(file);
        } finally {
            assertTrue(file.delete());
        }
    }

    public void test_stat_returns_correct_information_for_directory() throws Exception {
        assertStat(new File("/"));
    }

    public void test_stat_returns_correct_information_for_symlink() throws Exception {

        File file = createNonEmptyFile();
        try {

            File link = new File(file.getParent(), file.getName() + "-link");
            symlink(file.getPath().getBytes(), link.getPath().getBytes());
            try {
                assertStat(link);
            } finally {
                assertTrue(link.delete());
            }

        } finally {
            assertTrue(file.delete());
        }
    }

    private static void assertStat(File file) throws ErrnoException {
        Stat stat = stat(file.getPath().getBytes());
        assertStat(file, stat);
    }

    private static void assertLstat(File file) throws ErrnoException {
        Stat stat = lstat(file.getPath().getBytes());
        assertStat(file, stat);
    }

    private static void assertStat(File file, Stat stat) {
        assertEquals(file.length(), stat.size());
        assertEquals(file.length() / 512 * 512, stat.sizeOnDisk());
        assertEquals(file.lastModified() / 1000, stat.lastModifiedEpochSecond());
        assertEquals(file.isFile(), stat.isRegularFile());
        assertEquals(file.isDirectory(), stat.isDirectory());
        assertTrue(
                String.valueOf(stat.lastModifiedNanoOfSecond()),
                stat.lastModifiedNanoOfSecond() > 0);
    }

    public void test_lstat_throws_NullPointerException_on_null_path_arg() throws Exception {
        try {
            lstat(null);
            fail();
        } catch (NullPointerException e) {
            // Pass
        }
    }

    public void test_lstat_returns_correct_information_for_file() throws Exception {
        File file = createNonEmptyFile();
        try {
            assertLstat(file);
        } finally {
            assertTrue(file.delete());
        }
    }

    public void test_lstat_returns_correct_information_for_directory() throws Exception {
        assertLstat(new File("/"));
    }

    public void test_lstat_returns_correct_information_for_symlink() throws Exception {

        File file = createNonEmptyFile();
        try {

            File link = new File(file.getParent(), file.getName() + "-link");
            symlink(file.getPath().getBytes(), link.getPath().getBytes());
            try {

                Stat stat = lstat(link.getPath().getBytes());
                assertEquals(true, stat.isSymbolicLink());
                assertNotEqual(file.length(), stat.size());

            } finally {
                assertTrue(link.delete());
            }

        } finally {
            assertTrue(file.delete());
        }
    }

    public void test_chmod_throws_NullPointerException_on_null_path_arg() throws Exception {
        try {
            chmod(null, 0);
            fail();
        } catch (NullPointerException e) {
            // Pass
        }
    }

    public void test_chmod_throws_ErrnoException_if_path_does_not_exist() throws Exception {
        try {
            chmod("/abc".getBytes(), 0);
            fail();
        } catch (ErrnoException e) {
            assertEquals(ENOENT, e.errno);
        }
    }

    public void test_chmod_updates_permission() throws Exception {

        File file = createTempFile(getClass().getSimpleName(), null);
        try {

            assertTrue(file.setReadable(true));
            assertTrue(file.setWritable(true));
            assertTrue(file.setExecutable(true));

            assertTrue(file.canRead());
            assertTrue(file.canWrite());
            assertTrue(file.canExecute());

            chmod(file.getPath().getBytes(), 0);

            assertFalse(file.canRead());
            assertFalse(file.canWrite());
            assertFalse(file.canExecute());

        } finally {
            assertTrue(file.delete());
        }
    }

    public void test_mkdir_throws_NullPointerException_on_null_path_arg() throws Exception {
        try {
            mkdir(null, 0);
            fail();
        } catch (NullPointerException e) {
            // Pass
        }
    }

    public void test_mkdir_throws_ErrnoException_if_parent_does_not_exist() throws Exception {
        File dir = new File("/abc/def");
        try {
            mkdir(dir.getPath().getBytes(), 0);
            fail();
        } catch (ErrnoException e) {
            assertEquals(ENOENT, e.errno);
        }
    }

    @SuppressWarnings("OctalInteger")
    public void test_mkdir_creates_new_dir() throws Exception {
        File dir = createTempFile(getClass().getSimpleName(), null);
        try {

            assertTrue(dir.delete());
            assertFalse(dir.exists());

            mkdir(dir.getPath().getBytes(), 0700);
            assertTrue(dir.exists());
            assertTrue(dir.isDirectory());

            Stat stat = stat(dir.getPath().getBytes());
            assertEquals(
                    EnumSet.of(
                            Permission.OWNER_READ,
                            Permission.OWNER_WRITE,
                            Permission.OWNER_EXECUTE),
                    stat.permissions());

        } finally {
            assertTrue(!dir.exists() || dir.delete());
        }
    }
}
