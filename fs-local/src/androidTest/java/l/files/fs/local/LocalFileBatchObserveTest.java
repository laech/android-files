package l.files.fs.local;

import java.io.Closeable;
import java.util.HashSet;
import java.util.Set;

import l.files.fs.BatchObserver;
import l.files.fs.File;
import l.files.fs.FileConsumer;
import l.files.fs.Instant;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
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
    protected void setUp() throws Exception {
        super.setUp();
        observer = mock(BatchObserver.class);
        consumer = mock(FileConsumer.class);
    }

    public void test_notifies_self_change() throws Exception {
        try (Closeable ignored = dir1().observe(NOFOLLOW, observer, consumer, 10, MILLISECONDS)) {
            dir1().setLastModifiedTime(NOFOLLOW, Instant.ofMillis(1));
            verify(observer, timeout(100)).onBatchEvent(true, names());
            verifyNoMoreInteractions(observer);
            verifyZeroInteractions(consumer);
        }
    }

    public void test_notifies_children_change() throws Exception {
        File a = dir1().resolve("a").createFile();
        File b = dir1().resolve("b").createDir();
        dir1().resolve("c").createFile();
        dir1().resolve("d").createDir();

        try (Closeable ignored = dir1().observe(NOFOLLOW, observer, consumer, 10, MILLISECONDS)) {
            a.setLastModifiedTime(NOFOLLOW, Instant.ofMillis(1));
            b.setLastModifiedTime(NOFOLLOW, Instant.ofMillis(2));
            verify(observer, timeout(100)).onBatchEvent(false, names(a, b));
            verify(observer, timeout(100)).onBatchEvent(false, names(a, b));
            verifyNoMoreInteractions(observer);
        }
    }

    public void test_notifies_self_and_children_change() throws Exception {
        File child = dir1().resolve("a").createFile();

        try (Closeable ignored = dir1().observe(NOFOLLOW, observer, consumer, 10, MILLISECONDS)) {

            verify(consumer).accept(child);

            dir1().setLastModifiedTime(NOFOLLOW, Instant.ofMillis(1));
            child.setLastModifiedTime(NOFOLLOW, Instant.ofMillis(2));
            verify(observer, timeout(50)).onBatchEvent(true, names(child));

            child.setLastModifiedTime(NOFOLLOW, Instant.ofMillis(3));
            verify(observer, timeout(50)).onBatchEvent(false, names(child));

            dir1().setLastModifiedTime(NOFOLLOW, Instant.ofMillis(4));
            verify(observer, timeout(50)).onBatchEvent(true, names());

            verifyNoMoreInteractions(observer);

        }
    }

    private static Set<String> names(File... files) {
        Set<String> names = new HashSet<>();
        for (File file : files) {
            names.add(file.name().toString());
        }
        return names;
    }

}
