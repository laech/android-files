package l.files.fs;

import org.junit.Test;

import java.util.HashMap;

import l.files.fs.Path.Consumer;
import l.files.fs.event.BatchObserver;
import l.files.fs.event.Event;
import l.files.fs.event.Observation;
import l.files.testing.fs.PathBaseTest;

import static java.util.Collections.emptyMap;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.event.Event.CREATE;
import static l.files.fs.event.Event.DELETE;
import static l.files.fs.event.Event.MODIFY;
import static org.junit.Assert.assertFalse;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

public final class LocalFileBatchObserveTest extends PathBaseTest {

    private BatchObserver observer;
    private Consumer consumer;

    @Override
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        super.setUp();
        observer = mock(BatchObserver.class);
        consumer = mock(Consumer.class);
        given(consumer.accept(any(Path.class))).willReturn(true);
    }

    @Test
    public void notifies_self_change() throws Exception {
        try (Observation ignored = dir1().observe(
                NOFOLLOW,
                observer,
                consumer,
                10,
                MILLISECONDS,
                false,
                "LocalFileBatchObserveTest.test_notifies_self_change",
                -1)
        ) {

            dir1().setLastModifiedTime(NOFOLLOW, Instant.ofMillis(1));

            verify(observer, timeout(100))
                    .onLatestEvents(true, emptyMap());

            verifyNoMoreInteractions(observer);
            verifyZeroInteractions(consumer);

        }
    }

    @Test
    public void notifies_children_change() throws Exception {

        Path b = dir1().concat("b").createDirectory();
        Path a = dir1().concat("a").createFile();
        Path c = dir1().concat("c").createDirectory();
        Path d = dir1().concat("d");

        dir1().concat("e").createFile();
        dir1().concat("f").createDirectory();

        try (Observation observation = dir1().observe(
                NOFOLLOW,
                observer,
                consumer,
                500,
                MILLISECONDS,
                false,
                "LocalFileBatchObserveTest.test_notifies_children_change",
                -1)
        ) {

            assertFalse(observation.isClosed());

            a.setLastModifiedTime(NOFOLLOW, Instant.ofMillis(1));
            b.setLastModifiedTime(NOFOLLOW, Instant.ofMillis(2));
            c.delete();
            d.createFile();

            verify(observer, timeout(10000)).onLatestEvents(
                    false,
                    new HashMap<Path, Event>() {{
                        put(a.getFileName(), MODIFY);
                        put(b.getFileName(), MODIFY);
                        put(c.getFileName(), DELETE);
                        put(d.getFileName(), CREATE);
                    }});

            verifyNoMoreInteractions(observer);

        }
    }

    @Test
    public void notifies_latest_event() throws Exception {

        Path file = dir1().concat("file").createFile();
        try (Observation ignored = dir1().observe(
                NOFOLLOW,
                observer,
                consumer,
                500,
                MILLISECONDS,
                false,
                "LocalFileBatchObserveTest.test_notifies_latest_event",
                -1)
        ) {

            file.setLastModifiedTime(NOFOLLOW, Instant.ofMillis(1));
            file.delete();
            file.createFile();

            verify(observer, timeout(10000)).onLatestEvents(
                    false,
                    new HashMap<Path, Event>() {{
                        put(file.getFileName(), CREATE);
                    }});

            verifyNoMoreInteractions(observer);

        }
    }

    @Test
    public void notifies_self_and_children_change() throws Exception {

        Path child = dir1().concat("a").createFile();
        try (Observation ignored = dir1().observe(
                NOFOLLOW,
                observer,
                consumer,
                500,
                MILLISECONDS,
                false,
                "LocalFileBatchObserveTest.test_notifies_self_and_children_change",
                -1)
        ) {

            verify(consumer).accept(child);

            dir1().setLastModifiedTime(NOFOLLOW, Instant.ofMillis(1));
            child.setLastModifiedTime(NOFOLLOW, Instant.ofMillis(2));

            verify(observer, timeout(10000)).onLatestEvents(
                    true,
                    new HashMap<Path, Event>() {{
                        put(child.getFileName(), MODIFY);
                    }});

            child.setLastModifiedTime(NOFOLLOW, Instant.ofMillis(3));
            verify(observer, timeout(10000)).onLatestEvents(
                    false,
                    new HashMap<Path, Event>() {{
                        put(child.getFileName(), MODIFY);
                    }});

            dir1().setLastModifiedTime(NOFOLLOW, Instant.ofMillis(4));
            verify(observer, timeout(10000))
                    .onLatestEvents(true, emptyMap());

            verifyNoMoreInteractions(observer);

        }
    }
}
