package l.files.fs;

import android.os.Build;
import l.files.fs.Path.Consumer;
import l.files.fs.event.Event;
import l.files.fs.event.Observation;
import l.files.fs.event.Observer;
import l.files.fs.exception.AlreadyExist;
import l.files.testing.fs.PathBaseTest;
import l.files.testing.fs.Paths;
import org.junit.Test;
import org.mockito.AdditionalMatchers;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;

import static java.lang.Integer.parseInt;
import static java.lang.Math.random;
import static java.lang.Thread.sleep;
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.base.Objects.requireNonNull;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.ObservableTest.Recorder.observe;
import static l.files.fs.event.Event.*;
import static l.files.testing.fs.Paths.removeReadPermissions;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public final class ObservableTest extends PathBaseTest {

    @Test
    public void able_to_continue_observing_existing_dirs_when_new_dir_added_is_not_observable()
        throws Exception {

        Path readableDir = dir1().concat("readable");
        Path unreadableDir = dir1().concat("unreadable");

        readableDir.createDirectory();
        try (Recorder recorder = observe(dir1(), FOLLOW)) {
            recorder.awaitModifyByCreateFile(readableDir, "aa");
            recorder.await(
                CREATE,
                unreadableDir,
                () -> unreadableDir.createDirectory(EnumSet.of(
                    OWNER_EXECUTE,
                    OWNER_WRITE
                ))
            );

            recorder.awaitNoEvent(() -> unreadableDir.concat("zz")
                .createFile());
            recorder.awaitModifyByCreateFile(readableDir, "ab");
        }
    }

    @Test
    public void able_to_observe_the_rest_of_the_files_when_some_are_not_observable()
        throws Exception {

        List<Path> observables = new ArrayList<>();
        observables.add(createRandomChildDir(dir1()));
        observables.add(createRandomChildDir(dir1()));

        Path unobservable = dir1().concat("unobservable").createDirectory();
        removeReadPermissions(unobservable.toJavaPath());

        observables.add(createRandomChildDir(dir1()));
        observables.add(createRandomChildDir(dir1()));

        try (Recorder recorder = observe(dir1(), FOLLOW)) {
            recorder.awaitCreateFile(dir1().concat("1"));
            for (Path observable : observables) {
                recorder.awaitModifyByCreateFile(observable, "1");
            }
            recorder.awaitNoEvent(() -> unobservable.concat("1").createFile());
        }
    }

    @Test
    public void no_observe_on_procfs() throws Exception {
        try (Tracker tracker = registerMockTracker();
             Recorder observer = observe(
                 Path.of("/proc/self"),
                 FOLLOW,
                 false
             )) {
            assertTrue(observer.isClosed());
            assertNotNull(observer.closeReason);
            assertEquals(
                "procfs not supported",
                observer.closeReason.getMessage()
            );
            verify(tracker).onClose(any(), any());
            verifyZeroInteractions(tracker);
        }
    }

    @Test
    public void observe_on_regular_file() throws Exception {
        Path file = dir1().concat("file").createFile();
        try (Recorder observer = observe(file, NOFOLLOW)) {
            observer.awaitModifyByAppend(file, "hello");
        }
    }

    @Test
    public void release_watch_when_dir_moves_out() throws Exception {

        Path src = dir1().concat("src").createDirectory();
        Path dst = dir2().concat("dst");

        try (Tracker tracker = registerMockTracker();
             Recorder observer = observe(dir1(), NOFOLLOW)) {
            observer.awaitModify(src, () -> src.concat("b").createFile());
            observer.awaitMove(src, dst);
            observer.awaitNoEvent(() -> dst.concat("c").createFile());

            ArgumentCaptor<Integer> fd = ArgumentCaptor.forClass(Integer.class);
            ArgumentCaptor<Integer> wd = ArgumentCaptor.forClass(Integer.class);
            verify(tracker).onWatchAdded(
                fd.capture(),
                aryEq(src.toByteArray()),
                anyInt(),
                wd.capture()
            );
            verify(tracker).onWatchRemoved(fd.getValue(), wd.getValue());
        }
    }

    @Test
    public void notifies_observer_on_max_user_instances_reached()
        throws Exception {
        int maxUserInstances = maxUserInstances();
        List<Observation> observations = new ArrayList<>(maxUserInstances);
        try {

            for (int i = 1; i < maxUserInstances + 10; i++) {
                Path child = dir1().concat(String.valueOf(i)).createFile();
                Observer observer = mock(Observer.class);
                Observation observation =
                    Paths.observe(child, NOFOLLOW, observer);
                observations.add(observation);
                if (i <= maxUserInstances - 30) {
                    assertFalse("Failed at " + i, observation.isClosed());
                    verify(observer, never()).onIncompleteObservation(any(
                        IOException.class));
                } else if (i > maxUserInstances) {
                    assertTrue("Failed at " + i, observation.isClosed());
                    verify(observer).onIncompleteObservation(any(IOException.class));
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

    @Test
    public void notifies_observer_on_max_user_watches_reached_on_observe()
        throws Exception {

        int limit = 10;
        int count = limit * 2;
        for (int i = 0; i < count; i++) {
            createRandomChildDir(dir1());
        }

        @SuppressWarnings("unchecked")
        java.util.function.Consumer<java.nio.file.Path> consumer =
            mock(java.util.function.Consumer.class);
        Observer observer = mock(Observer.class);

        try (Tracker tracker = registerMockTracker();
             Observable observation = new Observable(dir1(), observer)) {
            observation.start(FOLLOW, consumer, limit);
            verify(observer, atLeastOnce()).onIncompleteObservation(any(
                IOException.class));
            verify(
                consumer,
                times(count)
            ).accept(notNull(java.nio.file.Path.class));
            verifyAllWatchesRemovedAndRootWatchAddedOnMaxUserWatchesReached(
                tracker,
                limit
            );
        }
    }

    @Test
    public void notifies_observer_on_max_user_watches_reached_during_observe()
        throws Exception {

        int limit = 10;
        int count = limit / 2;
        for (int i = 0; i < count; i++) {
            createRandomChildDir(dir1());
        }

        @SuppressWarnings("unchecked")
        java.util.function.Consumer<java.nio.file.Path> consumer =
            mock(java.util.function.Consumer.class);
        Observer observer = mock(Observer.class);

        try (Tracker tracker = registerMockTracker();
             Observable observation = new Observable(dir1(), observer)) {
            observation.start(FOLLOW, consumer, limit);
            verify(
                observer,
                never()
            ).onIncompleteObservation(any(IOException.class));
            assertFalse(observation.isClosed());
            for (int i = 0; i < limit; i++) {
                createRandomChildDir(dir1());
            }

            verify(
                observer,
                timeout(10000).atLeastOnce()
            ).onIncompleteObservation(any(IOException.class));
            verify(
                consumer,
                times(count)
            ).accept(notNull(java.nio.file.Path.class));
            verifyAllWatchesRemovedAndRootWatchAddedOnMaxUserWatchesReached(
                tracker,
                limit
            );
        }
    }

    private Path createRandomChildDir(Path dir) throws IOException {
        while (true) {
            try {
                return dir.concat(String.valueOf(random())).createDirectory();
            } catch (AlreadyExist ignore) {
            }
        }
    }

    /**
     * Verifies when max user watches is reached, all existing watches will
     * be released
     * but the root watch will be re added so that many of the modification
     * to the root
     * dir will still be notified.
     */
    private static void verifyAllWatchesRemovedAndRootWatchAddedOnMaxUserWatchesReached(
        Tracker tracker, int maxUserWatches
    ) {

        ArgumentCaptor<Integer> fd = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> wdsAdded =
            ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> wdsRemoved =
            ArgumentCaptor.forClass(Integer.class);

        InOrder order = inOrder(tracker);
        order.verify(tracker).onInit(fd.capture());
        order.verify(tracker, times(maxUserWatches)).onWatchAdded(
            eq(fd.getValue()),
            any(byte[].class),
            anyInt(),
            wdsAdded.capture()
        );

        order.verify(tracker, times(maxUserWatches - 1)).onWatchRemoved(
            eq(fd.getValue()),
            wdsRemoved.capture()
        );

        int rootWd = wdsAdded.getAllValues().get(0);
        List<Integer> expectedWdsRemoved =
            new ArrayList<>(wdsAdded.getAllValues());
        expectedWdsRemoved.remove((Object) rootWd);
        assertValuesEqual(expectedWdsRemoved, wdsRemoved.getAllValues());
        order.verifyNoMoreInteractions();

    }

    private static <T extends Comparable<? super T>> void assertValuesEqual(
        List<T> xs,
        List<T> ys
    ) {
        Collections.sort(xs);
        Collections.sort(ys);
        assertEquals(xs, ys);
    }

    @Test
    public void releases_all_watches_on_close() throws Exception {

        Path a = dir1().concat("a").createDirectory();
        Path b = dir1().concat("b").createDirectory();
        try (Tracker tracker = registerMockTracker()) {

            Paths.observe(dir1(), NOFOLLOW, mock(Observer.class)).close();

            ArgumentCaptor<Integer> fd = ArgumentCaptor.forClass(Integer.class);
            ArgumentCaptor<Integer> wd = ArgumentCaptor.forClass(Integer.class);
            verify(tracker).onInit(fd.capture());
            verify(tracker).onWatchAdded(
                eq(fd.getValue()),
                aryEq(a.toByteArray()),
                anyInt(),
                wd.capture()
            );
            verify(tracker).onWatchAdded(
                eq(fd.getValue()),
                aryEq(b.toByteArray()),
                anyInt(),
                wd.capture()
            );
            verify(tracker).onWatchRemoved(
                fd.getValue(),
                wd.getAllValues().get(0)
            );
            verify(tracker).onWatchRemoved(
                fd.getValue(),
                wd.getAllValues().get(1)
            );
            verify(tracker).onClose(eq(OptionalInt.of(fd.getValue())), any());

        }
    }

    @Test
    public void releases_fd_on_close() throws Exception {
        ArgumentCaptor<Integer> fd = ArgumentCaptor.forClass(Integer.class);
        try (Tracker tracker = registerMockTracker()) {
            Paths.observe(dir1(), NOFOLLOW, mock(Observer.class)).close();
            verify(tracker).onInit(fd.capture());
            verify(tracker).onClose(eq(OptionalInt.of(fd.getValue())), any());
        }
    }

    private int maxUserInstances() throws IOException {
        assumeTrue(Build.VERSION.SDK_INT < Build.VERSION_CODES.O);
        Path limitFile = Path.of("/proc/sys/fs/inotify/max_user_instances");
        return parseInt(Paths.readAllUtf8(limitFile).trim());
    }

    @Test
    public void observe_on_link_follow() throws Exception {

        Path dir = dir1().concat("dir").createDirectory();
        Path link = dir1().concat("link").createSymbolicLink(dir);
        Path child = link.concat("dir").createDirectory();
        try (Recorder observer = observe(link, FOLLOW)) {
            observer.awaitModifyByCreateFile(child, "a");
        }
    }

    @Test
    public void move_unreadable_dir_in_will_notify_incomplete_observation()
        throws Exception {

        testMoveDirIn(
            new PreActions().removeReadPermissions(),
            new PostActions()
                .awaitOnIncompleteObservation()
                .awaitCreateFileInParent("parent watch still works")
        );
    }

    @Test
    public void move_dir_in_then_change_its_permission() throws Exception {
        testMoveDirIn(new PostActions().awaitRemoveAllPermissions());
    }

    @Test
    public void rename_dir() throws Exception {
        Path src = dir1().concat("a").createDirectory();
        Path dst = dir1().concat("b");
        try (Recorder observer = observe(dir1())) {
            observer.awaitModifyByCreateFile(src, "1");
            observer.awaitMove(src, dst);
            observer.awaitModifyByCreateFile(dst, "2");
        }
    }

    @Test
    public void move_dir_in_then_add_file_into_it() throws Exception {
        testMoveDirIn(new PostActions().awaitCreateDir("hello"));
    }

    @Test
    public void move_dir_in_then_delete_file_from_it() throws Exception {
        testMoveDirIn(
            new PreActions().createFile("hello"),
            new PostActions().awaitDelete("hello")
        );
    }

    @Test
    public void move_dir_in_then_move_file_into_it() throws Exception {
        Path extra = dir2().concat("hello").createFile();
        testMoveDirIn(new PostActions().awaitMoveIn(extra));
    }

    @Test
    public void move_dir_in_then_move_file_out_of_it() throws Exception {

        Path src = dir2().concat("a").createDirectory();
        Path dir = dir1().concat("a");
        Path child = dir.concat("b");
        try (Recorder observer = observe(dir1())) {
            observer.awaitMove(src, dir);
            observer.await(
                asList(
                    event(MODIFY, dir),
                    event(MODIFY, dir)
                ),
                compose(
                    child::createFile,
                    () -> child.move(dir2().concat("b"))
                )
            );
        }
    }

    @Test
    public void move_file_in() throws Exception {

        Path src = dir2().concat("a").createFile();
        Path dst = dir1().concat("b");
        try (Recorder observer = observe(dir1())) {
            observer.awaitMove(src, dst);
        }
    }

    @Test
    public void move_file_out() throws Exception {

        Path file = dir1().concat("a").createFile();
        try (Recorder observer = observe(dir1())) {
            observer.awaitMove(file, dir2().concat("a"));
        }
    }

    @Test
    public void move_self_out() throws Exception {
        Path file = dir1().concat("file").createFile();
        Path dir = dir1().concat("dir").createDirectory();
        testMoveSelfOut(file, dir2().concat("a"));
        testMoveSelfOut(dir, dir2().concat("b"));
    }

    private void testMoveSelfOut(
        Path src,
        Path dst
    ) throws Exception {

        try (Recorder observer = observe(src)) {
            observer.awaitMove(src, dst);
        }
    }

    @Test
    public void modify_file_content() throws Exception {
        Path file = dir1().concat("a").createFile();
        testModifyFileContent(file, file);
        testModifyFileContent(file, dir1());
    }

    private void testModifyFileContent(
        Path file,
        Path observable
    ) throws Exception {

        try (Recorder observer = observe(observable)) {
            observer.awaitModifyByAppend(file, "abc");
        }
    }

    @Test
    public void modify_permissions() throws Exception {
        Path file = dir1().concat("file").createFile();
        Path dir = dir1().concat("dir").createDirectory();
        testModifyPermission(file, file);
        testModifyPermission(file, dir1());
        testModifyPermission(dir, dir);
        testModifyPermission(dir, dir1());
    }

    private void testModifyPermission(
        Path target,
        Path observable
    ) throws Exception {

        Set<PosixFilePermission> oldPerms = target.readAttributes(
            PosixFileAttributes.class,
            java.nio.file.LinkOption.NOFOLLOW_LINKS
        ).permissions();
        Set<PosixFilePermission> newPerms = EnumSet.copyOf(oldPerms);

        Set<PosixFilePermission> writePermissions =
            PosixFilePermissions.fromString("-w--w--w-");

        if (newPerms.equals(oldPerms)) {
            newPerms.addAll(writePermissions);
        }

        if (newPerms.equals(oldPerms)) {
            newPerms.removeAll(writePermissions);
        }

        try (Recorder observer = observe(observable)) {
            observer.awaitModify(target, () -> target.setPermissions(newPerms));
        }
    }

    @Test
    public void modify_mtime() throws Exception {
        Path file = dir1().concat("file").createFile();
        Path dir = dir1().concat("dir").createDirectory();
        testModifyLastModifiedTime(file, file);
        testModifyLastModifiedTime(file, dir1());
        testModifyLastModifiedTime(dir, dir);
        testModifyLastModifiedTime(dir, dir1());
    }

    private void testModifyLastModifiedTime(
        Path target,
        Path observable
    ) throws Exception {

        java.time.Instant old = target.stat(NOFOLLOW).lastModifiedTime();
        java.time.Instant t = old.minusSeconds(1);
        try (Recorder observer = observe(observable)) {
            observer.awaitModifyBySetLastModifiedTime(target, t);
        }
    }

    @Test
    public void delete() throws Exception {
        Path file = dir1().concat("file");
        Path dir = dir1().concat("dir");
        testDelete(file.createFile(), file);
        testDelete(file.createFile(), dir1());
        testDelete(dir.createDirectory(), dir);
        testDelete(dir.createDirectory(), dir1());
    }

    private void testDelete(Path target, Path observable) throws Exception {
        boolean file = target.stat(NOFOLLOW).isRegularFile();
        try (Recorder observer = observe(observable)) {
            List<WatchEvent> expected = new ArrayList<>();
            // If target is file and observing on the file itself, an IN_ATTRIB
            // event is first sent in addition to IN_DELETE when deleting
            if (file && target.equals(observable)) {
                expected.add(event(MODIFY, target));
            }
            expected.add(event(DELETE, target));
            observer.await(expected, () -> target.delete());
        }
    }

    @Test
    public void delete_recreate_dir_will_be_observed() throws Exception {
        Path dir = dir1().concat("dir");
        Path file = dir.concat("file");
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
                        () -> file.createFile(),
                        () -> file.delete(),
                        () -> dir.delete()
                    )
                );
            }
        }
    }

    @Test
    public void create() throws Exception {
        Path file = dir1().concat("file");
        Path dir = dir1().concat("dir");
        Path link = dir1().concat("link");
        testCreateFile(file, dir1());
        testCreateDir(dir, dir1());
        testCreateSymbolicLink(link, dir1(), dir1());
    }

    private void testCreateFile(
        Path target,
        Path observable
    ) throws Exception {

        try (Recorder observer = observe(observable)) {
            observer.awaitCreateFile(target);
        }
    }

    private void testCreateDir(
        Path target,
        Path observable
    ) throws Exception {

        try (Recorder observer = observe(observable)) {
            observer.awaitCreateDir(target);
        }
    }

    private void testCreateSymbolicLink(
        Path link,
        Path target,
        Path observable
    ) throws Exception {

        try (Recorder observer = observe(observable)) {
            observer.awaitCreateSymbolicLink(link, target);
        }
    }

    @Test
    public void observe_unreadable_child_dir_will_notify_incomplete_observation()
        throws Exception {

        Path dir = dir1().concat("dir").createDirectory();
        removeReadPermissions(dir.toJavaPath());
        try (Recorder observer = observe(dir1())) {
            observer.awaitOnIncompleteObservation();
            observer.awaitCreateFile(dir1().concat("parent watch still works"));
        }
    }

    @Test
    public void create_dir_then_make_it_unreadable() throws Exception {
        Path dir = dir1().concat("dir");
        try (Recorder observer = observe(dir1())) {
            observer.awaitCreateDir(dir);
            observer.awaitModifyBySetPermissions(
                dir,
                EnumSet.of(OWNER_WRITE, OWNER_EXECUTE)
            );
            observer.awaitModifyByCreateFile(dir, "a");
        }
    }

    @Test
    public void create_dir_then_create_items_into_it() throws Exception {
        Path dir = dir1().concat("dir");
        try (Recorder observer = observe(dir1())) {
            observer.awaitCreateDir(dir);
            observer.await(
                asList(
                    event(MODIFY, dir),
                    event(MODIFY, dir),
                    event(MODIFY, dir)
                ),
                compose(
                    () -> dir.concat("file").createFile(),
                    () -> dir.concat("dir2()").createDirectory(),
                    () -> dir.concat("link").createSymbolicLink(dir1())
                )
            );
        }
    }

    @Test
    public void create_dir_then_delete_items_from_it() throws Exception {
        Path parent = dir1().concat("parent");
        Path file = parent.concat("file");
        Path dir = parent.concat("dir");
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
                    () -> file.createFile(),
                    () -> dir.createDirectory(),
                    () -> file.delete(),
                    () -> dir.delete()
                )
            );
        }
    }

    @Test
    public void create_dir_then_move_items_out_of_it() throws Exception {
        Path parent = dir1().concat("parent");
        Path file = parent.concat("file");
        Path dir = parent.concat("dir");
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
                    file::createFile,
                    dir::createDirectory,
                    () -> file.move(dir2().concat("file")),
                    () -> dir.move(dir2().concat("dir"))
                )
            );
        }
    }

    @Test
    public void create_dir_then_move_file_into_it() throws Exception {
        Path parent = dir1().concat("parent");
        Path file = dir2().concat("file").createFile();
        Path dir = dir2().concat("dir").createDirectory();
        try (Recorder observer = observe(dir1())) {
            observer.awaitCreateDir(parent);
            observer.await(
                asList(
                    event(MODIFY, parent),
                    event(MODIFY, parent)
                ),
                compose(
                    () -> file.move(parent.concat("file")),
                    () -> dir.move(parent.concat("dir"))
                )
            );
        }
    }

    @Test
    public void multiple_operations() throws Exception {
        Path a = dir1().concat("a");
        Path b = dir1().concat("b");
        Path c = dir1().concat("c");
        Path d = dir1().concat("d");
        try (Recorder observer = observe(dir1())) {
            observer.awaitCreateDir(a);
            observer.awaitCreateDir(b);
            observer.awaitModify(a, () -> a.concat("1").createFile());
            observer.awaitMove(dir2().concat("c").createFile(), c);
            observer.awaitMove(c, dir2().concat("2"));
            observer.awaitDelete(b);
            observer.awaitCreateFile(d);
        }
    }

    private static WatchEvent event(Event kind, Path file) {
        return WatchEvent.create(kind, file);
    }

    private static Action compose(Action... callables) throws Exception {
        return () -> {
            for (Action callable : callables) {
                callable.call();
            }
        };
    }

    interface Action {
        void call() throws Exception;
    }

    static class Recorder extends Tracker implements Observer {

        private final Path root;

        private final Observer observer = mock(Observer.class);

        private Observation observation;
        private int fd;
        private int wd;
        private final Map<Path, Integer> allChildWds =
            new ConcurrentHashMap<>();
        private final Set<Integer> validChildWds = new CopyOnWriteArraySet<>();

        private final List<WatchEvent> expected = new CopyOnWriteArrayList<>();
        private final List<WatchEvent> actual = new CopyOnWriteArrayList<>();
        private volatile CountDownLatch success;

        Recorder(Path root) {
            this.root = root;
        }

        static Recorder observe(Path observable) throws Exception {
            return observe(observable, NOFOLLOW);
        }

        static Recorder observe(Path file, LinkOption option) throws Exception {
            return observe(file, option, true);
        }

        static Recorder observe(
            Path file,
            LinkOption option,
            boolean verifyTracker
        ) throws Exception {

            try (Tracker tracker = registerMockTracker()) {
                Recorder observer = new Recorder(file);
                InotifyTracker.get().registerTracker(observer);
                observer.observation = Paths.observe(file, option, observer);
                if (verifyTracker) {
                    if (observer.observation.isClosed()) {
                        fail(String.valueOf(observer.observation.closeReason()));
                    }
                    verifyTracker(observer, tracker, file, option);
                }
                return observer;
            }
        }

        private static void verifyTracker(
            Recorder observer,
            Tracker tracker,
            Path file,
            LinkOption option
        ) throws IOException {

            ArgumentCaptor<Integer> fd = ArgumentCaptor.forClass(Integer.class);
            ArgumentCaptor<Integer> wd = ArgumentCaptor.forClass(Integer.class);
            InOrder order = inOrder(tracker);
            order.verify(tracker).onInit(fd.capture());
            order.verify(tracker).onWatchAdded(
                eq(fd.getValue()),
                aryEq(file.toByteArray()),
                anyInt(),
                wd.capture()
            );

            observer.fd = fd.getValue();
            observer.wd = wd.getValue();

            if (file.stat(option).isDirectory()) {

                Paths.listDirectories(file, (Consumer) dir -> {
                    if (dir.isReadable()) {
                        order.verify(tracker).onWatchAdded(
                            eq(fd.getValue()),
                            AdditionalMatchers.aryEq(dir.toByteArray()),
                            anyInt(),
                            wd.capture()
                        );
                    }
                    observer.allChildWds.put(dir, wd.getValue());
                    observer.validChildWds.add(wd.getValue());
                    return true;
                });

            }

            order.verifyNoMoreInteractions();

        }

        @Override
        public void onWatchAdded(int fd, byte[] path, int mask, int wd) {
            super.onWatchAdded(fd, path, mask, wd);
            if (this.fd == fd) {
                this.allChildWds.put(Path.of(path), wd);
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
        public void onEvent(Event event, java.nio.file.Path childFileName) {
            observer.onEvent(event, childFileName);
            Path target =
                childFileName == null
                    ? root
                    : root.concat(childFileName.toString());
            actual.add(WatchEvent.create(event, target));
            if (expected.equals(actual)) {
                success.countDown();
            }
        }

        @Override
        public void onIncompleteObservation(IOException cause) {
            observer.onIncompleteObservation(cause);
        }

        void awaitOnIncompleteObservation() {
            verify(observer).onIncompleteObservation(any(IOException.class));
        }

        void await(Event kind, Path file, Action action) throws Exception {
            await(WatchEvent.create(kind, file), action);
        }

        void await(WatchEvent expected, Action action) throws Exception {
            await(singletonList(expected), action);
        }

        void await(List<WatchEvent> expected, Action action) throws Exception {
            this.actual.clear();
            this.expected.clear();
            this.expected.addAll(expected);
            this.success = new CountDownLatch(1);
            action.call();
            if (!success.await(5, SECONDS)) {
                fail("\nexpected: " + this.expected
                    + "\nactual:   " + this.actual);
            }
        }

        void awaitNoEvent(Action action) throws Exception {
            actual.clear();
            expected.clear();
            success = new CountDownLatch(0);
            action.call();
            sleep(SECONDS.toMillis(1));
            if (!actual.isEmpty()) {
                fail("expected no event but got " + actual);
            }
        }

        void awaitCreateFile(Path target) throws Exception {
            try (Tracker tracker = registerMockTracker()) {
                await(CREATE, target, target::createFile);
                verifyZeroInteractions(tracker);
            }
        }

        void awaitCreateDir(Path target) throws Exception {
            try (Tracker tracker = registerMockTracker()) {
                await(CREATE, target, target::createDirectory);
                verify(tracker).onWatchAdded(
                    eq(fd),
                    aryEq(target.toByteArray()),
                    anyInt(),
                    anyInt()
                );
            }
        }

        void awaitCreateSymbolicLink(Path link, Path target) throws Exception {
            try (Tracker tracker = registerMockTracker()) {
                await(CREATE, link, () -> link.createSymbolicLink(target));
                verifyZeroInteractions(tracker);
            }
        }

        void awaitDelete(Path target) throws Exception {
            await(DELETE, target, target::delete);
        }

        void awaitMove(Path src, Path dst) throws Exception {
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
                    awaitModify(src.parent(), () -> src.move(dst));

                } else if (root.equals(dst.parent().parent())) {
                    awaitModify(dst.parent(), () -> src.move(dst));

                } else {
                    fail("\nroot=" + root +
                        "\nsrc=" + src +
                        "\ndst=" + dst);
                }
            }
        }

        private void awaitMoveWithinSameDir(
            Tracker tracker,
            Path src,
            Path dst
        ) throws Exception {

            boolean isDir = src.stat(NOFOLLOW).isDirectory();
            await(
                asList(
                    event(DELETE, src),
                    event(CREATE, dst)
                ),
                () -> src.move(dst)
            );
            InOrder order = inOrder(tracker);
            if (isDir) {
                order.verify(tracker).onWatchRemoved(fd, allChildWds.get(src));
                order.verify(tracker).onWatchAdded(
                    eq(fd),
                    aryEq(dst.toByteArray()),
                    anyInt(),
                    anyInt()
                );
            }
            order.verifyNoMoreInteractions();
        }

        private void awaitMoveFrom(Tracker tracker, Path src, Path dst)
            throws Exception {
            boolean srcIsDir = src.stat(NOFOLLOW).isDirectory();
            await(DELETE, src, () -> src.move(dst));
            if (srcIsDir) {
                verify(tracker).onWatchRemoved(fd, allChildWds.get(src));
            }
            verifyNoMoreInteractions(tracker);
        }

        private void awaitMoveTo(Tracker tracker, Path src, Path dst)
            throws Exception {
            boolean srcIsDir = src.stat(NOFOLLOW).isDirectory();
            boolean readable = src.isReadable();
            await(CREATE, dst, () -> src.move(dst));

            if (srcIsDir) {
                if (readable) {
                    verify(tracker).onWatchAdded(
                        eq(fd),
                        aryEq(dst.toByteArray()),
                        anyInt(),
                        anyInt()
                    );
                    verify(observer, never()).onIncompleteObservation(any(
                        IOException.class));
                } else {
                    verify(tracker, never()).onWatchAdded(
                        eq(fd),
                        aryEq(dst.toByteArray()),
                        anyInt(),
                        anyInt()
                    );
                    verify(observer).onIncompleteObservation(any(IOException.class));
                }
            } else {
                verify(tracker, never()).onWatchAdded(
                    eq(fd),
                    aryEq(dst.toByteArray()),
                    anyInt(),
                    anyInt()
                );
                verify(observer, never()).onIncompleteObservation(any(
                    IOException.class));
            }
            verifyNoMoreInteractions(tracker);
        }

        private void awaitMoveSelf(Tracker tracker, Path dst) throws Exception {
            await(DELETE, root, () -> root.move(dst));
            for (int wd : validChildWds) {
                verify(tracker).onWatchRemoved(fd, wd);
            }
            verify(tracker).onWatchRemoved(fd, wd);
            verify(tracker).onClose(eq(OptionalInt.of(fd)), any());
            verifyNoMoreInteractions(tracker);
        }

        void awaitModify(Path target, Action action) throws Exception {
            try (Tracker tracker = registerMockTracker()) {
                await(MODIFY, target, action);
                verifyZeroInteractions(tracker);
            }
        }

        void awaitModifyByCreateFile(Path target, String child)
            throws Exception {
            awaitModify(target, () -> target.concat(child).createFile());
        }

        void awaitModifyByCreateDir(Path target, String child)
            throws Exception {
            awaitModify(target, () -> target.concat(child).createDirectory());
        }

        void awaitModifyBySetPermissions(
            Path target,
            Set<PosixFilePermission> perms
        ) throws Exception {
            awaitModify(target, () -> target.setPermissions(perms));
        }

        void awaitModifyByDelete(Path target, String child) throws Exception {
            awaitModify(target, () -> target.concat(child).delete());
        }

        void awaitModifyBySetLastModifiedTime(
            Path target,
            java.time.Instant time
        ) throws Exception {
            awaitModify(
                target,
                () -> target.setLastModifiedTime(FileTime.from(time))
            );
        }

        void awaitModifyByAppend(Path target, CharSequence content)
            throws Exception {
            awaitModify(target, () -> Paths.writeUtf8(target, content));
        }
    }

    private static Tracker registerMockTracker() {
        Tracker tracker = mock(Tracker.class);
        try {
            doAnswer(invocation -> {
                InotifyTracker.get().unregisterTracker(tracker);
                return null;
            }).when(tracker).close();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        InotifyTracker.get().registerTracker(tracker);
        return tracker;
    }

    public static abstract class Tracker
        implements InotifyTracker.Tracker, Closeable {

        volatile Throwable closeReason;

        @Override
        public void close() throws IOException {
        }

        @Override
        public void onClose(OptionalInt fd, Throwable cause) {
            this.closeReason = cause;
        }

        @Override
        public void onWatchRemoved(int fd, int wd) {
        }

        @Override
        public void onWatchAdded(int fd, byte[] path, int mask, int wd) {
        }

        @Override
        public void onInit(int fd) {
        }

    }

    static final class WatchEvent {

        final Event kind;
        final Path path;

        private WatchEvent(Event kind, Path path) {
            this.kind = requireNonNull(kind);
            this.path = requireNonNull(path);
        }

        static WatchEvent create(Event kind, Path path) {
            return new WatchEvent(kind, path);
        }

        @Override
        public String toString() {
            return "WatchEvent{" +
                "kind=" + kind +
                ", path=" + path +
                '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            WatchEvent that = (WatchEvent) o;

            return kind == that.kind &&
                path.equals(that.path);
        }

        @Override
        public int hashCode() {
            int result = kind.hashCode();
            result = 31 * result + path.hashCode();
            return result;
        }
    }

    private interface PreAction {
        void action(Path src) throws Exception;
    }

    private interface PostAction {
        void action(Path dst, Recorder observer) throws Exception;
    }

    private final class PreActions implements PreAction {

        private final List<PreAction> moves = new ArrayList<>();

        @Override
        public void action(Path src) throws Exception {
            for (PreAction move : moves) {
                move.action(src);
            }
        }

        PreActions add(PreAction move) {
            moves.add(move);
            return this;
        }

        PreActions removeReadPermissions() {
            return add(src -> Paths.removeReadPermissions(src.toJavaPath()));
        }

        PreActions createFile(String name) {
            return add(src -> src.concat(name).createFile());
        }

    }

    private static final class PostActions implements PostAction {

        private final List<PostAction> moves = new ArrayList<>();

        @Override
        public void action(Path dst, Recorder observer) throws Exception {
            for (PostAction move : moves) {
                move.action(dst, observer);
            }
        }

        PostActions add(PostAction move) {
            moves.add(move);
            return this;
        }

        PostActions awaitCreateFileInParent(String name) {
            return add((dst, observer) -> observer.awaitCreateFile(dst.parent()
                .concat(name)));
        }

        PostActions awaitDelete(String name) {
            return add((dst, observer) -> observer.awaitModifyByDelete(
                dst,
                name
            ));
        }

        PostActions awaitRemoveAllPermissions() {
            return add((dst, observer) -> observer.awaitModifyBySetPermissions(
                dst, emptySet()
            ));
        }

        PostActions awaitCreateDir(String name) {
            return add((dst, observer) -> observer.awaitModifyByCreateDir(
                dst,
                name
            ));
        }

        PostActions awaitMoveIn(Path src) {
            return add((dst, observer) -> observer.awaitMove(
                src,
                dst.concat(src.getFileName())
            ));
        }

        PostActions awaitOnIncompleteObservation() {
            return add((dst, observer) -> observer.awaitOnIncompleteObservation());
        }

    }

    private void testMoveDirIn(PostAction post) throws Exception {
        testMoveDirIn(new PreActions(), post);
    }

    private void testMoveDirIn(PreAction pre, PostAction post)
        throws Exception {
        Path dst = dir1().concat("a");
        Path src = dir2().concat("a").createDirectory();
        pre.action(src);
        try (Recorder observer = observe(dir1())) {
            observer.awaitMove(src, dst);
            post.action(dst, observer);
        }
    }

}
