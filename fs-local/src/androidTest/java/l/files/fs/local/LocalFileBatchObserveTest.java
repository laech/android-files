package l.files.fs.local;

import java.util.Collections;
import java.util.HashMap;

import l.files.fs.FileSystem.Consumer;
import l.files.fs.Instant;
import l.files.fs.Name;
import l.files.fs.Path;
import l.files.fs.event.BatchObserver;
import l.files.fs.event.Event;
import l.files.fs.event.Observation;
import l.files.testing.fs.PathBaseTest;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.event.Event.CREATE;
import static l.files.fs.event.Event.DELETE;
import static l.files.fs.event.Event.MODIFY;
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

    public LocalFileBatchObserveTest() {
        super(LocalFileSystem.INSTANCE);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        super.setUp();
        observer = mock(BatchObserver.class);
        consumer = mock(Consumer.class);
        given(consumer.accept(any(Path.class))).willReturn(true);
    }

    public void test_notifies_self_change() throws Exception {
        Observation observation = fs.observe(
                dir1(),
                NOFOLLOW,
                observer,
                consumer,
                10,
                MILLISECONDS,
                false,
                "LocalFileBatchObserveTest.test_notifies_self_change",
                -1);
        try {

            fs.setLastModifiedTime(dir1(), NOFOLLOW, Instant.ofMillis(1));

            verify(observer, timeout(100)).onLatestEvents(
                    true, Collections.<Name, Event>emptyMap());

            verifyNoMoreInteractions(observer);
            verifyZeroInteractions(consumer);

        } finally {
            observation.close();
        }
    }

    public void test_notifies_children_change() throws Exception {

        final Path b = fs.createDir(dir1().concat("b"));
        final Path a = fs.createFile(dir1().concat("a"));
        final Path c = fs.createDir(dir1().concat("c"));
        final Path d = dir1().concat("d");

        fs.createFile(dir1().concat("e"));
        fs.createDir(dir1().concat("f"));

        Observation observation = fs.observe(
                dir1(),
                NOFOLLOW,
                observer,
                consumer,
                500,
                MILLISECONDS,
                false,
                "LocalFileBatchObserveTest.test_notifies_children_change",
                -1);
        try {

            assertFalse(observation.isClosed());

            fs.setLastModifiedTime(a, NOFOLLOW, Instant.ofMillis(1));
            fs.setLastModifiedTime(b, NOFOLLOW, Instant.ofMillis(2));
            fs.delete(c);
            fs.createFile(d);

            verify(observer, timeout(10000)).onLatestEvents(
                    false,
                    new HashMap<Name, Event>() {{
                        put(a.name(), MODIFY);
                        put(b.name(), MODIFY);
                        put(c.name(), DELETE);
                        put(d.name(), CREATE);
                    }});

            verifyNoMoreInteractions(observer);

        } finally {
            observation.close();
        }
    }

    public void test_notifies_latest_event() throws Exception {

        final Path file = fs.createFile(dir1().concat("file"));
        final Observation observation = fs.observe(
                dir1(),
                NOFOLLOW,
                observer,
                consumer,
                500,
                MILLISECONDS,
                false,
                "LocalFileBatchObserveTest.test_notifies_latest_event",
                -1);
        try {

            fs.setLastModifiedTime(file, NOFOLLOW, Instant.ofMillis(1));
            fs.delete(file);
            fs.createFile(file);

            verify(observer, timeout(10000)).onLatestEvents(
                    false,
                    new HashMap<Name, Event>() {{
                        put(file.name(), CREATE);
                    }});

            verifyNoMoreInteractions(observer);

        } finally {
            observation.close();
        }
    }

    public void test_notifies_self_and_children_change() throws Exception {

        final Path child = fs.createFile(dir1().concat("a"));
        final Observation observation = fs.observe(
                dir1(),
                NOFOLLOW,
                observer,
                consumer,
                500,
                MILLISECONDS,
                false,
                "LocalFileBatchObserveTest.test_notifies_self_and_children_change",
                -1);
        try {

            verify(consumer).accept(child);

            fs.setLastModifiedTime(dir1(), NOFOLLOW, Instant.ofMillis(1));
            fs.setLastModifiedTime(child, NOFOLLOW, Instant.ofMillis(2));

            verify(observer, timeout(10000)).onLatestEvents(
                    true,
                    new HashMap<Name, Event>() {{
                        put(child.name(), MODIFY);
                    }});

            fs.setLastModifiedTime(child, NOFOLLOW, Instant.ofMillis(3));
            verify(observer, timeout(10000)).onLatestEvents(
                    false,
                    new HashMap<Name, Event>() {{
                        put(child.name(), MODIFY);
                    }});

            fs.setLastModifiedTime(dir1(), NOFOLLOW, Instant.ofMillis(4));
            verify(observer, timeout(10000)).onLatestEvents(
                    true, Collections.<Name, Event>emptyMap());

            verifyNoMoreInteractions(observer);

        } finally {
            observation.close();
        }
    }

}
