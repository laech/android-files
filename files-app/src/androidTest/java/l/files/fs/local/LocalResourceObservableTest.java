package l.files.fs.local;

import junit.framework.TestCase;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import l.files.common.testing.TempDir;
import l.files.fs.Instant;
import l.files.fs.Permission;
import l.files.fs.Resource;
import l.files.fs.WatchEvent;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.Permission.OWNER_WRITE;
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

    public void testMoveSelfOut() throws Exception {
        Resource file = dir1.resolve("file").createFile();
        Resource dir = dir1.resolve("dir").createDirectory();
        testMoveSelfOut(file, dir2.resolve("a"));
        testMoveSelfOut(dir, dir2.resolve("b"));
    }

    private static void testMoveSelfOut(
            Resource src, Resource dst) throws Exception {
        try (Recorder observer = observe(src)) {
            observer.await(DELETE, src, newMove(src, dst));
        }
    }

    public void testModifyFileContent() throws Exception {
        Resource file = dir1.resolve("a").createFile();
        testModifyFileContent(file, file);
        testModifyFileContent(file, dir1);
    }

    private static void testModifyFileContent(
            Resource file, Resource observable) throws Exception {
        try (Recorder observer = observe(observable)) {
            observer.await(MODIFY, file, newAppend(file, "abc"));
        }
    }

    public void testModifyPermissions() throws Exception {
        Resource file = dir1.resolve("file").createFile();
        Resource dir = dir1.resolve("directory").createDirectory();
        testModifyPermission(file, file);
        testModifyPermission(file, dir1);
        testModifyPermission(dir, dir);
        testModifyPermission(dir, dir1);
    }

    private static void testModifyPermission(
            Resource target, Resource observable) throws Exception {
        Set<Permission> oldPerms = target.readStatus(false).getPermissions();
        Set<Permission> newPerms;
        if (oldPerms.isEmpty()) {
            newPerms = singleton(OWNER_WRITE);
        } else {
            newPerms = emptySet();
        }
        try (Recorder observer = observe(observable)) {
            observer.await(MODIFY, target, newSetPermissions(target, newPerms));
        }
    }

    /*
     * Note: IN_MODIFY is fired instead of the expected IN_ATTRIB when changing
     * the last modified time, and when changing access time both are not fired
     * making it not untrackable.
     */
    public void testModifyModificationTime() throws Exception {
        Resource file = dir1.resolve("file").createFile();
        Resource dir = dir1.resolve("dir").createDirectory();
        testModifyModificationTime(file, file);
        testModifyModificationTime(file, dir1);
        testModifyModificationTime(dir, dir);
        testModifyModificationTime(dir, dir1);
    }

    private void testModifyModificationTime(
            Resource target, Resource observable) throws Exception {
        Instant old = target.readStatus(false).getModificationTime();
        Instant t = Instant.of(old.getSeconds() - 1, old.getNanos());
        try (Recorder observer = observe(observable)) {
            observer.await(MODIFY, target, newSetModificationTime(target, t));
        }
    }

    public void testDelete() throws Exception {
        Resource file = dir1.resolve("file");
        Resource dir = dir1.resolve("dir");
        testDelete(file.createFile(), file);
        testDelete(file.createFile(), dir1);
        testDelete(dir.createDirectory(), dir);
        testDelete(dir.createDirectory(), dir1);
    }

    private static void testDelete(
            Resource target, Resource observable) throws Exception {
        boolean file = target.readStatus(false).isRegularFile();
        try (Recorder observer = observe(observable)) {
            List<WatchEvent> expected = new ArrayList<>();
            // If target is file and observing on the file itself, an IN_ATTRIB
            // event is first sent in addition to IN_DELETE when deleting
            if (file && target.equals(observable)) {
                expected.add(event(MODIFY, target));
            }
            expected.add(event(DELETE, target));
            observer.await(expected, newDelete(target));
        }
    }

    public void testDeleteRecreateDirectoryWillBeObserved() throws Exception {
        Resource dir = dir1.resolve("dir");
        Resource file = dir.resolve("file");
        try (Recorder observer = observe(dir1)) {
            for (int i = 0; i < 10; i++) {
                observer.await(CREATE, dir, newCreateDirectory(dir));
                observer.await(
                        asList(
                                event(MODIFY, dir),
                                event(MODIFY, dir),
                                event(DELETE, dir)
                        ),
                        compose(
                                newCreateFile(file),
                                newDelete(file),
                                newDelete(dir)
                        )
                );
            }
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

    private static Callable<Void> newSetPermissions(
            final Resource resource,
            final Set<Permission> permissions) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                resource.setPermissions(permissions);
                return null;
            }
        };
    }

    private static Callable<Void> newSetModificationTime(
            final Resource resource,
            final Instant instant) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                resource.setModificationTime(instant);
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
