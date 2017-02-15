package linux;

import org.junit.Test;

import java.io.File;

import static java.io.File.createTempFile;
import static linux.Errno.EACCES;
import static linux.Errno.ENOENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public final class StdioTest {

    @Test
    public void remove_throws_NullPointerException_on_null_path_arg() throws Exception {
        try {
            Stdio.remove(null);
            fail();
        } catch (NullPointerException e) {
            // Pass
        }
    }

    @Test
    public void remove_throws_ErrnoException_if_path_does_not_exists() throws Exception {
        File file = createTempFile(getClass().getSimpleName(), null);
        assertTrue(file.delete());
        try {
            Stdio.remove(file.getPath().getBytes());
            fail();
        } catch (ErrnoException e) {
            assertEquals(ENOENT, e.errno);
        }
    }

    @Test
    public void remove_throws_ErrnoException_if_no_permission_to_delete() throws Exception {
        File dir = createTempFile(getClass().getSimpleName(), "dir");
        assertTrue(dir.delete());
        assertTrue(dir.mkdir());

        File file = createTempFile(getClass().getSimpleName(), "file", dir);
        try {

            assertTrue(dir.setWritable(false));
            Stdio.remove(file.getPath().getBytes());
            fail();

        } catch (ErrnoException e) {
            assertEquals(EACCES, e.errno);

        } finally {
            assertTrue(dir.setWritable(true));
            assertTrue(file.delete() || !file.exists());
            assertTrue(dir.delete());
        }
    }

    @Test
    public void remove_file() throws Exception {
        File file = createTempFile(getClass().getSimpleName(), null);
        assertEquals(true, file.exists());
        try {

            Stdio.remove(file.getPath().getBytes());
            assertEquals(false, file.exists());

        } finally {
            assertTrue(!file.exists() || file.delete());
        }
    }

    @Test
    public void rename_throws_NullPointerException_on_null_old_path_arg() throws Exception {
        try {
            Stdio.rename(null, "/abc".getBytes());
            fail();
        } catch (NullPointerException e) {
            // Pass
        }
    }

    @Test
    public void rename_throws_NullPointerException_on_null_new_path_arg() throws Exception {
        try {
            Stdio.rename("/abc".getBytes(), null);
            fail();
        } catch (NullPointerException e) {
            // Pass
        }
    }

    @Test
    public void rename_moves_file_to_new_location() throws Exception {
        File oldFile = createTempFile(getClass().getSimpleName(), null);
        File newFile = new File(oldFile.getParent(), oldFile.getName() + "-new");
        try {

            assertTrue(oldFile.exists());
            assertFalse(newFile.exists());
            Stdio.rename(oldFile.getPath().getBytes(), newFile.getPath().getBytes());
            assertFalse(oldFile.exists());
            assertTrue(newFile.exists());

        } finally {
            assertTrue(!oldFile.exists() || oldFile.delete());
            assertTrue(!newFile.exists() || newFile.delete());
        }

    }

    @Test
    public void rename_throws_ErrnoException_source_does_not_exits() throws Exception {
        File oldFile = createTempFile(getClass().getSimpleName(), null);
        File newFile = new File(oldFile.getParent(), oldFile.getName() + "-new");
        try {

            assertTrue(oldFile.delete());
            assertFalse(oldFile.exists());
            assertFalse(newFile.exists());
            Stdio.rename(oldFile.getPath().getBytes(), newFile.getPath().getBytes());

        } catch (ErrnoException e) {
            assertEquals(ENOENT, e.errno);
        }

    }
}