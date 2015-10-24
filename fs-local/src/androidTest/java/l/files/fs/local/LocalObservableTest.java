package l.files.fs.local;

import com.google.auto.value.AutoValue;

import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

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
import static l.files.fs.Permission.OWNER_EXECUTE;
import static l.files.fs.Permission.OWNER_WRITE;
import static l.files.fs.local.LocalObservableTest.Recorder.observe;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * @see File#observe(LinkOption, Observer)
 */
public final class LocalObservableTest extends FileBaseTest {

    public void test_no_observe_on_procfs() throws Exception {

        try (Tracker tracker = registerMockTracker();
             Recorder observer = observe(LocalFile.of("/proc/self"), FOLLOW, false)) {

            assertTrue(observer.isClosed());
            verifyZeroInteractions(tracker);
        }
    }

    public void test_observe_on_regular_file() throws Exception {
        File file = dir1().resolve("file").createFile();
        try (Recorder observer = observe(file, NOFOLLOW)) {
            observer.awaitModifyByAppend(file, "hello");
        }
    }

    public void test_observe_on_link() throws Exception {
        File file = dir1().resolve("link").createLink(dir2());
        try (Recorder observer = observe(file, NOFOLLOW)) {
            observer.awaitModifyBySetLastModifiedTime(file, EPOCH);
        }
    }

