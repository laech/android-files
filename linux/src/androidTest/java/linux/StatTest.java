package linux;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import static android.test.MoreAsserts.assertNotEqual;
import static java.io.File.createTempFile;
import static linux.Errno.ENOENT;
import static linux.Stat.S_ISDIR;
import static linux.Stat.S_ISLNK;
import static linux.Stat.S_ISREG;
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

    public void test_stat_throws_NullPointerException_on_null_path_arg() throws Exception {
        try {
            Stat.stat(null, new Stat());
            fail();
        } catch (NullPointerException e) {
            // Pass
        }
    }

    public void test_stat_throws_NullPointerException_on_null_stat_arg() throws Exception {
        try {
            Stat.stat(new byte[]{'/'}, null);
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

    private void assertStat(File file) throws ErrnoException, UnsupportedEncodingException {
        Stat stat = new Stat();
        Stat.stat(file.getPath().getBytes(), stat);
        assertStat(file, stat);
    }

    private void assertLstat(File file) throws ErrnoException, UnsupportedEncodingException {
        Stat stat = new Stat();
        Stat.lstat(file.getPath().getBytes(), stat);
        assertStat(file, stat);
    }

    private void assertStat(File file, Stat stat) {
        assertEquals(file.length(), stat.st_size);
        assertEquals(file.length() / 512, stat.st_blocks);
        assertEquals(file.lastModified() / 1000, stat.st_mtime);
        assertTrue(String.valueOf(stat.st_mtime_nsec), stat.st_mtime_nsec > 0);
        assertEquals(file.isFile(), S_ISREG(stat.st_mode));
        assertEquals(file.isDirectory(), S_ISDIR(stat.st_mode));
    }

    public void test_lstat_throws_NullPointerException_on_null_path_arg() throws Exception {
        try {
            Stat.lstat(null, new Stat());
            fail();
        } catch (NullPointerException e) {
            // Pass
        }
    }

    public void test_lstat_throws_NullPointerException_on_null_stat_arg() throws Exception {
        try {
            Stat.lstat(new byte[]{'/'}, null);
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
                Stat.lstat(link.getPath().getBytes(), stat);
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
            Stat.chmod(null, 0);
            fail();
        } catch (NullPointerException e) {
            // Pass
        }
    }

    public void test_chmod_throws_ErrnoException_if_path_does_not_exist() throws Exception {
        try {
            Stat.chmod("/abc".getBytes(), 0);
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

            Stat.chmod(file.getPath().getBytes(), 0);

            assertFalse(file.canRead());
            assertFalse(file.canWrite());
            assertFalse(file.canExecute());

        } finally {
            assertTrue(file.delete());
        }
    }
}
