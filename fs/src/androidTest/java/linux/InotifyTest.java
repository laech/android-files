package linux;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import static java.io.File.createTempFile;
import static linux.Errno.EBADF;
import static linux.Inotify.IN_ALL_EVENTS;
import static linux.Inotify.inotify_add_watch;
import static linux.Inotify.inotify_init;
import static linux.Inotify.inotify_rm_watch;
import static linux.Unistd.close;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public final class InotifyTest {

    @Test
    public void constants_are_initialized() throws Exception {
        Field[] fields = Inotify.class.getFields();
        assertNotEquals(0, fields.length);
        for (Field field : fields) {
            assertNotEquals(Inotify.placeholder(), field.getInt(null));
        }
    }

    @Test
    public void cannot_use_fd_after_close() throws Exception {

        File dir = createTempFile(getClass().getSimpleName(), null);
        assertTrue(dir.delete());
        assertTrue(dir.mkdir());
        try {

            int fd = inotify_init();
            inotify_add_watch(fd, dir.getPath().getBytes(), IN_ALL_EVENTS);
            close(fd);

            try {

                inotify_add_watch(fd, dir.getPath().getBytes(), IN_ALL_EVENTS);
                fail();

            } catch (ErrnoException e) {
                assertEquals(EBADF, e.errno);
            }

        } finally {
            assertTrue(dir.delete());
        }
    }

    @Test
    public void init_add_watch_then_remove_watch_gives_no_error() throws Exception {
        int fd = inotify_init();
        try {

            File dir = createTempDir();
            try {

                int wd = inotify_add_watch(fd, dir.getPath().getBytes(), IN_ALL_EVENTS);
                inotify_rm_watch(fd, wd);

            } finally {
                assertTrue(dir.delete() || !dir.exists());
            }

        } finally {
            close(fd);
        }
    }

    @Test
    public void inotify_add_watch_throws_NullPointerException_on_null_path_arg() throws Exception {
        try {
            inotify_add_watch(-1, null, 0);
            fail();
        } catch (NullPointerException e) {
            // Pass
        }
    }

    private File createTempDir() throws IOException {
        return TempDir.createTempDir(getClass().getSimpleName());
    }
}
