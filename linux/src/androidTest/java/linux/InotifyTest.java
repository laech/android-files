package linux;

import junit.framework.TestCase;

import java.io.File;
import java.lang.reflect.Field;

import static android.test.MoreAsserts.assertNotEqual;
import static java.io.File.createTempFile;
import static linux.Errno.EBADF;
import static linux.Inotify.IN_ALL_EVENTS;
import static linux.Inotify.inotify_add_watch;
import static linux.Inotify.inotify_init;
import static linux.Inotify.inotify_rm_watch;
import static linux.Unistd.close;

public final class InotifyTest extends TestCase {

    public void test_constants_are_initialized() throws Exception {
        Field[] fields = Inotify.class.getFields();
        assertNotEqual(0, fields.length);
        for (Field field : fields) {
            assertNotEqual(Inotify.placeholder(), field.getInt(null));
        }
    }

    public void test_cannot_use_fd_after_close() throws Exception {

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

    public void test_init_add_watch_then_remove_watch_gives_no_error() throws Exception {
        int fd = inotify_init();
        try {

            int wd = inotify_add_watch(fd, "/".getBytes(), IN_ALL_EVENTS);
            inotify_rm_watch(fd, wd);

        } finally {
            close(fd);
        }
    }

    public void test_inotify_add_watch_throws_NullPointerException_on_null_path_arg() throws Exception {
        try {
            inotify_add_watch(-1, null, 0);
            fail();
        } catch (NullPointerException e) {
            // Pass
        }
    }
}
