package linux;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static android.test.MoreAsserts.assertNotEqual;
import static linux.Errno.ENOENT;
import static linux.Fcntl.O_WRONLY;
import static linux.Fcntl.placeholder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public final class FcntlTest {

    @Rule
    public final TestName testName = new TestName();

    @Test
    public void constants_are_initialized() throws Exception {
        Field[] fields = Fcntl.class.getFields();
        assertNotEqual(0, fields.length);
        for (Field field : fields) {
            assertNotEqual(placeholder(), field.getInt(null));
        }
    }

    @Test
    public void open_throws_ErrnoException_if_path_does_not_exist() throws Exception {
        try {
            Fcntl.open("/abcdef".getBytes(), 0, 0);
            fail();
        } catch (ErrnoException e) {
            assertEquals(ENOENT, e.errno);
        }
    }

    @Test
    public void open_throws_NullPointerException_on_null_path_arg() throws Exception {
        try {
            Fcntl.open(null, 0, 0);
            fail();
        } catch (NullPointerException e) {
            // Pass
        }
    }

    @Test
    public void open_returns_fd() throws Exception {

        File file = File.createTempFile(getClass().getSimpleName()
                + "." + testName.getMethodName(), null);
        try {

            int fd = Fcntl.open(file.getPath().getBytes("UTF-8"), O_WRONLY, 0);
            OutputStream out = new FileOutputStream(toFileDescriptor(fd));
            try {
                out.write("hello".getBytes("UTF-8"));
            } finally {
                // FileOutputStream doesn't close fd because it doesn't own it
                Unistd.close(fd);
            }

            assertEquals("hello", readAllUtf8(file));


        } finally {
            assertTrue(file.delete());
        }
    }

    private static String readAllUtf8(File file) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FileInputStream in = new FileInputStream(file);
        try {
            byte[] buffer = new byte[1024];
            int count;
            while ((count = in.read(buffer)) != -1) {
                out.write(buffer, 0, count);
            }
            return out.toString("UTF-8");
        } finally {
            in.close();
        }
    }

    private static FileDescriptor toFileDescriptor(int fd) {

        FileDescriptor descriptor = new FileDescriptor();

        try {
            Method setter = FileDescriptor.class.getMethod("setInt$", int.class);
            setter.setAccessible(true);
            setter.invoke(descriptor, fd);
            return descriptor;
        } catch (NoSuchMethodException ignored) {
        } catch (IllegalAccessException ignored) {
        } catch (InvocationTargetException ignored) {
        }

        try {
            Field field = FileDescriptor.class.getField("descriptor");
            field.setAccessible(true);
            field.set(descriptor, fd);
            return descriptor;
        } catch (NoSuchFieldException ignored) {
        } catch (IllegalAccessException ignored) {
        }

        return descriptor;
    }

}