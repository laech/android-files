package linux;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import linux.Dirent.DIR;

import static junit.framework.Assert.assertTrue;
import static linux.Dirent.DT_DIR;
import static linux.Dirent.DT_REG;
import static linux.Dirent.closedir;
import static linux.Dirent.opendir;
import static linux.Dirent.placeholder;
import static linux.Dirent.readdir;
import static linux.Errno.ENOENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public final class DirentTest {

    private File tempDir;

    @Before
    public void setUp() throws Exception {
        tempDir = createTempDir();
        tempDir.deleteOnExit();
    }

    @After
    public void tearDown() throws Exception {
        if (tempDir != null) {
            assertTrue(tempDir.delete() || !tempDir.exists());
        }
    }

    @Test
    public void constants_are_initialized() throws Exception {
        Field[] fields = Dirent.class.getFields();
        Set<Byte> values = new HashSet<>();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                byte value = field.getByte(null);
                assertNotEquals(placeholder(), value);
                assertTrue(values.add(value));
            }
        }
        assertNotEquals(0, values.size());
    }


    private File createTempDir() throws IOException {
        return TempDir.createTempDir(getClass().getSimpleName());
    }

    @Test
    public void readdir_reads_entries_from_dir() throws Exception {

        File childFile = new File(tempDir, "child");
        assertTrue(childFile.createNewFile());
        try {

            DIR dir = opendir(tempDir.getPath().getBytes("UTF-8"));
            try {

                Dirent self = readdir(dir, new Dirent());
                assertNotEquals(0, self.d_ino);
                assertEquals(DT_DIR, self.d_type);
                assertEquals(1, self.d_name_len);
                assertEquals('.', self.d_name[0]);

                Dirent parent = readdir(dir, new Dirent());
                assertNotEquals(0, parent.d_ino);
                assertEquals(DT_DIR, parent.d_type);
                assertEquals(2, parent.d_name_len);
                assertEquals('.', parent.d_name[0]);
                assertEquals('.', parent.d_name[1]);

                Dirent child = readdir(dir, new Dirent());
                assertNotEquals(0, child.d_ino);
                assertEquals(DT_REG, child.d_type);
                assertEquals(5, child.d_name_len);
                assertEquals('c', child.d_name[0]);
                assertEquals('h', child.d_name[1]);
                assertEquals('i', child.d_name[2]);
                assertEquals('l', child.d_name[3]);
                assertEquals('d', child.d_name[4]);

                assertNull(readdir(dir, new Dirent()));

            } finally {
                closedir(dir);
            }

        } finally {
            assertTrue(childFile.delete());
        }

    }

    @Test
    public void readdir_throws_NullPointerException_on_null_dirent_arg() throws Exception {

        DIR dir = opendir(tempDir.getPath().getBytes("UTF-8"));
        try {
            readdir(dir, null);
            fail();
        } catch (NullPointerException e) {
            // Pass
        } finally {
            closedir(dir);
        }

    }

    @Test
    public void readdir_throws_NullPointerException_on_null_dir_arg() throws Exception {
        try {
            readdir(null, new Dirent());
            fail();
        } catch (NullPointerException e) {
            // Pass
        }
    }

    @Test
    public void closedir_cannot_readdir_afterward_throws_IllegalStateException() throws Exception {

        DIR dir = opendir(tempDir.getPath().getBytes("UTF-8"));
        closedir(dir);

        try {
            readdir(dir, new Dirent());
            fail();
        } catch (IllegalStateException e) {
            // Pass
        }

    }

    @Test
    public void closedir_called_multiple_times_will_throw_IllegalStateException() throws Exception {

        DIR dir = opendir(tempDir.getPath().getBytes("UTF-8"));
        closedir(dir);

        try {
            closedir(dir);
            fail();
        } catch (IllegalStateException e) {
            // Pass
        }

    }

    @Test
    public void closedir_throws_NullPointerException_on_null_dir_arg() throws Exception {
        try {
            closedir(null);
            fail();
        } catch (NullPointerException e) {
            // Pass
        }
    }

    @Test
    public void open_throws_NullPointerException_on_null_path_arg() throws Exception {
        try {
            opendir(null);
            fail();
        } catch (NullPointerException e) {
            // Pass
        }
    }

    @Test
    public void open_throws_ErrnoException_if_path_does_not_exist() throws Exception {
        try {
            opendir("/abcdef".getBytes());
            fail();
        } catch (ErrnoException e) {
            assertEquals(ENOENT, e.errno);
        }
    }
}
