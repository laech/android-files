package l.files.fs;

import l.files.fs.event.BatchObserver;
import l.files.fs.event.Event;
import l.files.fs.event.Observation;
import l.files.testing.fs.PathBaseTest;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.function.Consumer;

import static java.nio.file.Files.*;
import static java.util.Collections.emptyMap;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.event.Event.*;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

public final class LocalFileBatchObserveTest extends PathBaseTest {

    private BatchObserver observer;
    private Consumer<Path> consumer;

    @Override
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        super.setUp();
        observer = mock(BatchObserver.class);
        consumer = mock(Consumer.class);
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
            -1
        )
        ) {

            dir1().setLastModifiedTime(FileTime.fromMillis(1));

            verify(observer, timeout(100))
                .onLatestEvents(true, emptyMap());

            verifyNoMoreInteractions(observer);
            verifyZeroInteractions(consumer);

        }
    }

    @Test
    public void notifies_children_change() throws Exception {

        Path b =
            createDirectory(dir1().toJavaPath().resolve("b"));
        Path a = createFile(dir1().toJavaPath().resolve("a"));
        Path c =
            createDirectory(dir1().toJavaPath().resolve("c"));
        Path d = dir1().toJavaPath().resolve("d");

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
            -1
        )) {

            assertFalse(observation.isClosed());

            setLastModifiedTime(a, FileTime.fromMillis(1));
            setLastModifiedTime(b, FileTime.fromMillis(2));
            delete(c);
            createFile(d);

            verify(observer, timeout(10000)).onLatestEvents(
                false,
                new HashMap<Path, Event>() {{
                    put(a.getFileName(), MODIFY);
                    put(b.getFileName(), MODIFY);
                    put(c.getFileName(), DELETE);
                    put(d.getFileName(), CREATE);
                }}
            );

            verifyNoMoreInteractions(observer);

        }
    }

    @Test
    public void notifies_latest_event() throws Exception {

        Path file =
            createFile(dir1().toJavaPath().resolve("file"));
        try (Observation ignored = dir1().observe(
            NOFOLLOW,
            observer,
            consumer,
            500,
            MILLISECONDS,
            false,
            "LocalFileBatchObserveTest.test_notifies_latest_event",
            -1
        )) {

            setLastModifiedTime(file, FileTime.fromMillis(1));
            delete(file);
            createFile(file);

            verify(observer, timeout(10000)).onLatestEvents(
                false,
                new HashMap<Path, Event>() {{
                    put(file.getFileName(), CREATE);
                }}
            );

            verifyNoMoreInteractions(observer);

        }
    }

    @Test
    public void notifies_self_and_children_change() throws Exception {

        Path child = createFile(dir1().toJavaPath().resolve("a"));
        try (Observation ignored = dir1().observe(
            NOFOLLOW,
            observer,
            consumer,
            500,
            MILLISECONDS,
            false,
            "LocalFileBatchObserveTest.test_notifies_self_and_children_change",
            -1
        )) {

            verify(consumer).accept(child);

            setLastModifiedTime(dir1().toJavaPath(), FileTime.fromMillis(1));
            setLastModifiedTime(child, FileTime.fromMillis(2));

            verify(observer, timeout(10000)).onLatestEvents(
                true,
                new HashMap<Path, Event>() {{
                    put(child.getFileName(), MODIFY);
                }}
            );

            setLastModifiedTime(child, FileTime.fromMillis(3));
            verify(observer, timeout(10000)).onLatestEvents(
                false,
                new HashMap<Path, Event>() {{
                    put(child.getFileName(), MODIFY);
                }}
            );

            dir1().setLastModifiedTime(FileTime.fromMillis(4));
            verify(observer, timeout(10000))
                .onLatestEvents(true, emptyMap());

            verifyNoMoreInteractions(observer);

        }
    }
}
