package l.files.fs.local;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import l.files.fs.Instant;
import l.files.fs.LinkOption;
import l.files.fs.Permission;
import l.files.fs.Resource;
import l.files.fs.WatchEvent;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Permission.OWNER_WRITE;
import static l.files.fs.WatchEvent.Kind.CREATE;
import static l.files.fs.WatchEvent.Kind.DELETE;
import static l.files.fs.WatchEvent.Kind.MODIFY;
import static l.files.fs.local.LocalResource_observe_Test.Recorder.observe;

/**
 * @see Resource#observe(LinkOption, WatchEvent.Listener)
 */
public final class LocalResource_observe_Test extends ResourceBaseTest {

    public void testObserveOnLinkNoFollow() throws Exception {
        Resource dir = dir1().resolve("dir").createDirectory();
        Resource link = dir1().resolve("link").createSymbolicLink(dir);
        Resource file = link.resolve("file");
        try (Recorder observer = observe(link, NOFOLLOW)) {
            // No follow, can observe the link
            observer.await(MODIFY, link,
                    newSetModificationTime(link, NOFOLLOW, Instant.of(1, 1)));
            // But not the child content
            try {
                observer.await(CREATE, file, newCreateFile(file));
            } catch (AssertionError e) {
                assertTrue(observer.actual.isEmpty());
                return;
            }
            fail();
        }
    }

    public void testObserveOnLinkFollow() throws Exception {
        Resource dir = dir1().resolve("dir").createDirectory();
        Resource link = dir1().resolve("link").createSymbolicLink(dir);
        Resource child = link.resolve("dir").createDirectory();
        try (Recorder observer = observe(link, FOLLOW)) {
            observer.await(MODIFY, child, newCreateFile(child.resolve("a")));
        }
    }

    public void testMoveDirectoryInThenAddFileIntoIt() throws Exception {
        Resource dst = dir1().resolve("a");
        Resource src = dir2().resolve("a").createDirectory();
        try (Recorder observer = observe(dir1())) {
            observer.await(CREATE, dst, newMove(src, dst));
            observer.await(MODIFY, dst, newCreateDirectory(dst.resolve("b")));
        }
    }

    public void testMoveDirectoryInThenDeleteFileFromIt() throws Exception {
        Resource dstDir = dir1().resolve("a");
        Resource srcDir = dir2().resolve("a").createDirectory();
        srcDir.resolve("b").createFile();
        try (Recorder observer = observe(dir1())) {
            observer.await(CREATE, dstDir, newMove(srcDir, dstDir));
            observer.await(MODIFY, dstDir, newDelete(dstDir.resolve("b")));
        }
    }

    public void testMoveDirectoryInThenMoveFileIntoIt() throws Exception {
        Resource dir = dir1().resolve("a");
        Resource src1 = dir2().resolve("a").createDirectory();
        Resource src2 = dir2().resolve("b").createFile();
        try (Recorder observer = observe(dir1())) {
            observer.await(CREATE, dir, newMove(src1, dir));
            observer.await(MODIFY, dir, newMove(src2, dir.resolve("b")));
        }
    }

    public void testMoveDirectoryInThenMoveFileOutOfIt() throws Exception {
        Resource src = dir2().resolve("a").createDirectory();
        Resource dir = dir1().resolve("a");
        Resource child = dir.resolve("b");
        try (Recorder observer = observe(dir1())) {
            observer.await(CREATE, dir, newMove(src, dir));
            observer.await(
                    asList(
                            event(MODIFY, dir),
                            event(MODIFY, dir)
                    ),
                    compose(
                            newCreateFile(child),
                            newMove(child, dir2().resolve("b"))
                    )
            );
        }
    }

    public void testMoveFileIn() throws Exception {
        Resource src = dir2().resolve("a").createFile();
        Resource dst = dir1().resolve("b");
        try (Recorder observer = observe(dir1())) {
            observer.await(CREATE, dst, newMove(src, dst));
        }
    }

