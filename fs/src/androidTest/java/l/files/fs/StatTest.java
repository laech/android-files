package l.files.fs;

import android.os.Parcel;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public final class StatTest extends PathBaseTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void can_create_from_parcel() throws Exception {
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

    @Test
    public void constants_are_initialized() throws Exception {
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

    private File createNonEmptyFile() throws IOException {
        File file = temporaryFolder.newFile();
        try (OutputStream out = new FileOutputStream(file)) {
            out.write(new byte[1 << 16]);
        }
        return file;
    }

    @Test
    public void fstat_returns_correct_information_for_file() throws Exception {

        File file = createNonEmptyFile();

        try (FileInputStream in = new FileInputStream(file)) {

            Field field = FileDescriptor.class.getDeclaredField("descriptor");
            field.setAccessible(true);
            int fd = field.getInt(in.getFD());

            Stat stat = fstat(fd);
            assertStat(file, stat);
        }
    }

    @Test
    public void fstat_returns_correct_information_for_directory() throws Exception {

        File dir = temporaryFolder.newFolder();
        int fd = open(dir.getPath().getBytes(), 0, 0);
        try {

            Stat stat = fstat(fd);
            assertStat(dir, stat);

        } finally {
            close(fd);
        }
    }

    @Test
    public void stat_throws_NullPointerException_on_null_path_arg() throws Exception {
        try {
            stat(null);
            fail();
        } catch (NullPointerException e) {
            // Pass
        }
    }

    @Test
    public void stat_returns_correct_information_for_file() throws Exception {
        assertStat(createNonEmptyFile());
    }

    @Test
    public void stat_returns_correct_information_for_directory() throws Exception {
        assertStat(new File("/"));
    }

    @Test
    public void stat_returns_correct_information_for_symlink() throws Exception {
        File file = createNonEmptyFile();
        File link = new File(file.getParent(), file.getName() + "-link");
        symlink(file.getPath().getBytes(), link.getPath().getBytes());
        assertStat(link);
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

    @Test
    public void lstat_throws_NullPointerException_on_null_path_arg() throws Exception {
        try {
            lstat(null);
            fail();
        } catch (NullPointerException e) {
            // Pass
        }
    }

    @Test
    public void lstat_returns_correct_information_for_file() throws Exception {
        assertLstat(createNonEmptyFile());
    }

    @Test
    public void lstat_returns_correct_information_for_directory() throws Exception {
        assertLstat(new File("/"));
    }

    @Test
    public void lstat_returns_correct_information_for_symlink() throws Exception {
        File file = createNonEmptyFile();
        File link = new File(file.getParent(), file.getName() + "-link");
        symlink(file.getPath().getBytes(), link.getPath().getBytes());
        Stat stat = lstat(link.getPath().getBytes());
        assertEquals(true, stat.isSymbolicLink());
        assertNotEqual(file.length(), stat.size());
    }

    @Test
    public void chmod_throws_NullPointerException_on_null_path_arg() throws Exception {
        try {
            chmod(null, 0);
            fail();
        } catch (NullPointerException e) {
            // Pass
        }
    }

    @Test
    public void chmod_throws_ErrnoException_if_path_does_not_exist() throws Exception {
        try {
            chmod("/abc".getBytes(), 0);
            fail();
        } catch (ErrnoException e) {
            assertEquals(ENOENT, e.errno);
        }
    }

    @Test
    public void chmod_updates_permission() throws Exception {

        File file = temporaryFolder.newFile();

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

    }

    @Test
    public void mkdir_throws_NullPointerException_on_null_path_arg() throws Exception {
        try {
            mkdir(null, 0);
            fail();
        } catch (NullPointerException e) {
            // Pass
        }
    }

    @Test
    public void mkdir_throws_ErrnoException_if_parent_does_not_exist() throws Exception {
        File dir = new File("/abc/def");
        try {
            mkdir(dir.getPath().getBytes(), 0);
            fail();
        } catch (ErrnoException e) {
            assertEquals(ENOENT, e.errno);
        }
    }


    @Test
    @SuppressWarnings("OctalInteger")
    public void mkdir_creates_new_dir() throws Exception {
        File dir = new File(temporaryFolder.getRoot(), "dir");

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

    }
}
