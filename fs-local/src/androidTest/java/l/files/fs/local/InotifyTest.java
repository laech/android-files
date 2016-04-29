package l.files.fs.local;

import linux.ErrnoException;
import linux.Unistd;

import static l.files.fs.local.Inotify.IN_ALL_EVENTS;
import static linux.Errno.EBADF;
import static org.mockito.Mockito.mock;

public final class InotifyTest extends PathBaseTest {

    public void test_cannot_use_fd_after_close() throws Exception {
        int fd = Inotify.get().init(mock(Inotify.Callback.class));
        Inotify.get().addWatch(fd, dir1().toByteArray(), IN_ALL_EVENTS);
        Unistd.close(fd);
        try {

            Inotify.get().addWatch(fd, dir2().toByteArray(), IN_ALL_EVENTS);
            fail();

        } catch (ErrnoException e) {
            if (e.errno != EBADF) {
                throw e;
            }
        }
    }

}
