package l.files.fs.local;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;

import static android.system.OsConstants.EBADF;

public final class UnistdTest extends FileBaseTest {

    public void testClose() throws Exception {
        try (OutputStream out = dir1().resolve("a").createFile().newOutputStream()) {
            out.write(1); // Check write okay
            Unistd.close(getFd(out));
            try {
                out.write(1); // Error closed
            } catch (IOException e) {
                assertEquals(EBADF, ((android.system.ErrnoException) e.getCause()).errno);
            }
        }
    }

    private int getFd(OutputStream out) throws ReflectiveOperationException, IOException {
        assertTrue(out instanceof FileOutputStream);
        Field field = FileDescriptor.class.getDeclaredField("descriptor");
        field.setAccessible(true);
        return (int) field.get(((FileOutputStream) out).getFD());
    }

}