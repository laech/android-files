package l.files.fs.local;

import com.google.auto.value.AutoValue;

import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;

import l.files.fs.Event;
import l.files.fs.File;
import l.files.fs.FileConsumer;
import l.files.fs.Instant;
import l.files.fs.LinkOption;
import l.files.fs.Observation;
import l.files.fs.Observer;
import l.files.fs.Permission;
import l.files.fs.Stream;
import l.files.fs.local.Inotify.Tracker;

import static android.os.Environment.getExternalStorageDirectory;
import static java.lang.Integer.parseInt;
import static java.lang.Thread.sleep;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.Event.CREATE;
import static l.files.fs.Event.DELETE;
import static l.files.fs.Event.MODIFY;
import static l.files.fs.Instant.EPOCH;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.local.Inotify.registerTracker;
import static l.files.fs.local.LocalFileObserveTest.Recorder.observe;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * @see File#observe(LinkOption, Observer)
 */
public final class LocalFileObserveTest extends FileBaseTest {

    // TODO rename methods to make consistent

    public void test_release_watch_when_directory_moves_out()
            throws Exception {

        File src = dir1().resolve("src").createDir();
        File dst = dir2().resolve("dst");

        try (Tracker tracker = registerMockTracker();
             Recorder observer = observe(dir1(), NOFOLLOW)) {

            observer.awaitModify(src, newCreateFile(src.resolve("b")));
            observer.awaitMove(src, dst);
            observer.awaitNoEvent(newCreateFile(dst.resolve("c")));

            ArgumentCaptor<Integer> fd = ArgumentCaptor.forClass(Integer.class);
            ArgumentCaptor<Integer> wd = ArgumentCaptor.forClass(Integer.class);
            verify(tracker).onWatchAdded(fd.capture(), eq(src.path()), anyInt(), wd.capture());
            verify(tracker).onWatchRemoved(fd.getValue(), wd.getValue());
        }
    }

