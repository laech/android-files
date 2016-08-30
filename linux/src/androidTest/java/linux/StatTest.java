package linux;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import static android.test.MoreAsserts.assertNotEqual;
import static java.io.File.createTempFile;
import static linux.Errno.ENOENT;
import static linux.Fcntl.open;
import static linux.Stat.S_IRGRP;
import static linux.Stat.S_IROTH;
import static linux.Stat.S_IRUSR;
import static linux.Stat.S_ISDIR;
import static linux.Stat.S_ISLNK;
import static linux.Stat.S_ISREG;
import static linux.Stat.S_IWGRP;
import static linux.Stat.S_IWOTH;
import static linux.Stat.S_IWUSR;
import static linux.Stat.S_IXGRP;
import static linux.Stat.S_IXOTH;
import static linux.Stat.S_IXUSR;
import static linux.Stat.chmod;
import static linux.Stat.fstat;
import static linux.Stat.lstat;
import static linux.Stat.mkdir;
import static linux.Stat.stat;
import static linux.TempDir.createTempDir;
import static linux.Unistd.close;
import static linux.Unistd.symlink;

public final class StatTest extends TestCase {

    public void test_constants_are_initialized() throws Exception {
        Field[] fields = Stat.class.getFields();
        Set<Integer> values = new HashSet<>();
        assertNotEqual(0, fields.length);
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                int value = field.getInt(null);
                assertNotEqual(field.getName(), Stat.placeholder(), value);
                assertTrue(values.add(value));
            }
        }
        assertNotEqual(0, values.size());
    }

    public void test_stat_fields_are_initialized() throws Exception {

        Stat stat = new Stat();
        Field[] fields = Stat.class.getFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (field.getType() == int.class) {
                assertEquals(Stat.placeholder(), field.getInt(stat));
            } else {
                assertEquals(Stat.placeholder(), field.getLong(stat));
            }
        }

        stat("/".getBytes(), stat);
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (field.getType() == int.class) {
                assertNotEqual(Stat.placeholder(), field.getInt(stat));
            } else {
                assertNotEqual(Stat.placeholder(), field.getLong(stat));
            }
        }
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

    public void test_fstat_throws_NullPointerException_on_null_stat_arg() throws Exception {
        try {
            fstat(-1, null);
            fail();
        } catch (NullPointerException e) {
            // Pass
        }
    }

    public void test_fstat_returns_correct_information_for_file() throws Exception {

        File file = createNonEmptyFile();
        try {

            FileInputStream in = new FileInputStream(file);
            try {

                Field field = FileDescriptor.class.getDeclaredField("descriptor");
                field.setAccessible(true);
                int fd = field.getInt(in.getFD());

                Stat stat = new Stat();
                fstat(fd, stat);
                assertStat(file, stat);

            } finally {
                in.close();
            }

        } finally {
            assertTrue(file.delete());
        }
    }

    public void test_fstat_returns_correct_information_for_directory() throws Exception {

        File dir = createTempDir("StatTest");
        try {
            int fd = open(dir.getPath().getBytes(), 0, 0);
            try {

                Stat stat = new Stat();
                fstat(fd, stat);
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
            stat(null, new Stat());
            fail();
        } catch (NullPointerException e) {
            // Pass
        }
    }

    public void test_stat_throws_NullPointerException_on_null_stat_arg() throws Exception {
        try {
            stat(new byte[]{'/'}, null);
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
        Stat stat = new Stat();
        stat(file.getPath().getBytes(), stat);
        assertStat(file, stat);
    }

    private static void assertLstat(File file) throws ErrnoException {
        Stat stat = new Stat();
        lstat(file.getPath().getBytes(), stat);
        assertStat(file, stat);
    }

    private static void assertStat(File file, Stat stat) {
        assertEquals(file.length(), stat.st_size);
        assertEquals(file.length() / 512, stat.st_blocks);
        assertEquals(file.lastModified() / 1000, stat.st_mtime);
        assertTrue(String.valueOf(stat.st_mtime_nsec), stat.st_mtime_nsec > 0);
        assertEquals(file.isFile(), S_ISREG(stat.st_mode));
        assertEquals(file.isDirectory(), S_ISDIR(stat.st_mode));
    }

    public void test_lstat_throws_NullPointerException_on_null_path_arg() throws Exception {
        try {
            lstat(null, new Stat());
            fail();
        } catch (NullPointerException e) {
            // Pass
        }
    }

    public void test_lstat_throws_NullPointerException_on_null_stat_arg() throws Exception {
        try {
            lstat(new byte[]{'/'}, null);
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

                Stat stat = new Stat();
                lstat(link.getPath().getBytes(), stat);
                assertEquals(true, S_ISLNK(stat.st_mode));
                assertNotEqual(file.length(), stat.st_size);

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

    public void test_mkdir_creates_new_dir() throws Exception {
        File dir = createTempFile(getClass().getSimpleName(), null);
        try {

            assertTrue(dir.delete());
            assertFalse(dir.exists());

            mkdir(dir.getPath().getBytes(), 0700);
            assertTrue(dir.exists());
            assertTrue(dir.isDirectory());

            Stat stat = new Stat();
            stat(dir.getPath().getBytes(), stat);
            assertTrue((S_IRUSR & stat.st_mode) != 0);
            assertTrue((S_IWUSR & stat.st_mode) != 0);
            assertTrue((S_IXUSR & stat.st_mode) != 0);
            assertTrue((S_IRGRP & stat.st_mode) == 0);
            assertTrue((S_IWGRP & stat.st_mode) == 0);
            assertTrue((S_IXGRP & stat.st_mode) == 0);
            assertTrue((S_IROTH & stat.st_mode) == 0);
            assertTrue((S_IWOTH & stat.st_mode) == 0);
            assertTrue((S_IXOTH & stat.st_mode) == 0);

        } finally {
            assertTrue(!dir.exists() || dir.delete());
        }
    }
}
