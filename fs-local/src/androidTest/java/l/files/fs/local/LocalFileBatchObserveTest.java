package l.files.fs.local;

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;

import l.files.base.io.Closer;
import l.files.fs.BatchObserver;
import l.files.fs.Event;
import l.files.fs.File;
import l.files.fs.FileConsumer;
import l.files.fs.Instant;
import l.files.fs.Name;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static l.files.fs.Event.CREATE;
import static l.files.fs.Event.DELETE;
import static l.files.fs.Event.MODIFY;
import static l.files.fs.LinkOption.NOFOLLOW;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

public final class LocalFileBatchObserveTest extends FileBaseTest {

    private BatchObserver observer;
    private FileConsumer consumer;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        observer = mock(BatchObserver.class);
        consumer = mock(FileConsumer.class);
    }

    @Test
    public void notifies_self_change() throws Exception {
        Closer closer = Closer.create();
        try {

            closer.register(dir1().observe(NOFOLLOW, observer, consumer, 10, MILLISECONDS));

            dir1().setLastModifiedTime(NOFOLLOW, Instant.ofMillis(1));

            verify(observer, timeout(100)).onLatestEvents(
                    true, Collections.<Name, Event>emptyMap());

            verifyNoMoreInteractions(observer);
            verifyZeroInteractions(consumer);

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Test
    public void notifies_children_change() throws Exception {

        final File a = dir1().resolve("a").createFile();
        final File b = dir1().resolve("b").createDir();
        final File c = dir1().resolve("c").createDir();
        final File d = dir1().resolve("d");

        dir1().resolve("e").createFile();
        dir1().resolve("f").createDir();

        Closer closer = Closer.create();
        try {

            closer.register(dir1().observe(NOFOLLOW, observer, consumer, 30, MILLISECONDS));

            a.setLastModifiedTime(NOFOLLOW, Instant.ofMillis(1));
            b.setLastModifiedTime(NOFOLLOW, Instant.ofMillis(2));
            c.delete();
            d.createFile();

            verify(observer, timeout(100)).onLatestEvents(
                    false,
                    new HashMap<Name, Event>() {{
                        put(a.name(), MODIFY);
                        put(b.name(), MODIFY);
                        put(c.name(), DELETE);
                        put(d.name(), CREATE);
                    }});

            verifyNoMoreInteractions(observer);

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Test
    public void notifies_latest_event() throws Exception {

        final File file = dir1().resolve("file").createFile();

        Closer closer = Closer.create();
        try {

            closer.register(dir1().observe(NOFOLLOW, observer, consumer, 30, MILLISECONDS));

            file.setLastModifiedTime(NOFOLLOW, Instant.ofMillis(1));
            file.delete();
            file.createFile();

            verify(observer, timeout(100)).onLatestEvents(
                    false,
                    new HashMap<Name, Event>() {{
                        put(file.name(), CREATE);
                    }});

            verifyNoMoreInteractions(observer);

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Test
    public void notifies_self_and_children_change() throws Exception {

        final File child = dir1().resolve("a").createFile();

        Closer closer = Closer.create();
        try {
            closer.register(dir1().observe(NOFOLLOW, observer, consumer, 10, MILLISECONDS));

            verify(consumer).accept(child);

            dir1().setLastModifiedTime(NOFOLLOW, Instant.ofMillis(1));
            child.setLastModifiedTime(NOFOLLOW, Instant.ofMillis(2));

            verify(observer, timeout(100)).onLatestEvents(
                    true,
                    new HashMap<Name, Event>() {{
                        put(child.name(), MODIFY);
                    }});

            child.setLastModifiedTime(NOFOLLOW, Instant.ofMillis(3));
            verify(observer, timeout(100)).onLatestEvents(
                    false,
                    new HashMap<Name, Event>() {{
                        put(child.name(), MODIFY);
                    }});

            dir1().setLastModifiedTime(NOFOLLOW, Instant.ofMillis(4));
            verify(observer, timeout(100)).onLatestEvents(
                    true, Collections.<Name, Event>emptyMap());

            verifyNoMoreInteractions(observer);

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

}
