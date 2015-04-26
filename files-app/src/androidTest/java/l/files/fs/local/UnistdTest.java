package l.files.fs.local;

import android.system.OsConstants;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import l.files.common.testing.FileBaseTest;

public final class UnistdTest extends FileBaseTest {

    public void testClose() throws Exception {
        try (FileOutputStream out = new FileOutputStream(tmp().createFile("a"))) {
            out.write(1); // Check write okay
            Unistd.close(getFd(out));
            try {
                out.write(1); // Error closed
            } catch (IOException e) {
                assertEquals(OsConstants.EBADF, ((android.system.ErrnoException) e.getCause()).errno);
            }
        }
    }

    private int getFd(FileOutputStream out) throws ReflectiveOperationException, IOException {
        Field field = FileDescriptor.class.getDeclaredField("descriptor");
        field.setAccessible(true);
        return (int) field.get(out.getFD());
    }

}
