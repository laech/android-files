package linux;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

import static android.test.MoreAsserts.assertNotEqual;
import static java.io.File.createTempFile;
import static linux.Errno.EACCES;
import static linux.Errno.ENOENT;
import static linux.Unistd.F_OK;
import static linux.Unistd.R_OK;
import static linux.Unistd.W_OK;
import static linux.Unistd.X_OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public final class UnistdTest {

    @Test
    public void constants_are_initialized() throws Exception {
        Field[] fields = Unistd.class.getFields();
        assertNotEqual(0, fields.length);
        for (Field field : fields) {
            assertNotEqual(field.getName(), Unistd.placeholder(), field.getByte(null));
        }
    }

    @Test
    public void close_cannot_use_fd_afterward() throws Exception {

        File file = createTempFile(getClass().getSimpleName(), null);
        try {

            FileOutputStream out = new FileOutputStream(file);
            out.write(1);
            Unistd.close(getFd(out));
            try {
                out.write(1); // Error closed
                fail();
            } catch (IOException e) {
                // Pass
            }

        } finally {
            assertTrue(file.delete());
        }
    }

    private int getFd(FileOutputStream out) throws Exception {
        Field field = FileDescriptor.class.getDeclaredField("descriptor");
        field.setAccessible(true);
        return (int) field.get(out.getFD());
    }

    @Test
    public void access_throws_NullPointerException_on_null_path() throws Exception {
        try {
            Unistd.access(null, F_OK);
            fail();
        } catch (NullPointerException e) {
            // Pass
        }
    }

    @Test
    public void access_read() throws Exception {
        File file = createTempFile(getClass().getSimpleName(), null);
        try {

            assertTrue(file.setReadable(true));
            Unistd.access(file.getPath().getBytes(), R_OK);

            assertTrue(file.setReadable(false));
            try {
                Unistd.access(file.getPath().getBytes(), R_OK);
                fail();
            } catch (ErrnoException e) {
                assertEquals(EACCES, e.errno);
            }

        } finally {
            assertTrue(file.delete());
        }
    }

    @Test
    public void access_write() throws Exception {
        File file = createTempFile(getClass().getSimpleName(), null);
        try {

            assertTrue(file.setWritable(true));
            Unistd.access(file.getPath().getBytes(), W_OK);

            assertTrue(file.setWritable(false));
            try {
                Unistd.access(file.getPath().getBytes(), W_OK);
                fail();
            } catch (ErrnoException e) {
                assertEquals(EACCES, e.errno);
            }

        } finally {
            assertTrue(file.delete());
        }
    }

    @Test
    public void access_execute() throws Exception {
        File file = createTempFile(getClass().getSimpleName(), null);
        try {

            assertTrue(file.setExecutable(true));
            Unistd.access(file.getPath().getBytes(), X_OK);

            assertTrue(file.setExecutable(false));
            try {
                Unistd.access(file.getPath().getBytes(), X_OK);
                fail();
            } catch (ErrnoException e) {
                assertEquals(EACCES, e.errno);
            }

        } finally {
            assertTrue(file.delete());
        }
    }

    @Test
    public void access_exist() throws Exception {
        File file = createTempFile(getClass().getSimpleName(), null);
        try {

            Unistd.access(file.getPath().getBytes(), F_OK);

        } finally {
            assertTrue(file.delete());
        }

        try {
            Unistd.access(file.getPath().getBytes(), F_OK);
            fail();
        } catch (ErrnoException e) {
            assertEquals(ENOENT, e.errno);
        }
    }

    @Test
    public void symlink_throws_NullPointerException_on_null_target_arg() throws Exception {
        try {
            Unistd.symlink(null, new byte[]{'/', 'a', 'b', 'c'});
            fail();
        } catch (NullPointerException e) {
            // Pass
        }
    }

    @Test
    public void symlink_throws_NullPointerException_on_null_link_arg() throws Exception {
        try {
            Unistd.symlink(new byte[]{'/'}, null);
            fail();
        } catch (NullPointerException e) {
            // Pass
        }
    }

    @Test
    public void symlink_to_target() throws Exception {

        File file = createTempFile(getClass().getSimpleName(), null);
        File link = new File(file.getParent(), file.getName() + "-link");
        Unistd.symlink(file.getPath().getBytes(), link.getPath().getBytes());

        OutputStream out = new FileOutputStream(file);
        try {
            out.write("hello".getBytes());
        } finally {
            out.close();
        }

        InputStream in = new FileInputStream(link);
        try {
            assertEquals("hello", readAllUtf8(in));
        } finally {
            in.close();
        }
    }

    private String readAllUtf8(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int count;
        while ((count = in.read(buffer)) != -1) {
            out.write(buffer, 0, count);
        }
        return out.toString();
    }

    @Test
    public void readlink_throws_NullPointerException_on_null_path_arg() throws Exception {
        try {
            Unistd.readlink(null);
            fail();
        } catch (NullPointerException e) {
            // Pass
        }
    }

    @Test
    public void readlink_returns_target_path() throws Exception {
        File file = createTempFile(getClass().getSimpleName(), null);
        File link = new File(file.getParent(), file.getName() + "-link");
        Unistd.symlink(file.getPath().getBytes(), link.getPath().getBytes());
        assertEquals(file.getPath(), new String(Unistd.readlink(link.getPath().getBytes())));
    }

}