    public void testMoveFileOut() throws Exception {
        Resource file = dir1().resolve("a").createFile();
        try (Recorder observer = observe(dir1())) {
            observer.await(DELETE, file, newMove(file, dir2().resolve("a")));
        }
    }

    public void testMoveSelfOut() throws Exception {
        Resource file = dir1().resolve("file").createFile();
        Resource dir = dir1().resolve("dir").createDirectory();
        testMoveSelfOut(file, dir2().resolve("a"));
        testMoveSelfOut(dir, dir2().resolve("b"));
    }

    private static void testMoveSelfOut(
            Resource src, Resource dst) throws Exception {
        try (Recorder observer = observe(src)) {
            observer.await(DELETE, src, newMove(src, dst));
        }
    }

    public void testModifyFileContent() throws Exception {
        Resource file = dir1().resolve("a").createFile();
        testModifyFileContent(file, file);
        testModifyFileContent(file, dir1());
    }

    private static void testModifyFileContent(
            Resource file, Resource observable) throws Exception {
        try (Recorder observer = observe(observable)) {
            observer.await(MODIFY, file, newAppend(file, "abc"));
        }
    }

    public void testModifyPermissions() throws Exception {
        Resource file = dir1().resolve("file").createFile();
        Resource dir = dir1().resolve("directory").createDirectory();
        testModifyPermission(file, file);
        testModifyPermission(file, dir1());
        testModifyPermission(dir, dir);
        testModifyPermission(dir, dir1());
    }

    private static void testModifyPermission(
            Resource target, Resource observable) throws Exception {
        Set<Permission> oldPerms = target.readStatus(NOFOLLOW).getPermissions();
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
        Resource file = dir1().resolve("file").createFile();
        Resource dir = dir1().resolve("dir").createDirectory();
        testModifyModificationTime(file, file);
        testModifyModificationTime(file, dir1());
        testModifyModificationTime(dir, dir);
        testModifyModificationTime(dir, dir1());
    }

    private void testModifyModificationTime(
            Resource target, Resource observable) throws Exception {
        Instant old = target.readStatus(NOFOLLOW).getModificationTime();
        Instant t = Instant.of(old.getSeconds() - 1, old.getNanos());
        try (Recorder observer = observe(observable)) {
            observer.await(MODIFY, target,
                    newSetModificationTime(target, NOFOLLOW, t));
        }
    }

    public void testDelete() throws Exception {
        Resource file = dir1().resolve("file");
        Resource dir = dir1().resolve("dir");
        testDelete(file.createFile(), file);
        testDelete(file.createFile(), dir1());
        testDelete(dir.createDirectory(), dir);
        testDelete(dir.createDirectory(), dir1());
    }

