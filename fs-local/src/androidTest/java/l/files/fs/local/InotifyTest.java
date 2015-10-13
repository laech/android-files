package l.files.fs.local;

import android.system.ErrnoException;

import static android.system.OsConstants.EINVAL;
import static l.files.fs.local.Inotify.IN_ALL_EVENTS;
import static l.files.fs.local.Inotify.addWatch;
import static l.files.fs.local.Inotify.init1;
import static l.files.fs.local.Unistd.close;

public final class InotifyTest extends FileBaseTest {

    public void test_cannot_use_fd_after_close() throws Exception {
        int fd = init1(0);
        addWatch(fd, dir1().path(), IN_ALL_EVENTS);
        close(fd);
        try {

            addWatch(fd, dir2().path(), IN_ALL_EVENTS);
            fail();

        } catch (ErrnoException e) {
            if (e.errno != EINVAL) {
                throw e;
            }
        }
    }

}
