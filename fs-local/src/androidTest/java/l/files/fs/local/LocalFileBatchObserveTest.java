package l.files.fs.local;

import java.util.Collections;
import java.util.HashMap;

import l.files.base.io.Closer;
import l.files.fs.BatchObserver;
import l.files.fs.Event;
import l.files.fs.FileSystem.Consumer;
import l.files.fs.Files;
import l.files.fs.Instant;
import l.files.fs.Name;
import l.files.fs.Observation;
import l.files.fs.Path;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static l.files.fs.Event.CREATE;
import static l.files.fs.Event.DELETE;
import static l.files.fs.Event.MODIFY;
import static l.files.fs.LinkOption.NOFOLLOW;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

public final class LocalFileBatchObserveTest extends PathBaseTest {

    private BatchObserver observer;
    private Consumer<Path> consumer;

    @Override
    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        super.setUp();
        observer = mock(BatchObserver.class);
        consumer = mock(Consumer.class);
        given(consumer.accept(any(Path.class))).willReturn(true);
    }

    public void test_notifies_self_change() throws Exception {
        Closer closer = Closer.create();
        try {

            closer.register(Files.observe(dir1(), NOFOLLOW, observer, consumer, 10, MILLISECONDS, false));

            Files.setLastModifiedTime(dir1(), NOFOLLOW, Instant.ofMillis(1));

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

    public void test_notifies_children_change() throws Exception {

        final Path b = Files.createDir(dir1().resolve("b"));
        final Path a = Files.createFile(dir1().resolve("a"));
        final Path c = Files.createDir(dir1().resolve("c"));
        final Path d = dir1().resolve("d");

        Files.createFile(dir1().resolve("e"));
        Files.createDir(dir1().resolve("f"));

        Closer closer = Closer.create();
        try {

            Observation observation = Files.observe(dir1(), NOFOLLOW, observer, consumer, 30, MILLISECONDS, false);
            closer.register(observation);
            assertFalse(observation.isClosed());

            Files.setLastModifiedTime(a, NOFOLLOW, Instant.ofMillis(1));
            Files.setLastModifiedTime(b, NOFOLLOW, Instant.ofMillis(2));
            Files.delete(c);
            Files.createFile(d);

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

    public void test_notifies_latest_event() throws Exception {

        final Path file = Files.createFile(dir1().resolve("file"));

        Closer closer = Closer.create();
        try {

            closer.register(Files.observe(dir1(), NOFOLLOW, observer, consumer, 30, MILLISECONDS, false));

            Files.setLastModifiedTime(file, NOFOLLOW, Instant.ofMillis(1));
            Files.delete(file);
            Files.createFile(file);

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

    public void test_notifies_self_and_children_change() throws Exception {

        final Path child = Files.createFile(dir1().resolve("a"));

        Closer closer = Closer.create();
        try {
            closer.register(Files.observe(dir1(), NOFOLLOW, observer, consumer, 10, MILLISECONDS, false));

            verify(consumer).accept(child);

            Files.setLastModifiedTime(dir1(), NOFOLLOW, Instant.ofMillis(1));
            Files.setLastModifiedTime(child, NOFOLLOW, Instant.ofMillis(2));

            verify(observer, timeout(100)).onLatestEvents(
                    true,
                    new HashMap<Name, Event>() {{
                        put(child.name(), MODIFY);
                    }});

            Files.setLastModifiedTime(child, NOFOLLOW, Instant.ofMillis(3));
            verify(observer, timeout(100)).onLatestEvents(
                    false,
                    new HashMap<Name, Event>() {{
                        put(child.name(), MODIFY);
                    }});

            Files.setLastModifiedTime(dir1(), NOFOLLOW, Instant.ofMillis(4));
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