    public void test_notifies_observer_on_max_user_instances_reached() throws Exception {
        int maxUserInstances = maxUserInstances();
        List<Observation> observations = new ArrayList<>(maxUserInstances);
        try {

            for (int i = 1; i < maxUserInstances + 10; i++) {
                File child = dir1().resolve(String.valueOf(i)).createFile();
                Observation observation = child.observe(NOFOLLOW, mock(Observer.class));
                observations.add(observation);
                if (i <= maxUserInstances) {
                    assertFalse("Failed at " + i, observation.isClosed());
                } else {
                    assertTrue("Failed at " + i, observation.isClosed());
                }
            }

        } finally {
            for (Observation observation : observations) {
                try {
                    observation.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public void test_notifies_observer_on_max_user_watches_reached_on_observe()
            throws Exception {

        File dir = linkToMaxUserWatchesTestDir();
        int expectedCount = maxUserWatches() + 10;
        ensureExactNumberOfChildDirs(dir, expectedCount);

        FileConsumer consumer = mock(FileConsumer.class);
        Observer observer = mock(Observer.class);

        try (Observation observation = dir.observe(FOLLOW, observer, consumer)) {
            assertTrue(observation.isClosed());
            verify(consumer, times(expectedCount)).accept(notNull(File.class));
        }
    }

    public void test_notifies_observer_on_max_user_watches_reached_during_observe()
            throws Exception {

        File dir = linkToMaxUserWatchesTestDir();
        int expectedCount = maxUserWatches() - 10;
        ensureExactNumberOfChildDirs(dir, expectedCount);

        FileConsumer consumer = mock(FileConsumer.class);
        Observer observer = mock(Observer.class);

        try (Observation observation = dir.observe(FOLLOW, observer, consumer)) {
            assertFalse(observation.isClosed());
            for (int i = 0; i < 20; i++) {
                dir.resolve(String.valueOf(Math.random())).createDir();
            }
            verify(observer, timeout(1000)).onCancel();
            verify(consumer, times(expectedCount)).accept(notNull(File.class));
        }
    }

    private File linkToMaxUserWatchesTestDir() throws IOException {
        return linkToExternalDir("files-test-max-user-watches-exceeded");
    }

    private void ensureExactNumberOfChildDirs(File dir, int expectedCount) throws IOException {
        int actualCount = 0;
        try (Stream<File> children = dir.listDirs(FOLLOW)) {
            for (File child : children) {
                actualCount++;
                if (actualCount > expectedCount) {
                    child.deleteRecursive();
                    actualCount--;
                }
            }
        }
        while (actualCount < expectedCount) {
            dir.resolve(String.valueOf(Math.random())).createDir();
            actualCount++;
        }
    }


    private File linkToExternalDir(String name) throws IOException {
        return dir1().resolve(name).createLink(
                externalStorageDir()
                        .resolve(name)
                        .createDirs()
        );
    }

    private File externalStorageDir() {
        return LocalFile.of(getExternalStorageDirectory());
    }

    public void test_releases_all_watches_on_close() throws Exception {
        int maxUserWatches = maxUserWatches();
        int count = maxUserWatches / 5;
        for (int i = 0; i < count; i++) {
            dir1().resolve(String.valueOf(i)).createDir();
        }
        for (int i = 0; i < 10; i++) {
            dir1().observe(FOLLOW, mock(Observer.class)).close();
        }
    }

    public void test_releases_fd_on_close() throws Exception {
        ArgumentCaptor<Integer> fd = ArgumentCaptor.forClass(Integer.class);
        try (Tracker tracker = registerMockTracker()) {
            dir1().observe(NOFOLLOW, mock(Observer.class)).close();
            verify(tracker).onInit(fd.capture());
            verify(tracker).onClose(fd.getValue());
        }
    }

    private int maxUserInstances() throws IOException {
        File limitFile = LocalFile.of("/proc/sys/fs/inotify/max_user_instances");
        return parseInt(limitFile.readAllUtf8().trim());
    }

    private int maxUserWatches() throws IOException {
        File limitFile = LocalFile.of("/proc/sys/fs/inotify/max_user_watches");
        return parseInt(limitFile.readAllUtf8().trim());
    }

    public void test_observe_on_link_no_follow() throws Exception {
        File dir = dir1().resolve("dir").createDir();
        File link = dir1().resolve("link").createLink(dir);
        File file = link.resolve("file");
        try (Recorder observer = observe(link, NOFOLLOW)) {
            // No follow, can observe the link
            observer.awaitModify(
                    link, newSetModificationTime(link, NOFOLLOW, Instant.of(1, 1)));
            // But not the child content
            try {
                observer.awaitCreateFile(file);
            } catch (AssertionError e) {
                assertTrue(observer.actual.isEmpty());
                return;
            }
            fail();
        }
    }

    public void test_observe_on_link_follow() throws Exception {
        File dir = dir1().resolve("dir").createDir();
        File link = dir1().resolve("link").createLink(dir);
        File child = link.resolve("dir").createDir();
        try (Recorder observer = observe(link, FOLLOW)) {
            observer.awaitModify(child, newCreateFile(child.resolve("a")));
        }
    }

    public void test_move_unreadable_directory_in_then_create_file_in_it()
            throws Exception {

        testMoveUnreadableDirectory(
                new PostMoves().createFile("c")
        );
    }

    public void test_move_unreadable_directory_in_then_create_link_in_it()
            throws Exception {

        testMoveUnreadableDirectory(
                new PostMoves().createLink("c", dir1())
        );
    }

    public void test_move_unreadable_directory_in_then_create_directory_in_it()
            throws Exception {

        testMoveUnreadableDirectory(
                new PostMoves().createDirectory("c")
        );
    }

    public void test_move_unreadable_directory_in_then_delete_file_from_it()
            throws Exception {

        testMoveUnreadableDirectory(
                new PreMoves().createFile("b"),
                new PostMoves().delete("b")
        );
    }

    public void test_move_unreadable_directory_in_then_delete_link_from_it()
            throws Exception {

        testMoveUnreadableDirectory(
                new PreMoves().createLink("b", dir1()),
                new PostMoves().delete("b")
        );
    }

    public void test_move_unreadable_directory_in_then_delete_directory_from_it()
            throws Exception {

        testMoveUnreadableDirectory(
                new PreMoves().createDirectory("b"),
                new PostMoves().delete("b")
        );
    }

    public void test_move_unreadable_directory_in_then_change_its_atime()
            throws Exception {

        testMoveUnreadableDirectory(
                new PostMoves().setLastAccessedTime(EPOCH)
        );
    }

    public void test_move_unreadable_directory_in_then_change_its_mtime()
            throws Exception {

        testMoveUnreadableDirectory(
                new PostMoves().setLastModifiedTime(EPOCH)
        );
    }

    public void test_move_unreadable_directory_in_then_change_its_permission()
            throws Exception {

        testMoveUnreadableDirectory(
                new PostMoves().removeAllPermissions()
        );
    }

    public void test_move_directory_in_then_change_its_permission()
            throws Exception {

        testMoveDirectory(
                new PostMoves().removeAllPermissions()
        );
    }

    public void test_move_directory_in_then_add_file_into_it() throws Exception {
        File dst = dir1().resolve("a");
        File src = dir2().resolve("a").createDir();
        try (Recorder observer = observe(dir1())) {
            observer.awaitMove(src, dst);
            observer.awaitModify(dst, newCreateDirectory(dst.resolve("b")));
        }
    }

    public void test_move_directory_in_then_delete_file_from_it() throws Exception {
        File dstDir = dir1().resolve("a");
        File srcDir = dir2().resolve("a").createDir();
        srcDir.resolve("b").createFile();
        try (Recorder observer = observe(dir1())) {
            observer.awaitMove(srcDir, dstDir);
            observer.awaitModify(dstDir, newDelete(dstDir.resolve("b")));
        }
    }

    public void test_move_directory_in_then_move_file_into_it() throws Exception {
        File dir = dir1().resolve("a");
        File src1 = dir2().resolve("a").createDir();
        File src2 = dir2().resolve("b").createFile();
        try (Recorder observer = observe(dir1())) {
            observer.awaitMove(src1, dir);
            observer.awaitModify(dir, newMove(src2, dir.resolve("b")));
        }
    }

    public void test_move_directory_in_then_move_file_out_of_it() throws Exception {
        File src = dir2().resolve("a").createDir();
        File dir = dir1().resolve("a");
        File child = dir.resolve("b");
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

    public void test_move_file_in() throws Exception {
        File src = dir2().resolve("a").createFile();
        File dst = dir1().resolve("b");
        try (Recorder observer = observe(dir1())) {
            observer.awaitMove(src, dst);
        }
    }

    public void test_move_file_out() throws Exception {
        File file = dir1().resolve("a").createFile();
        try (Recorder observer = observe(dir1())) {
            observer.awaitMove(file, dir2().resolve("a"));
        }
    }

    public void test_move_self_out() throws Exception {
        File file = dir1().resolve("file").createFile();
        File dir = dir1().resolve("dir").createDir();
        testMoveSelfOut(file, dir2().resolve("a"));
        testMoveSelfOut(dir, dir2().resolve("b"));
    }

    private static void testMoveSelfOut(
            File src,
            File dst) throws Exception {

        try (Recorder observer = observe(src)) {
            observer.awaitMove(src, dst);
        }
    }

    public void test_modify_file_content() throws Exception {
        File file = dir1().resolve("a").createFile();
        testModifyFileContent(file, file);
        testModifyFileContent(file, dir1());
    }

    private static void testModifyFileContent(
            File file,
            File observable) throws Exception {

        try (Recorder observer = observe(observable)) {
            observer.awaitModify(file, newAppend(file, "abc"));
        }
    }

    public void test_modify_permissions() throws Exception {
        File file = dir1().resolve("file").createFile();
        File dir = dir1().resolve("directory").createDir();
        testModifyPermission(file, file);
        testModifyPermission(file, dir1());
        testModifyPermission(dir, dir);
        testModifyPermission(dir, dir1());
    }

    private static void testModifyPermission(
            File target,
            File observable) throws Exception {

        Set<Permission> oldPerms = target.stat(NOFOLLOW).permissions();
        Set<Permission> newPerms = EnumSet.copyOf(oldPerms);
        if (newPerms.equals(oldPerms)) {
            newPerms.addAll(Permission.write());
        }
        if (newPerms.equals(oldPerms)) {
            newPerms.removeAll(Permission.write());
        }
        try (Recorder observer = observe(observable)) {
            observer.awaitModify(target, newSetPermissions(target, newPerms));
        }
    }

    /*
     * Note: IN_MODIFY is fired instead of the expected IN_ATTRIB when changing
     * the last modified time, and when changing access time both are not fired
     * making it not untrackable.
     */
    public void test_modify_modification_time() throws Exception {
        File file = dir1().resolve("file").createFile();
        File dir = dir1().resolve("dir").createDir();
        testModifyModificationTime(file, file);
        testModifyModificationTime(file, dir1());
        testModifyModificationTime(dir, dir);
        testModifyModificationTime(dir, dir1());
    }

    private void testModifyModificationTime(
            File target,
            File observable) throws Exception {

        Instant old = target.stat(NOFOLLOW).lastModifiedTime();
        Instant t = Instant.of(old.seconds() - 1, old.nanos());
        try (Recorder observer = observe(observable)) {
            observer.awaitModify(target, newSetModificationTime(target, NOFOLLOW, t));
        }
    }

    public void test_delete() throws Exception {
        File file = dir1().resolve("file");
        File dir = dir1().resolve("dir");
        testDelete(file.createFile(), file);
        testDelete(file.createFile(), dir1());
        testDelete(dir.createDir(), dir);
        testDelete(dir.createDir(), dir1());
    }

    private static void testDelete(
            File target,
            File observable) throws Exception {
        boolean file = target.stat(NOFOLLOW).isRegularFile();
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

    public void test_delete_recreate_directory_will_be_observed() throws Exception {
        File dir = dir1().resolve("dir");
        File file = dir.resolve("file");
        try (Recorder observer = observe(dir1())) {
            for (int i = 0; i < 10; i++) {
                observer.awaitCreateDirectory(dir);
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

    public void test_create() throws Exception {
        File file = dir1().resolve("file");
        File dir = dir1().resolve("dir");
        File link = dir1().resolve("link");
        testCreateFile(file, dir1());
        testCreateDirectory(dir, dir1());
        testCreateSymbolicLink(link, dir1(), dir1());
    }

    private static void testCreateFile(
            File target,
            File observable) throws Exception {

        try (Recorder observer = observe(observable)) {
            observer.awaitCreateFile(target);
        }
    }

    private static void testCreateDirectory(
            File target,
            File observable) throws Exception {

        try (Recorder observer = observe(observable)) {
            observer.awaitCreateDirectory(target);
        }
    }

    private static void testCreateSymbolicLink(
            File link,
            File target,
            File observable) throws Exception {

        try (Recorder observer = observe(observable)) {
            observer.awaitCreateLink(link, target);
        }
    }

    public void test_create_directory_then_create_items_into_it() throws Exception {
        File dir = dir1().resolve("dir");
        try (Recorder observer = observe(dir1())) {
            observer.awaitCreateDirectory(dir);
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

    public void test_create_directory_then_delete_items_from_it() throws Exception {
        File parent = dir1().resolve("parent");
        File file = parent.resolve("file");
        File dir = parent.resolve("dir");
        try (Recorder observer = observe(dir1())) {
            observer.awaitCreateDirectory(parent);
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

    public void test_create_directory_then_move_items_out_of_it() throws Exception {
        File parent = dir1().resolve("parent");
        File file = parent.resolve("file");
        File dir = parent.resolve("dir");
        try (Recorder observer = observe(dir1())) {
            observer.awaitCreateDirectory(parent);
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

    public void test_create_directory_then_move_file_into_it() throws Exception {
        File parent = dir1().resolve("parent");
        File file = dir2().resolve("file").createFile();
        File dir = dir2().resolve("dir").createDir();
        try (Recorder observer = observe(dir1())) {
            observer.awaitCreateDirectory(parent);
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

    public void test_multiple_operations() throws Exception {
        File a = dir1().resolve("a");
        File b = dir1().resolve("b");
        File c = dir1().resolve("c");
        File d = dir1().resolve("d");
        try (Recorder observer = observe(dir1())) {
            observer.awaitCreateDirectory(a);
            observer.awaitCreateDirectory(b);
            observer.awaitModify(a, newCreateFile(a.resolve("1")));
            observer.awaitMove(dir2().resolve("c").createFile(), c);
            observer.awaitMove(c, dir2().resolve("2"));
            observer.awaitDelete(b);
            observer.awaitCreateFile(d);
        }
    }

    private static WatchEvent event(Event kind, File file) {
        return WatchEvent.create(kind, file);
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

    private static Callable<Void> newMove(final File src, final File dst) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                src.moveTo(dst);
                return null;
            }
        };
    }

    private static Callable<Void> newDelete(final File file) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                file.delete();
                return null;
            }
        };
    }

    private static Callable<Void> newCreateFile(final File file) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                file.createFile();
                return null;
            }
        };
    }

    private static Callable<Void> newCreateDirectory(final File directory) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                directory.createDir();
                return null;
            }
        };
    }

    private static Callable<Void> newCreateSymbolicLink(
            final File link,
            final File target) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                link.createLink(target);
                return null;
            }
        };
    }

    private static Callable<Void> newAppend(
            final File file,
            final CharSequence content) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                file.writeAllUtf8(content);
                return null;
            }
        };
    }

