package l.files.fs.local;

import android.system.OsConstants;

import java.io.File;
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

    public void testSymlink() throws Exception {
        File a = tmp().createFile("a");
        File b = tmp().get("b");
        assertFalse(b.exists());

        Unistd.symlink(a.getPath(), b.getPath());

        assertTrue(b.exists());
        assertEquals(a.getCanonicalPath(), b.getCanonicalPath());
    }

    public void testReadlink() throws Exception {
        File a = tmp().createFile("a");
        File b = tmp().get("b");
        Unistd.symlink(a.getPath(), b.getPath());
        assertEquals(a.getPath(), Unistd.readlink(b.getPath()));
    }

    public void testAccess_R_OK_true() throws Exception {
        testAccessTrue(Unistd.R_OK, new Consumer<File>() {
            @Override
            public void apply(File input) {
                assertTrue(input.setReadable(true));
            }
        });
    }

    public void testAccess_R_OK_false() throws Exception {
        testAccessFalse(Unistd.R_OK, new Consumer<File>() {
            @Override
            public void apply(File input) {
                assertTrue(input.setReadable(false));
            }
        });
    }

    public void testAccess_W_OK_true() throws Exception {
        testAccessTrue(Unistd.W_OK, new Consumer<File>() {
            @Override
            public void apply(File input) {
                assertTrue(input.setWritable(true));
            }
        });
    }

    public void testAccess_W_OK_false() throws Exception {
        testAccessFalse(Unistd.W_OK, new Consumer<File>() {
            @Override
            public void apply(File input) {
                assertTrue(input.setWritable(false));
            }
        });
    }

    public void testAccess_X_OK_true() throws Exception {
        testAccessTrue(Unistd.X_OK, new Consumer<File>() {
            @Override
            public void apply(File input) {
                assertTrue(input.setExecutable(true));
            }
        });
    }

    public void testAccess_X_OK_false() throws Exception {
        testAccessFalse(Unistd.X_OK, new Consumer<File>() {
            @Override
            public void apply(File input) {
                assertTrue(input.setExecutable(false));
            }
        });
    }

    public void testAccess_F_OK_true() throws Exception {
        testAccessTrue(Unistd.F_OK, new Consumer<File>() {
            @Override
            public void apply(File input) {
            }
        });
    }

    public void testAccess_F_OK_false() throws Exception {
        testAccessFalse(Unistd.F_OK, new Consumer<File>() {
            @Override
            public void apply(File input) {
                assertTrue(input.delete());
            }
        });
    }

    public void testAccessTrue(int mode, Consumer<File> fn) throws Exception {
        File file = tmp().createFile("a");
        fn.apply(file);
        Unistd.access(file.getPath(), mode);
    }

    public void testAccessFalse(int mode, Consumer<File> fn) throws Exception {
        File file = tmp().createFile("a");
        fn.apply(file);
        try {
            Unistd.access(file.getPath(), mode);
            fail();
        } catch (ErrnoException e) {
            // Pass
        }
    }

    private static interface Consumer<T> {
        void apply(T input);
    }
}
