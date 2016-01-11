package l.files.fs.local;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;

import l.files.fs.Files;

public final class UnistdTest extends PathBaseTest {

    public void test_close() throws Exception {
        OutputStream out = Files.newOutputStream(Files.createFile(dir1().resolve("a")));
        try {

            out.write(1); // Check write okay
            Unistd.close(getFd(out));
            try {
                out.write(1); // Error closed
                fail();
            } catch (IOException ignored) {
            }

        } finally {
            try {
                out.close();
            } catch (IOException ignored) {
            }
        }
    }

    private int getFd(OutputStream out) throws Exception {
        assertTrue(out instanceof FileOutputStream);
        Field field = FileDescriptor.class.getDeclaredField("descriptor");
        field.setAccessible(true);
        return (int) field.get(((FileOutputStream) out).getFD());
    }

}