    private static Callable<Void> newSetPermissions(
            final File file,
            final Set<Permission> permissions) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                file.setPermissions(permissions);
                return null;
            }
        };
    }

    private static Callable<Void> newSetModificationTime(
            final File file,
            final LinkOption option,
            final Instant instant) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                file.setLastModifiedTime(option, instant);
                return null;
            }
        };
    }

    private static Callable<Void> newSetLastAccessedTime(
            final File file,
            final LinkOption option,
            final Instant instant) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                file.setLastAccessedTime(option, instant);
                return null;
            }
        };
    }

    static class Recorder extends Tracker implements Observer {

        private final File root;

        private Observation observation;
        private int fd;
        private int wd;
        private final Map<File, Integer> allChildWds = new ConcurrentHashMap<>();
        private final Set<Integer> validChildWds = new CopyOnWriteArraySet<>();

        private final List<WatchEvent> expected = new CopyOnWriteArrayList<>();
        private final List<WatchEvent> actual = new CopyOnWriteArrayList<>();
        private volatile CountDownLatch success;

        Recorder(File root) {
            this.root = root;
        }

        static Recorder observe(File observable) throws IOException, InterruptedException {
            return observe(observable, NOFOLLOW);
        }

        static Recorder observe(File file, LinkOption option)
                throws IOException, InterruptedException {

            try (Tracker tracker = registerMockTracker()) {
                Recorder observer = new Recorder(file);
                observer.observation = file.observe(option, observer);
                assertFalse(observer.observation.isClosed());
                verifyTracker(observer, tracker, file, option);
                registerTracker(observer);
                return observer;
            }
        }

        private static void verifyTracker(
                Recorder observer,
                Tracker tracker,
                File file,
                LinkOption option) throws IOException {

            ArgumentCaptor<Integer> fd = ArgumentCaptor.forClass(Integer.class);
            ArgumentCaptor<Integer> wd = ArgumentCaptor.forClass(Integer.class);
            InOrder order = inOrder(tracker);
            order.verify(tracker).onInit(fd.capture());
            order.verify(tracker).onWatchAdded(eq(fd.getValue()), eq(file.path()), anyInt(), wd.capture());

            observer.fd = fd.getValue();
            observer.wd = wd.getValue();

            if (file.stat(option).isDirectory()) {
                try (Stream<File> dirs = file.listDirs(option)) {
                    for (File dir : dirs) {
                        order.verify(tracker).onWatchAdded(eq(fd.getValue()), eq(dir.path()), anyInt(), wd.capture());
                        observer.allChildWds.put(dir, wd.getValue());
                        observer.validChildWds.add(wd.getValue());
                    }
                }
            }

            order.verifyNoMoreInteractions();

        }

        @Override
        public void onWatchAdded(int fd, String path, int mask, int wd) {
            super.onWatchAdded(fd, path, mask, wd);
            if (this.fd == fd) {
                this.allChildWds.put(LocalFile.of(path), wd);
                this.validChildWds.add(wd);
            }
        }

        @Override
        public void onWatchRemoved(int fd, int wd) {
            super.onWatchRemoved(fd, wd);
            if (this.fd == fd) {
                validChildWds.remove(wd);
            }
        }

        @Override
        public void close() throws IOException {
            super.close();
            observation.close();
        }

        @Override
        public void onEvent(Event event, String child) {
            File target = child == null ? root : root.resolve(child);
            actual.add(WatchEvent.create(event, target));
            if (expected.equals(actual)) {
                success.countDown();
            }
        }

        @Override
        public void onCancel() {
            // fail(); TODO handle observing on dir add/remove no permission etc
        }

        void await(Event kind, File file, Callable<?> action) throws Exception {
            await(WatchEvent.create(kind, file), action);
        }

        void await(WatchEvent expected, Callable<?> action) throws Exception {
            await(singletonList(expected), action);
        }

        void await(List<WatchEvent> expected, Callable<?> action) throws Exception {
            this.actual.clear();
            this.expected.clear();
            this.expected.addAll(expected);
            this.success = new CountDownLatch(1);
            action.call();
            if (!success.await(1, SECONDS)) {
                fail("\nexpected: " + this.expected
                        + "\nactual:   " + this.actual);
            }
        }

        void awaitNoEvent(Callable<?> action) throws Exception {
            actual.clear();
            expected.clear();
            success = new CountDownLatch(0);
            action.call();
            sleep(SECONDS.toMillis(1));
            if (!actual.isEmpty()) {
                fail("expected no event but got " + actual);
            }
        }

        void awaitCreateFile(File target) throws Exception {
            try (Tracker tracker = registerMockTracker()) {
                await(CREATE, target, newCreateFile(target));
                verifyZeroInteractions(tracker);
            }
        }

        void awaitCreateDirectory(File target) throws Exception {
            try (Tracker tracker = registerMockTracker()) {
                await(CREATE, target, newCreateDirectory(target));
                verify(tracker).onWatchAdded(eq(fd), eq(target.path()), anyInt(), anyInt());
            }
        }

        void awaitCreateLink(File link, File target) throws Exception {
            try (Tracker tracker = registerMockTracker()) {
                await(CREATE, link, newCreateSymbolicLink(link, target));
                verifyZeroInteractions(tracker);
            }
        }

        void awaitDelete(File target) throws Exception {
            try (Tracker tracker = registerMockTracker()) {
                await(DELETE, target, newDelete(target));
                verifyZeroInteractions(tracker);
            }
        }

        void awaitMove(File src, File dst) throws Exception {
            try (Tracker tracker = registerMockTracker()) {

                boolean rootIsSrcParent = root.equals(src.parent());
                boolean rootIsDstParent = root.equals(dst.parent());

                if (rootIsSrcParent && rootIsDstParent) {
                    fail("Not implemented"); // TODO

                } else if (rootIsSrcParent) {
                    awaitMoveFrom(tracker, src, dst);

                } else if (rootIsDstParent) {
                    awaitMoveTo(tracker, src, dst);

                } else if (root.equals(src)) {
                    awaitMoveSelf(tracker, dst);

                } else {
                    fail();
                }
            }
        }

        private void awaitMoveFrom(Tracker tracker, File src, File dst) throws Exception {
            boolean srcIsDir = src.stat(NOFOLLOW).isDirectory();
            await(DELETE, src, newMove(src, dst));
            if (srcIsDir) {
                verify(tracker).onWatchRemoved(fd, allChildWds.get(src));
            }
            verifyNoMoreInteractions(tracker);
        }

        private void awaitMoveTo(Tracker tracker, File src, File dst) throws Exception {
            boolean srcIsDir = src.stat(NOFOLLOW).isDirectory();
            await(CREATE, dst, newMove(src, dst));
            if (srcIsDir) {
                verify(tracker).onWatchAdded(eq(fd), eq(dst.path()), anyInt(), anyInt());
            }
            verifyNoMoreInteractions(tracker);
        }

        private void awaitMoveSelf(Tracker tracker, File dst) throws Exception {
            await(DELETE, root, newMove(root, dst));
            for (int wd : validChildWds) {
                verify(tracker).onWatchRemoved(fd, wd);
            }
            verify(tracker).onWatchRemoved(fd, wd);
            verify(tracker).onClose(fd);
            verifyNoMoreInteractions(tracker);
        }

        void awaitModify(File target, Callable<Void> action) throws Exception {
            try (Tracker tracker = registerMockTracker()) {
                await(MODIFY, target, action);
                verifyZeroInteractions(tracker);
            }
        }

    }

    private static Tracker registerMockTracker() {
        return registerTracker(mock(Tracker.class));
    }

    @AutoValue
    static abstract class WatchEvent {
        abstract Event kind();

        abstract File resource();

        static WatchEvent create(Event kind, File file) {
            return new AutoValue_LocalFileObserveTest_WatchEvent(
                    kind, file
            );
        }
    }

    private interface PreMove {
        void onPreMove(File src) throws Exception;
    }

    private interface PostMove {
        void onPostMove(File dst, Recorder observer) throws Exception;
    }

    private static final class PreMoves implements PreMove {

        private final List<PreMove> moves = new ArrayList<>();

        @Override
        public void onPreMove(File src) throws Exception {
            for (PreMove move : moves) {
                move.onPreMove(src);
            }
        }

        PreMoves add(PreMove move) {
            moves.add(move);
            return this;
        }

        PreMoves setNoRead() {
            return add(new PreMove() {
                @Override
                public void onPreMove(File src) throws Exception {
                    src.removePermissions(Permission.read());
                }
            });
        }

        PreMoves createFile(final String name) {
            return add(new PreMove() {
                @Override
                public void onPreMove(File src) throws Exception {
                    src.resolve(name).createFile();
                }
            });
        }

        PreMoves createDirectory(final String name) {
            return add(new PreMove() {
                @Override
                public void onPreMove(File src) throws Exception {
                    src.resolve(name).createDir();
                }
            });
        }

        PreMoves createLink(final String name, final File target) {
            return add(new PreMove() {
                @Override
                public void onPreMove(File src) throws Exception {
                    src.resolve(name).createLink(target);
                }
            });
        }
    }

    private static final class PostMoves implements PostMove {

        private final List<PostMove> moves = new ArrayList<>();

        @Override
        public void onPostMove(File dst, Recorder observer) throws Exception {
            for (PostMove move : moves) {
                move.onPostMove(dst, observer);
            }
        }

        PostMoves add(PostMove move) {
            moves.add(move);
            return this;
        }

        PostMoves createFile(final String name) {
            return add(new PostMove() {
                @Override
                public void onPostMove(File dst, Recorder observer) throws Exception {
                    observer.await(MODIFY, dst, newCreateFile(dst.resolve(name)));
                }
            });
        }

        PostMoves setLastModifiedTime(final Instant instant) {
            return add(new PostMove() {
                @Override
                public void onPostMove(File dst, Recorder observer) throws Exception {
                    observer.await(MODIFY, dst, newSetModificationTime(dst, NOFOLLOW, instant));
                }
            });
        }

        PostMoves delete(final String name) {
            return add(new PostMove() {
                @Override
                public void onPostMove(File dst, Recorder observer) throws Exception {
                    observer.await(MODIFY, dst, newDelete(dst.resolve(name)));
                }
            });
        }

        PostMoves removeAllPermissions() {
            return add(new PostMove() {
                @Override
                public void onPostMove(File dst, Recorder observer) throws Exception {
                    observer.await(MODIFY, dst, newSetPermissions(dst, Permission.none()));
                }
            });
        }

        PostMoves setLastAccessedTime(final Instant instant) {
            return add(new PostMove() {
                @Override
                public void onPostMove(File dst, Recorder observer) throws Exception {
                    observer.await(MODIFY, dst, newSetLastAccessedTime(dst, NOFOLLOW, instant));
                }
            });
        }

        PostMoves createDirectory(final String name) {
            return add(new PostMove() {
                @Override
                public void onPostMove(File dst, Recorder observer) throws Exception {
                    observer.await(MODIFY, dst, newCreateDirectory(dst.resolve(name)));
                }
            });
        }

        PostMoves createLink(final String name, final File target) {
            return add(new PostMove() {
                @Override
                public void onPostMove(File dst, Recorder observer) throws Exception {
                    observer.await(MODIFY, dst, newCreateSymbolicLink(dst.resolve(name), target));
                }
            });
        }
    }

    private void testMoveUnreadableDirectory(PostMove post) throws Exception {
        testMoveUnreadableDirectory(new PreMoves(), post);
    }

    private void testMoveUnreadableDirectory(PreMove pre, PostMove post) throws Exception {
        testMoveDirectory(new PreMoves().add(pre).setNoRead(), post);
    }

    private void testMoveDirectory(PostMove post) throws Exception {
        testMoveDirectory(new PreMoves(), post);
    }

    private void testMoveDirectory(PreMove pre, PostMove post) throws Exception {
        File dst = dir1().resolve("a");
        File src = dir2().resolve("a").createDir();
        pre.onPreMove(src);
        try (Recorder observer = observe(dir1())) {
            observer.await(CREATE, dst, newMove(src, dst));
            post.onPostMove(dst, observer);
        }
    }

}
