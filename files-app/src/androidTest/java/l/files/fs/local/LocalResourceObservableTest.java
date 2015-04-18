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
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.WatchEvent.Kind.CREATE;
import static l.files.fs.WatchEvent.Kind.DELETE;
import static l.files.fs.WatchEvent.Kind.MODIFY;
import static l.files.fs.local.LocalResourceObservableTest.Recorder.observe;

public final class LocalResourceObservableTest extends TestCase {

    private TempDir tmp1;
    private TempDir tmp2;
    private Resource dir1;
    private Resource dir2;

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

    public void testMoveDirectoryInThenAddFileIntoIt() throws Exception {
        Resource dst = dir1.resolve("a");
        Resource src = dir2.resolve("a").createDirectory();
        try (Recorder observer = observe(dir1)) {
            observer.await(CREATE, dst, newMove(src, dst));
            observer.await(MODIFY, dst, newCreateDirectory(dst.resolve("b")));
        }
    }

    /**
     * Directory moved into the monitored directory should be monitored for
     * files deletions in that directory, as that will change the new
     * directory's last modified date.
     */
    public void testMoveDirectoryInThenDeleteFileFromIt() throws Exception {
        Resource dstDir = dir1.resolve("a");
        Resource srcDir = dir2.resolve("a").createDirectory();
        srcDir.resolve("b").createFile();
        try (Recorder observer = observe(dir1)) {
            observer.await(CREATE, dstDir, newMove(srcDir, dstDir));
            observer.await(MODIFY, dstDir, newDelete(dstDir.resolve("b")));
        }
    }

    /**
     * Directory moved into the monitored directory should be monitored for
     * files moving into that directory, as that will change the new directory's
     * last modified date.
     */
    public void testMoveDirectoryInThenMoveFileIntoIt() throws Exception {
        Resource dir = dir1.resolve("a");
        Resource src1 = dir2.resolve("a").createDirectory();
        Resource src2 = dir2.resolve("b").createFile();
        try (Recorder observer = observe(dir1)) {
            observer.await(CREATE, dir, newMove(src1, dir));
            observer.await(MODIFY, dir, newMove(src2, dir.resolve("b")));
        }
    }

    /**
     * Directory moved into the monitored directory should be monitored for
     * files moving out of the directory, as that will change the directory's
     * last modified date.
     */
    public void testMoveDirectoryInThenMoveFileOutOfIt() throws Exception {
        Resource src = dir2.resolve("a").createDirectory();
        Resource dir = dir1.resolve("a");
        Resource child = dir.resolve("b");
        try (Recorder observer = observe(dir1)) {
            observer.await(CREATE, dir, newMove(src, dir));
            observer.await(
                    asList(
                            event(MODIFY, dir),
                            event(MODIFY, dir)
                    ),
                    compose(
                            newCreateFile(child),
                            newMove(child, dir2.resolve("b"))
                    )
            );
        }
    }

    public void testMoveFileIn() throws Exception {
        Resource src = dir2.resolve("a").createFile();
        Resource dst = dir1.resolve("b");
        try (Recorder observer = observe(dir1)) {
            observer.await(CREATE, dst, newMove(src, dst));
        }
    }

    public void testMoveFileOut() throws Exception {
        Resource file = dir1.resolve("a").createFile();
        try (Recorder observer = observe(dir1)) {
            observer.await(DELETE, file, newMove(file, dir2.resolve("a")));
        }
    }

    public void testMoveSelfDirectoryOut() throws Exception {
        try (Recorder observer = observe(dir1)) {
            observer.await(DELETE, dir1, newMove(dir1, dir2.resolve("a")));
        }
    }

    public void testMoveSelfFileOut() throws Exception {
        Resource file = dir1.resolve("file").createFile();
        try (Recorder observer = observe(file)) {
            observer.await(DELETE, file, newMove(file, dir2.resolve("a")));
        }
    }

    public void testModifyFileObservingFromParent() throws Exception {
        Resource file = dir1.resolve("a").createFile();
        try (Recorder observer = observe(dir1)) {
            observer.await(MODIFY, file, newAppend(file, "abc"));
        }
    }

    public void testModifyFileObservingFromSelf() throws Exception {
        Resource file = dir1.resolve("a").createFile();
        try (Recorder observer = observe(file)) {
            observer.await(MODIFY, file, newAppend(file, "abc"));
        }
    }

    private static WatchEvent event(WatchEvent.Kind kind, Resource resource) {
        return WatchEvent.create(kind, resource);
    }

    private static Callable<Void> compose(final Callable<?>... callables) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                for (Callable<?> callable : callables) {
                    callable.call();
                }
                return null;
            }
        };
    }

    private static Callable<Void> newMove(final Resource src, final Resource dst) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                src.renameTo(dst);
                return null;
            }
        };
    }

    private static Callable<Void> newDelete(final Resource resource) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                resource.delete();
                return null;
            }
        };
    }

    private static Callable<Void> newCreateFile(final Resource file) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                file.createFile();
                return null;
            }
        };
    }

    private static Callable<Void> newCreateDirectory(final Resource directory) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                directory.createDirectory();
                return null;
            }
        };
    }

    private static Callable<Void> newAppend(
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

    static final class Recorder implements WatchEvent.Listener, Closeable {

        private Closeable subscription;
        private volatile List<WatchEvent> expected;
        private volatile List<WatchEvent> actual;
        private volatile CountDownLatch success;

        static Recorder observe(Resource observable) throws IOException {
            Recorder observer = new Recorder();
            observer.subscription = observable.observe(observer);
            return observer;
        }

        @Override
        public void close() throws IOException {
            subscription.close();
        }

        @Override
        public void onEvent(WatchEvent event) {
            actual.add(event);
            if (expected.equals(actual)) {
                success.countDown();
            }
        }

        void await(WatchEvent.Kind kind,
                   Resource resource,
                   Callable<?> action) throws Exception {
            await(WatchEvent.create(kind, resource), action);
        }

        void await(WatchEvent expected, Callable<?> action) throws Exception {
            await(singletonList(expected), action);
        }

        void await(List<WatchEvent> expected, Callable<?> action) throws Exception {
            this.actual = new ArrayList<>();
            this.expected = new ArrayList<>(expected);
            this.success = new CountDownLatch(1);
            action.call();
            if (!success.await(1, SECONDS)) {
                fail("\nexpected: " + this.expected
                        + "\nactual:   " + this.actual);
            }
        }
    }

}
