package linux;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

import linux.Dirent.DIR;

import static android.test.MoreAsserts.assertNotEqual;
import static java.io.File.createTempFile;
import static linux.Dirent.DT_DIR;
import static linux.Dirent.DT_REG;
import static linux.Dirent.closedir;
import static linux.Dirent.opendir;
import static linux.Dirent.readdir;

public final class DirentTest extends TestCase {

    private File tempDir;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tempDir = createTempDir();
        tempDir.deleteOnExit();
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            super.tearDown();
        } finally {
            if (tempDir != null) {
                assertTrue(tempDir.delete() || !tempDir.exists());
            }
        }
    }

    private File createTempDir() throws IOException {
        File dir = createTempFile(getClass().getSimpleName(), null);
        assertTrue(dir.delete());
        assertTrue(dir.mkdir());
        return dir;
    }

    public void test_fdopendir_throws_ErrnoException_on_invalid_fd() throws Exception {
        try {
            Dirent.fdopendir(-1);
            fail();
        } catch (ErrnoException e) {
            // Pass
        }
    }

    public void test_readdir_reads_entries_from_dir() throws Exception {

        File childFile = new File(tempDir, "child");
        assertTrue(childFile.createNewFile());
        try {

            DIR dir = opendir(tempDir.getPath().getBytes("UTF-8"));
            try {

                Dirent self = readdir(dir, new Dirent());
                assertNotEqual(0, self.d_ino);
                assertEquals(DT_DIR, self.d_type);
                assertEquals(1, self.d_name_len);
                assertEquals('.', self.d_name[0]);

                Dirent parent = readdir(dir, new Dirent());
                assertNotEqual(0, parent.d_ino);
                assertEquals(DT_DIR, parent.d_type);
                assertEquals(2, parent.d_name_len);
                assertEquals('.', parent.d_name[0]);
                assertEquals('.', parent.d_name[1]);

                Dirent child = readdir(dir, new Dirent());
                assertNotEqual(0, child.d_ino);
                assertEquals(DT_REG, child.d_type);
                assertEquals(5, child.d_name_len);
                assertEquals('c', child.d_name[0]);
                assertEquals('h', child.d_name[1]);
                assertEquals('i', child.d_name[2]);
                assertEquals('l', child.d_name[3]);
                assertEquals('d', child.d_name[4]);

                assertEquals(null, readdir(dir, new Dirent()));

            } finally {
                closedir(dir);
            }

        } finally {
            assertTrue(childFile.delete());
        }

    }

    public void test_readdir_throws_NullPointerException_on_null_dirent_arg() throws Exception {

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

    public void test_readdir_throws_NullPointerException_on_null_dir_arg() throws Exception {
        try {
            readdir(null, new Dirent());
            fail();
        } catch (NullPointerException e) {
            // Pass
        }
    }

    public void test_closedir_cannot_readdir_afterward_throws_IllegalStateException() throws Exception {

        DIR dir = opendir(tempDir.getPath().getBytes("UTF-8"));
        closedir(dir);

        try {
            readdir(dir, new Dirent());
            fail();
        } catch (IllegalStateException e) {
            // Pass
        }

    }

    public void test_closedir_called_multiple_times_will_throw_IllegalStateException() throws Exception {

        DIR dir = opendir(tempDir.getPath().getBytes("UTF-8"));
        closedir(dir);

        try {
            closedir(dir);
            fail();
        } catch (IllegalStateException e) {
            // Pass
        }

    }

    public void test_closedir_throws_NullPointerException_on_null_dir_arg() throws Exception {
        try {
            closedir(null);
            fail();
        } catch (NullPointerException e) {
            // Pass
        }
    }

    public void test_open_throws_NullPointerException_on_null_path_arg() throws Exception {
        try {
            opendir(null);
            fail();
        } catch (NullPointerException e) {
            // Pass
        }
    }

}