    private static void testDelete(
            Resource target, Resource observable) throws Exception {
        boolean file = target.readStatus(NOFOLLOW).isRegularFile();
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
        Resource dir = dir1().resolve("dir");
        Resource file = dir.resolve("file");
        try (Recorder observer = observe(dir1())) {
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

    public void testCreate() throws Exception {
        Resource file = dir1().resolve("file");
        Resource dir = dir1().resolve("dir");
        Resource link = dir1().resolve("link");
        testCreateFile(file, dir1());
        testCreateDirectory(dir, dir1());
        testCreateSymbolicLink(link, dir1(), dir1());
    }

    private static void testCreateFile(
            Resource target, Resource observable) throws Exception {
        try (Recorder observer = observe(observable)) {
            observer.await(CREATE, target, newCreateFile(target));
        }
    }

    private static void testCreateDirectory(
            Resource target, Resource observable) throws Exception {
        try (Recorder observer = observe(observable)) {
            observer.await(CREATE, target, newCreateDirectory(target));
        }
    }

    private static void testCreateSymbolicLink(
            Resource link,
            Resource target,
            Resource observable) throws Exception {
        try (Recorder observer = observe(observable)) {
            observer.await(CREATE, link, newCreateSymbolicLink(link, target));
        }
    }

    public void testCreateDirectoryThenCreateItemsIntoIt() throws Exception {
        Resource dir = dir1().resolve("dir");
        try (Recorder observer = observe(dir1())) {
            observer.await(CREATE, dir, newCreateDirectory(dir));
            observer.await(
                    asList(
                            event(MODIFY, dir),
                            event(MODIFY, dir),
                            event(MODIFY, dir)
                    ),
                    compose(
                            newCreateFile(dir.resolve("file")),
                            newCreateDirectory(dir.resolve("dir2()")),
                            newCreateSymbolicLink(dir.resolve("link"), dir1())
                    )
            );
        }
    }

    public void testCreateDirectoryThenDeleteItemsFromIt() throws Exception {
        Resource parent = dir1().resolve("parent");
        Resource file = parent.resolve("file");
        Resource dir = parent.resolve("dir");
        try (Recorder observer = observe(dir1())) {
            observer.await(CREATE, parent, newCreateDirectory(parent));
            observer.await(
                    asList(
                            event(MODIFY, parent),
                            event(MODIFY, parent),
                            event(MODIFY, parent),
                            event(MODIFY, parent)
                    ),
                    compose(
                            newCreateFile(file),
                            newCreateDirectory(dir),
                            newDelete(file),
                            newDelete(dir)
                    )
            );
        }
    }

    public void testCreateDirectoryThenMoveItemsOutOfIt() throws Exception {
        Resource parent = dir1().resolve("parent");
        Resource file = parent.resolve("file");
        Resource dir = parent.resolve("dir");
        try (Recorder observer = observe(dir1())) {
            observer.await(CREATE, parent, newCreateDirectory(parent));
            observer.await(
                    asList(
                            event(MODIFY, parent),
                            event(MODIFY, parent),
                            event(MODIFY, parent),
                            event(MODIFY, parent)
                    ),
                    compose(
                            newCreateFile(file),
                            newCreateDirectory(dir),
                            newMove(file, dir2().resolve("file")),
                            newMove(dir, dir2().resolve("dir"))
                    )
            );
        }
    }

    public void testCreateDirectoryThenMoveFileIntoIt() throws Exception {
        Resource parent = dir1().resolve("parent");
        Resource file = dir2().resolve("file").createFile();
        Resource dir = dir2().resolve("dir").createDirectory();
        try (Recorder observer = observe(dir1())) {
            observer.await(CREATE, parent, newCreateDirectory(parent));
            observer.await(
                    asList(
                            event(MODIFY, parent),
                            event(MODIFY, parent)
                    ),
                    compose(
                            newMove(file, parent.resolve("file")),
                            newMove(dir, parent.resolve("dir"))
                    )
            );
        }
    }

    public void testMultipleOperations() throws Exception {
        Resource a = dir1().resolve("a");
        Resource b = dir1().resolve("b");
        Resource c = dir1().resolve("c");
        Resource d = dir1().resolve("d");
        try (Recorder observer = observe(dir1())) {
            observer.await(CREATE, a, newCreateDirectory(a));
            observer.await(CREATE, b, newCreateDirectory(b));
            observer.await(MODIFY, a, newCreateFile(a.resolve("1")));
            observer.await(CREATE, c, newMove(dir2().resolve("c").createFile(), c));
            observer.await(DELETE, c, newMove(c, dir2().resolve("2")));
            observer.await(DELETE, b, newDelete(b));
            observer.await(CREATE, d, newCreateFile(d));
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
                src.moveTo(dst);
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

    private static Callable<Void> newCreateSymbolicLink(
            final Resource link,
            final Resource target) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                link.createSymbolicLink(target);
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
                try (OutputStream out = file.openOutputStream(NOFOLLOW, true)) {
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
            final LinkOption option,
            final Instant instant) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                resource.setModificationTime(option, instant);
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
            return observe(observable, NOFOLLOW);
        }

        static Recorder observe(Resource observable, LinkOption option) throws IOException {
            Recorder observer = new Recorder();
            observer.subscription = observable.observe(option, observer);
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