    public void test_release_watch_when_dir_moves_out() throws Exception {

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
                Observer observer = mock(Observer.class);
                Observation observation = child.observe(NOFOLLOW, observer);
                observations.add(observation);
                if (i <= maxUserInstances) {
                    assertFalse("Failed at " + i, observation.isClosed());
                    verify(observer, never()).onIncompleteObservation();
                } else {
                    assertTrue("Failed at " + i, observation.isClosed());
                    verify(observer).onIncompleteObservation();
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

    public void test_notifies_observer_on_max_user_watches_reached_on_observe() throws Exception {

        File dir = linkToMaxUserWatchesTestDir();
        int expectedCount = maxUserWatches() + 10;
        ensureExactNumberOfChildDirs(dir, expectedCount);

        FileConsumer consumer = mock(FileConsumer.class);
        Observer observer = mock(Observer.class);

        try (Observation ignored = dir.observe(FOLLOW, observer, consumer)) {
            verify(observer, atLeastOnce()).onIncompleteObservation();
            verify(consumer, times(expectedCount)).accept(notNull(File.class));
        }
    }

    public void test_notifies_observer_on_max_user_watches_reached_during_observe() throws Exception {

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
            verify(observer, timeout(1000).atLeastOnce()).onIncompleteObservation();
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

        File a = dir1().resolve("a").createDir();
        File b = dir1().resolve("b").createDir();
        try (Tracker tracker = registerMockTracker()) {
            dir1().observe(NOFOLLOW, mock(Observer.class)).close();

            ArgumentCaptor<Integer> fd = ArgumentCaptor.forClass(Integer.class);
            ArgumentCaptor<Integer> wd = ArgumentCaptor.forClass(Integer.class);
            verify(tracker).onInit(fd.capture());
            verify(tracker).onWatchAdded(eq(fd.getValue()), eq(a.path()), anyInt(), wd.capture());
            verify(tracker).onWatchAdded(eq(fd.getValue()), eq(b.path()), anyInt(), wd.capture());
            verify(tracker).onWatchRemoved(fd.getValue(), wd.getAllValues().get(0));
            verify(tracker).onWatchRemoved(fd.getValue(), wd.getAllValues().get(1));
            verify(tracker).onClose(fd.getValue());
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
            observer.awaitModifyBySetLastModifiedTime(link, Instant.of(1, 1));
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
            observer.awaitModifyByCreateFile(child, "a");
        }
    }

    public void test_move_unreadable_dir_in_will_notify_incomplete_observation() throws Exception {
        testMoveDirIn(
                new PreActions().removeReadPermissions(),
                new PostActions()
                        .awaitOnIncompleteObservation()
                        .awaitCreateFileInParent("parent watch still works")
        );
    }

    public void test_move_dir_in_then_change_its_permission() throws Exception {
        testMoveDirIn(new PostActions().awaitRemoveAllPermissions());
    }

    public void test_rename_dir() throws Exception {
        File src = dir1().resolve("a").createDir();
        File dst = dir1().resolve("b");
        try (Recorder observer = observe(dir1())) {
            observer.awaitModifyByCreateFile(src, "1");
            observer.awaitMove(src, dst);
            observer.awaitModifyByCreateFile(dst, "2");
        }
    }

    public void test_move_dir_in_then_add_file_into_it() throws Exception {
        testMoveDirIn(new PostActions().awaitCreateDir("hello"));
    }

    public void test_move_dir_in_then_delete_file_from_it() throws Exception {
        testMoveDirIn(
                new PreActions().createFile("hello"),
                new PostActions().awaitDelete("hello")
        );
    }

    public void test_move_dir_in_then_move_file_into_it() throws Exception {
        File extra = dir2().resolve("hello").createFile();
        testMoveDirIn(new PostActions().awaitMoveIn(extra));
    }

    public void test_move_dir_in_then_move_file_out_of_it() throws Exception {
        File src = dir2().resolve("a").createDir();
        File dir = dir1().resolve("a");
        File child = dir.resolve("b");
        try (Recorder observer = observe(dir1())) {
            observer.awaitMove(src, dir);
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

    private static void testMoveSelfOut(File src, File dst) throws Exception {
        try (Recorder observer = observe(src)) {
            observer.awaitMove(src, dst);
        }
    }

    public void test_modify_file_content() throws Exception {
        File file = dir1().resolve("a").createFile();
        testModifyFileContent(file, file);
        testModifyFileContent(file, dir1());
    }

    private static void testModifyFileContent(File file, File observable) throws Exception {
        try (Recorder observer = observe(observable)) {
            observer.awaitModifyByAppend(file, "abc");
        }
    }

    public void test_modify_permissions() throws Exception {
        File file = dir1().resolve("file").createFile();
        File dir = dir1().resolve("dir").createDir();
        testModifyPermission(file, file);
        testModifyPermission(file, dir1());
        testModifyPermission(dir, dir);
        testModifyPermission(dir, dir1());
    }

    private static void testModifyPermission(File target, File observable) throws Exception {
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

    public void test_modify_mtime() throws Exception {
        File file = dir1().resolve("file").createFile();
        File dir = dir1().resolve("dir").createDir();
        testModifyLastModifiedTime(file, file);
        testModifyLastModifiedTime(file, dir1());
        testModifyLastModifiedTime(dir, dir);
        testModifyLastModifiedTime(dir, dir1());
    }

    private void testModifyLastModifiedTime(File target, File observable) throws Exception {
        Instant old = target.stat(NOFOLLOW).lastModifiedTime();
        Instant t = Instant.of(old.seconds() - 1, old.nanos());
        try (Recorder observer = observe(observable)) {
            observer.awaitModifyBySetLastModifiedTime(target, t);
        }
    }

    public void test_modify_atime() throws Exception {
        File file = dir1().resolve("file").createFile();
        File dir = dir1().resolve("dir").createDir();
        testModifyLastAccessedTime(file, file);
        testModifyLastAccessedTime(file, dir1());
        testModifyLastAccessedTime(dir, dir);
        testModifyLastAccessedTime(dir, dir1());
    }

    private void testModifyLastAccessedTime(File target, File observable) throws Exception {
        Instant old = target.stat(NOFOLLOW).lastModifiedTime();
        Instant t = Instant.of(old.seconds() - 1, old.nanos());
        try (Recorder observer = observe(observable)) {
            observer.awaitModifyBySetLastAccessedTime(target, t);
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

    private static void testDelete(File target, File observable) throws Exception {
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

    public void test_delete_recreate_dir_will_be_observed() throws Exception {
        File dir = dir1().resolve("dir");
        File file = dir.resolve("file");
        try (Recorder observer = observe(dir1())) {
            for (int i = 0; i < 10; i++) {
                observer.awaitCreateDir(dir);
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
        testCreateDir(dir, dir1());
        testCreateLink(link, dir1(), dir1());
    }

    private static void testCreateFile(File target, File observable) throws Exception {
        try (Recorder observer = observe(observable)) {
            observer.awaitCreateFile(target);
        }
    }

    private static void testCreateDir(File target, File observable) throws Exception {
        try (Recorder observer = observe(observable)) {
            observer.awaitCreateDir(target);
        }
    }

    private static void testCreateLink(
            File link,
            File target,
            File observable) throws Exception {

        try (Recorder observer = observe(observable)) {
            observer.awaitCreateLink(link, target);
        }
    }

    public void test_observe_unreadable_child_dir_will_notify_incomplete_observation() throws Exception {
        File dir = dir1().resolve("dir").createDir();
        dir.removePermissions(Permission.read());
        try (Recorder observer = observe(dir1())) {
            observer.awaitOnIncompleteObservation();
            observer.awaitCreateFile(dir1().resolve("parent watch still works"));
        }
    }

    public void test_create_dir_then_make_it_unreadable() throws Exception {
        File dir = dir1().resolve("dir");
        try (Recorder observer = observe(dir1())) {
            observer.awaitCreateDir(dir);
            observer.awaitModifyBySetPermissions(dir, EnumSet.of(OWNER_WRITE, OWNER_EXECUTE));
            observer.awaitModifyByCreateFile(dir, "a");
        }
    }

    public void test_create_dir_then_create_items_into_it() throws Exception {
        File dir = dir1().resolve("dir");
        try (Recorder observer = observe(dir1())) {
            observer.awaitCreateDir(dir);
            observer.await(
                    asList(
                            event(MODIFY, dir),
                            event(MODIFY, dir),
                            event(MODIFY, dir)
                    ),
                    compose(
                            newCreateFile(dir.resolve("file")),
                            newCreateDir(dir.resolve("dir2()")),
                            newCreateLink(dir.resolve("link"), dir1())
                    )
            );
        }
    }

    public void test_create_dir_then_delete_items_from_it() throws Exception {
        File parent = dir1().resolve("parent");
        File file = parent.resolve("file");
        File dir = parent.resolve("dir");
        try (Recorder observer = observe(dir1())) {
            observer.awaitCreateDir(parent);
            observer.await(
                    asList(
                            event(MODIFY, parent),
                            event(MODIFY, parent),
                            event(MODIFY, parent),
                            event(MODIFY, parent)
                    ),
                    compose(
                            newCreateFile(file),
                            newCreateDir(dir),
                            newDelete(file),
                            newDelete(dir)
                    )
            );
        }
    }

    public void test_create_dir_then_move_items_out_of_it() throws Exception {
        File parent = dir1().resolve("parent");
        File file = parent.resolve("file");
        File dir = parent.resolve("dir");
        try (Recorder observer = observe(dir1())) {
            observer.awaitCreateDir(parent);
            observer.await(
                    asList(
                            event(MODIFY, parent),
                            event(MODIFY, parent),
                            event(MODIFY, parent),
                            event(MODIFY, parent)
                    ),
                    compose(
                            newCreateFile(file),
                            newCreateDir(dir),
                            newMove(file, dir2().resolve("file")),
                            newMove(dir, dir2().resolve("dir"))
                    )
            );
        }
    }

    public void test_create_dir_then_move_file_into_it() throws Exception {
        File parent = dir1().resolve("parent");
        File file = dir2().resolve("file").createFile();
        File dir = dir2().resolve("dir").createDir();
        try (Recorder observer = observe(dir1())) {
            observer.awaitCreateDir(parent);
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
            observer.awaitCreateDir(a);
            observer.awaitCreateDir(b);
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

    private static Callable<Void> newCreateDir(final File dir) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                dir.createDir();
                return null;
            }
        };
    }

    private static Callable<Void> newCreateLink(
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

    private static Callable<Void> newSetLastModifiedTime(
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

        private final Observer observer = mock(Observer.class);

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

        static Recorder observe(File observable) throws Exception {
            return observe(observable, NOFOLLOW);
        }

        static Recorder observe(File file, LinkOption option) throws Exception {
            return observe(file, option, true);
        }

        static Recorder observe(File file, LinkOption option, boolean verifyTracker) throws Exception {
            try (Tracker tracker = registerMockTracker()) {
                Recorder observer = new Recorder(file);
                observer.observation = file.observe(option, observer);
                if (verifyTracker) {
                    assertFalse(observer.observation.isClosed());
                    verifyTracker(observer, tracker, file, option);
                }
                Inotify.get().registerTracker(observer);
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
                        if (dir.isReadable()) {
                            order.verify(tracker).onWatchAdded(
                                    eq(fd.getValue()), eq(dir.path()), anyInt(), wd.capture());
                        }
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

        boolean isClosed() {
            return observation.isClosed();
        }

        @Override
        public void onEvent(Event event, String child) {
            observer.onEvent(event, child);
            File target = child == null ? root : root.resolve(child);
            actual.add(WatchEvent.create(event, target));
            if (expected.equals(actual)) {
                success.countDown();
            }
        }

        @Override
        public void onIncompleteObservation() {
            observer.onIncompleteObservation();
        }

        void awaitOnIncompleteObservation() {
            verify(observer).onIncompleteObservation();
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

        void awaitCreateDir(File target) throws Exception {
            try (Tracker tracker = registerMockTracker()) {
                await(CREATE, target, newCreateDir(target));
                verify(tracker).onWatchAdded(eq(fd), eq(target.path()), anyInt(), anyInt());
            }
        }

        void awaitCreateLink(File link, File target) throws Exception {
            try (Tracker tracker = registerMockTracker()) {
                await(CREATE, link, newCreateLink(link, target));
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
                    awaitMoveWithinSameDir(tracker, src, dst);

                } else if (rootIsSrcParent) {
                    awaitMoveFrom(tracker, src, dst);

                } else if (rootIsDstParent) {
                    awaitMoveTo(tracker, src, dst);

                } else if (root.equals(src)) {
                    awaitMoveSelf(tracker, dst);

                } else if (root.equals(src.parent().parent())) {
                    awaitModify(src.parent(), newMove(src, dst));

                } else if (root.equals(dst.parent().parent())) {
                    awaitModify(dst.parent(), newMove(src, dst));

                } else {
                    fail();
                }
            }
        }

        private void awaitMoveWithinSameDir(Tracker tracker, File src, File dst) throws Exception {
            boolean isDir = src.stat(NOFOLLOW).isDirectory();
            await(
                    asList(
                            event(DELETE, src),
                            event(CREATE, dst)
                    ),
                    newMove(src, dst)
            );
            InOrder order = inOrder(tracker);
            if (isDir) {
                order.verify(tracker).onWatchRemoved(fd, allChildWds.get(src));
                order.verify(tracker).onWatchAdded(eq(fd), eq(dst.path()), anyInt(), anyInt());
            }
            order.verifyNoMoreInteractions();
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
            boolean readable = src.isReadable();
            await(CREATE, dst, newMove(src, dst));

            if (srcIsDir) {
                if (readable) {
                    verify(tracker).onWatchAdded(eq(fd), eq(dst.path()), anyInt(), anyInt());
                    verify(observer, never()).onIncompleteObservation();
                } else {
                    verify(tracker, never()).onWatchAdded(eq(fd), eq(dst.path()), anyInt(), anyInt());
                    verify(observer).onIncompleteObservation();
                }
            } else {
                verify(tracker, never()).onWatchAdded(eq(fd), eq(dst.path()), anyInt(), anyInt());
                verify(observer, never()).onIncompleteObservation();
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

        void awaitModifyByCreateFile(File target, String child) throws Exception {
            awaitModify(target, newCreateFile(target.resolve(child)));
        }

        void awaitModifyByCreateDir(File target, String child) throws Exception {
            awaitModify(target, newCreateDir(target.resolve(child)));
        }

        void awaitModifyBySetLastAccessedTime(File target, Instant time) throws Exception {
            awaitModify(target, newSetLastAccessedTime(target, NOFOLLOW, time));
        }

        void awaitModifyBySetPermissions(File target, Set<Permission> perms) throws Exception {
            awaitModify(target, newSetPermissions(target, perms));
        }

        void awaitModifyByDelete(File target, String child) throws Exception {
            awaitModify(target, newDelete(target.resolve(child)));
        }

        void awaitModifyBySetLastModifiedTime(File target, Instant time) throws Exception {
            awaitModify(target, newSetLastModifiedTime(target, NOFOLLOW, time));
        }

        void awaitModifyByAppend(File target, CharSequence content) throws Exception {
            awaitModify(target, newAppend(target, content));
        }
    }

    private static Tracker registerMockTracker() {
        final Tracker tracker = mock(Tracker.class);
        try {
            doAnswer(new Answer<Void>() {
                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    Inotify.get().unregisterTracker(tracker);
                    return null;
                }
            }).when(tracker).close();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        Inotify.get().registerTracker(tracker);
        return tracker;
    }

    public static abstract class Tracker implements Inotify.Tracker, AutoCloseable {

        @Override
        public void close() throws IOException {
        }

        @Override
        public void onClose(int fd) {
        }

        @Override
        public void onWatchRemoved(int fd, int wd) {
        }

        @Override
        public void onWatchAdded(int fd, String path, int mask, int wd) {
        }

        @Override
        public void onInit(int fd) {
        }

    }

    @AutoValue
    static abstract class WatchEvent {
        abstract Event kind();

        abstract File resource();

        static WatchEvent create(Event kind, File file) {
            return new AutoValue_LocalObservableTest_WatchEvent(
                    kind, file
            );
        }
    }

    private interface PreAction {
        void action(File src) throws Exception;
    }

    private interface PostAction {
        void action(File dst, Recorder observer) throws Exception;
    }

    private static final class PreActions implements PreAction {

        private final List<PreAction> moves = new ArrayList<>();

        @Override
        public void action(File src) throws Exception {
            for (PreAction move : moves) {
                move.action(src);
            }
        }

        PreActions add(PreAction move) {
            moves.add(move);
            return this;
        }

        PreActions removeReadPermissions() {
            return add(new PreAction() {
                @Override
                public void action(File src) throws Exception {
                    src.removePermissions(Permission.read());
                }
            });
        }

        PreActions createFile(final String name) {
            return add(new PreAction() {
                @Override
                public void action(File src) throws Exception {
                    src.resolve(name).createFile();
                }
            });
        }

    }

    private static final class PostActions implements PostAction {

        private final List<PostAction> moves = new ArrayList<>();

        @Override
        public void action(File dst, Recorder observer) throws Exception {
            for (PostAction move : moves) {
                move.action(dst, observer);
            }
        }

        PostActions add(PostAction move) {
            moves.add(move);
            return this;
        }

        PostActions awaitCreateFile(final String name) {
            return add(new PostAction() {
                @Override
                public void action(File dst, Recorder observer) throws Exception {
                    observer.awaitModifyByCreateFile(dst, name);
                }
            });
        }

        PostActions awaitCreateFileInParent(final String name) {
            return add(new PostAction() {
                @Override
                public void action(File dst, Recorder observer) throws Exception {
                    observer.awaitCreateFile(dst.parent().resolve(name));
                }
            });
        }

        PostActions awaitDelete(final String name) {
            return add(new PostAction() {
                @Override
                public void action(File dst, Recorder observer) throws Exception {
                    observer.awaitModifyByDelete(dst, name);
                }
            });
        }

        PostActions awaitRemoveAllPermissions() {
            return add(new PostAction() {
                @Override
                public void action(File dst, Recorder observer) throws Exception {
                    observer.awaitModifyBySetPermissions(dst, Permission.none());
                }
            });
        }

        PostActions awaitCreateDir(final String name) {
            return add(new PostAction() {
                @Override
                public void action(File dst, Recorder observer) throws Exception {
                    observer.awaitModifyByCreateDir(dst, name);
                }
            });
        }

        PostActions awaitMoveIn(final File src) {
            return add(new PostAction() {
                @Override
                public void action(File dst, Recorder observer) throws Exception {
                    observer.awaitMove(src, dst.resolve(src.name()));
                }
            });
        }

        PostActions awaitOnIncompleteObservation() {
            return add(new PostAction() {
                @Override
                public void action(File dst, Recorder observer) throws Exception {
                    observer.awaitOnIncompleteObservation();
                }
            });
        }

    }

    private void testMoveDirIn(PostAction post) throws Exception {
        testMoveDirIn(new PreActions(), post);
    }

    private void testMoveDirIn(PreAction pre, PostAction post) throws Exception {
        File dst = dir1().resolve("a");
        File src = dir2().resolve("a").createDir();
        pre.action(src);
        try (Recorder observer = observe(dir1())) {
            observer.awaitMove(src, dst);
            post.action(dst, observer);
        }
    }

}
