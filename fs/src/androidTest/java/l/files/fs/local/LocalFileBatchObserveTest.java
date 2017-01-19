package l.files.fs.local;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;

import l.files.fs.Instant;
import l.files.fs.Name;
import l.files.fs.Path;
import l.files.fs.Path.Consumer;
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
    private Consumer consumer;

    @Override
    protected Path create(File file) {
        return LocalPath.fromFile(file);
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
        Observation observation = dir1().observe(
                NOFOLLOW,
                observer,
                consumer,
                10,
                MILLISECONDS,
                false,
                "LocalFileBatchObserveTest.test_notifies_self_change",
                -1);
        try {

            dir1().setLastModifiedTime(NOFOLLOW, Instant.ofMillis(1));

            verify(observer, timeout(100)).onLatestEvents(
                    true, Collections.<Name, Event>emptyMap());

            verifyNoMoreInteractions(observer);
            verifyZeroInteractions(consumer);

        } finally {
            observation.close();
        }
    }

    public void test_notifies_children_change() throws Exception {

        final Path b = dir1().concat("b").createDir();
        final Path a = dir1().concat("a").createFile();
        final Path c = dir1().concat("c").createDir();
        final Path d = dir1().concat("d");

        dir1().concat("e").createFile();
        dir1().concat("f").createDir();

        Observation observation = dir1().observe(
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

            a.setLastModifiedTime(NOFOLLOW, Instant.ofMillis(1));
            b.setLastModifiedTime(NOFOLLOW, Instant.ofMillis(2));
            c.delete();
            d.createFile();

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

        final Path file = dir1().concat("file").createFile();
        final Observation observation = dir1().observe(
                NOFOLLOW,
                observer,
                consumer,
                500,
                MILLISECONDS,
                false,
                "LocalFileBatchObserveTest.test_notifies_latest_event",
                -1);
        try {

            file.setLastModifiedTime(NOFOLLOW, Instant.ofMillis(1));
            file.delete();
            file.createFile();

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

        final Path child = dir1().concat("a").createFile();
        final Observation observation = dir1().observe(
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

            dir1().setLastModifiedTime(NOFOLLOW, Instant.ofMillis(1));
            child.setLastModifiedTime(NOFOLLOW, Instant.ofMillis(2));

            verify(observer, timeout(10000)).onLatestEvents(
                    true,
                    new HashMap<Name, Event>() {{
                        put(child.name(), MODIFY);
                    }});

            child.setLastModifiedTime(NOFOLLOW, Instant.ofMillis(3));
            verify(observer, timeout(10000)).onLatestEvents(
                    false,
                    new HashMap<Name, Event>() {{
                        put(child.name(), MODIFY);
                    }});

            dir1().setLastModifiedTime(NOFOLLOW, Instant.ofMillis(4));
            verify(observer, timeout(10000)).onLatestEvents(
                    true, Collections.<Name, Event>emptyMap());

            verifyNoMoreInteractions(observer);

        } finally {
            observation.close();
        }
    }
}
