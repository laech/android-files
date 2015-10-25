package l.files.fs.local;

import static l.files.fs.local.ErrnoException.EBADF;
import static l.files.fs.local.Inotify.IN_ALL_EVENTS;
import static l.files.fs.local.Unistd.close;
import static org.mockito.Mockito.mock;

public final class InotifyTest extends FileBaseTest {

    public void test_cannot_use_fd_after_close() throws Exception {
        int fd = Inotify.get().init(mock(Inotify.Callback.class));
        Inotify.get().addWatch(fd, dir1().path(), IN_ALL_EVENTS);
        close(fd);
        try {

            Inotify.get().addWatch(fd, dir2().path(), IN_ALL_EVENTS);
            fail();

        } catch (ErrnoException e) {
            if (e.errno != EBADF) {
                throw e;
            }
        }
    }

}
