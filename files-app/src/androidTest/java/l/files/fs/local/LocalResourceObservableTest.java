package l.files.fs.local;

import junit.framework.TestCase;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import l.files.common.testing.TempDir;
import l.files.fs.Resource;
import l.files.fs.WatchEvent;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.synchronizedList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.WatchEvent.Kind.MODIFY;

public final class LocalResourceObservableTest extends TestCase {

    private TempDir tmp1;
    private TempDir tmp2;
    private LocalResource dir1;
    private LocalResource dir2;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tmp1 = TempDir.create();
        tmp2 = TempDir.create();
        dir1 = LocalResource.create(tmp1.get());
        dir2 = LocalResource.create(tmp2.get());
    }

    @Override
    protected void tearDown() throws Exception {
        tmp1.delete();
        tmp2.delete();
        super.tearDown();
    }

    public void testModifyFile() throws Exception {
        Resource file = createFile(dir1.resolve("a"));
        await(dir1, appending(file, "abc"), event(MODIFY, file));
    }

    private static WatchEvent event(WatchEvent.Kind kind, Resource resource) {
        return WatchEvent.create(kind, resource);
    }

    private static Callable<Void> appending(
            final Resource file,
            final CharSequence content) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                try (OutputStream out = file.openOutputStream(true)) {
                    out.write(content.toString().getBytes(UTF_8));
                }
                return null;
            }
        };
    }

    /**
     * Observes on the given resource, execute the action, and expected the
     * given events to be received.
     */
    private static void await(
            Resource observable,
            Callable<?> action,
            WatchEvent... expected) throws Exception {
        Recorder observer = new Recorder(expected);
        try (Closeable ignored = observable.observe(observer)) {
            action.call();
            observer.await();
        }
    }

    private static Resource createFile(Resource file) throws IOException {
        file.createFile();
        return file;
    }

    private static final class Recorder implements WatchEvent.Listener {

        private final List<WatchEvent> expected;
        private final List<WatchEvent> actual;
        private final CountDownLatch latch;

        private Recorder(WatchEvent... expected) {
            this.expected = synchronizedList(asList(expected));
            this.actual = synchronizedList(new ArrayList<WatchEvent>());
            this.latch = new CountDownLatch(1);
        }

        @Override
        public void onEvent(WatchEvent event) {
            actual.add(event);
            if (expected.equals(actual)) {
                latch.countDown();
            }
        }

        void await() throws InterruptedException {
            if (!latch.await(1, SECONDS)) {
                fail("\nexpected: " + expected + "\nactual:   " + actual);
            }
        }

    }

}
