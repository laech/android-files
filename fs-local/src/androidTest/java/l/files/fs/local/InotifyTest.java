package l.files.fs.local;

import org.junit.Test;

import static l.files.fs.local.ErrnoException.EBADF;
import static l.files.fs.local.Inotify.IN_ALL_EVENTS;
import static l.files.fs.local.Unistd.close;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public final class InotifyTest extends FileBaseTest {

    @Test
    public void cannot_use_fd_after_close() throws Exception {
        int fd = Inotify.get().init(mock(Inotify.Callback.class));
        Inotify.get().addWatch(fd, dir1().pathBytes(), IN_ALL_EVENTS);
        close(fd);
        try {

            Inotify.get().addWatch(fd, dir2().pathBytes(), IN_ALL_EVENTS);
            fail();

        } catch (ErrnoException e) {
            if (e.errno != EBADF) {
                throw e;
            }
        }
    }

}
