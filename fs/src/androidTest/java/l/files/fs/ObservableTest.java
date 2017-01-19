package l.files.fs;

import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;

import l.files.fs.Path.Consumer;
import l.files.fs.event.Event;
import l.files.fs.event.Observation;
import l.files.fs.event.Observer;
import l.files.testing.fs.ExtendedPath;
import l.files.testing.fs.PathBaseTest;

import static java.lang.Integer.parseInt;
import static java.lang.Math.random;
import static java.lang.Thread.sleep;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.base.Objects.requireNonNull;
import static l.files.fs.Instant.EPOCH;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.ObservableTest.Recorder.observe;
import static l.files.fs.Permission.OWNER_EXECUTE;
import static l.files.fs.Permission.OWNER_WRITE;
import static l.files.fs.event.Event.CREATE;
import static l.files.fs.event.Event.DELETE;
import static l.files.fs.event.Event.MODIFY;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
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

public final class ObservableTest extends PathBaseTest {

    public void test_able_to_continue_observing_existing_dirs_when_new_dir_added_is_not_observable()
            throws Exception {

        Path readableDir = dir1().concat("readable");
        Path unreadableDir = dir1().concat("unreadable");

        readableDir.createDir();
        Recorder recorder = observe(dir1(), FOLLOW);
        try {
            recorder.awaitModifyByCreateFile(readableDir, "aa");
            recorder.await(CREATE, unreadableDir, newCreateDir(
                    unreadableDir, EnumSet.of(OWNER_EXECUTE, OWNER_WRITE)));

            recorder.awaitNoEvent(newCreateFile(unreadableDir.concat("zz")));
            recorder.awaitModifyByCreateFile(readableDir, "ab");
        } finally {
            recorder.close();
        }
    }

    public void test_able_to_observe_the_rest_of_the_files_when_some_are_not_observable()
            throws Exception {

        List<Path> observables = new ArrayList<>();
        observables.add(createRandomChildDir(dir1()));
        observables.add(createRandomChildDir(dir1()));

        ExtendedPath unobservable = dir1().concat("unobservable").createDir();
        unobservable.removePermissions(Permission.read());

        observables.add(createRandomChildDir(dir1()));
        observables.add(createRandomChildDir(dir1()));

        Recorder recorder = observe(dir1(), FOLLOW);
        try {
            recorder.awaitCreateFile(dir1().concat("1"));
            for (Path observable : observables) {
                recorder.awaitModifyByCreateFile(observable, "1");
            }
            recorder.awaitNoEvent(newCreateFile(unobservable.concat("1")));
        } finally {
            recorder.close();
        }
    }

    public void test_no_observe_on_procfs() throws Exception {

        Tracker tracker = registerMockTracker();
        try {
            Recorder observer = observe(
                    Path.create("/proc/self"), FOLLOW, false);
            try {
                assertTrue(observer.isClosed());
                verifyZeroInteractions(tracker);
            } finally {
                observer.close();
            }
        } finally {
            tracker.close();
        }
    }

    public void test_observe_on_regular_file() throws Exception {
        Path file = dir1().concat("file").createFile();
        Recorder observer = observe(file, NOFOLLOW);
        try {
            observer.awaitModifyByAppend(file, "hello");
        } finally {
            observer.close();
        }
    }

    public void test_observe_on_link() throws Exception {
        Path file = dir1().concat("link").createSymbolicLink(dir2());
        Recorder observer = observe(file, NOFOLLOW);
        try {
            observer.awaitModifyBySetLastModifiedTime(file, EPOCH);
        } finally {
            observer.close();
        }
    }

    public void test_release_watch_when_dir_moves_out() throws Exception {

        Path src = dir1().concat("src").createDir();
        Path dst = dir2().concat("dst");

        Tracker tracker = registerMockTracker();
        try {
            Recorder observer = observe(dir1(), NOFOLLOW);
            try {
                observer.awaitModify(src, newCreateFile(src.concat("b")));
                observer.awaitMove(src, dst);
                observer.awaitNoEvent(newCreateFile(dst.concat("c")));

                ArgumentCaptor<Integer> fd = ArgumentCaptor.forClass(Integer.class);
                ArgumentCaptor<Integer> wd = ArgumentCaptor.forClass(Integer.class);
                verify(tracker).onWatchAdded(fd.capture(), aryEq(src.toByteArray()), anyInt(), wd.capture());
                verify(tracker).onWatchRemoved(fd.getValue(), wd.getValue());
            } finally {
                observer.close();
            }
        } finally {
            tracker.close();
        }
    }

    public void test_notifies_observer_on_max_user_instances_reached() throws Exception {
        int maxUserInstances = maxUserInstances();
        List<Observation> observations = new ArrayList<>(maxUserInstances);
        try {

            for (int i = 1; i < maxUserInstances + 10; i++) {
                ExtendedPath child = dir1().concat(String.valueOf(i)).createFile();
                Observer observer = mock(Observer.class);
                Observation observation = child.observe(NOFOLLOW, observer);
                observations.add(observation);
                if (i <= maxUserInstances) {
                    assertFalse("Failed at " + i, observation.isClosed());
                    verify(observer, never()).onIncompleteObservation(any(IOException.class));
                } else {
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

    public void test_notifies_observer_on_max_user_watches_reached_on_observe() throws Exception {

        int limit = 10;
        int count = limit * 2;
        for (int i = 0; i < count; i++) {
            createRandomChildDir(dir1());
        }

        Consumer consumer = mock(Consumer.class);
        given(consumer.accept(any(Path.class))).willReturn(true);
        Observer observer = mock(Observer.class);

        Tracker tracker = registerMockTracker();
        try {
            Observable observation = new Observable(dir1(), observer);
            try {
                observation.start(FOLLOW, consumer, limit);
                verify(observer, atLeastOnce()).onIncompleteObservation(any(IOException.class));
                verify(consumer, times(count)).accept(notNull(Path.class));
                verifyAllWatchesRemovedAndRootWatchAddedOnMaxUserWatchesReached(tracker, limit);
            } finally {
                observation.close();
            }
        } finally {
            tracker.close();
        }
    }

    public void test_notifies_observer_on_max_user_watches_reached_during_observe() throws Exception {

        int limit = 10;
        int count = limit / 2;
        for (int i = 0; i < count; i++) {
            createRandomChildDir(dir1());
        }

        Consumer consumer = mock(Consumer.class);
        given(consumer.accept(any(Path.class))).willReturn(true);
        Observer observer = mock(Observer.class);

        Tracker tracker = registerMockTracker();
        try {
            Observable observation = new Observable(dir1(), observer);
            try {
                observation.start(FOLLOW, consumer, limit);
                verify(observer, never()).onIncompleteObservation(any(IOException.class));
                assertFalse(observation.isClosed());
                for (int i = 0; i < limit; i++) {
                    createRandomChildDir(dir1());
                }

                verify(observer, timeout(10000).atLeastOnce()).onIncompleteObservation(any(IOException.class));
                verify(consumer, times(count)).accept(notNull(Path.class));
                verifyAllWatchesRemovedAndRootWatchAddedOnMaxUserWatchesReached(tracker, limit);
            } finally {
                observation.close();
            }
        } finally {
            tracker.close();
        }
    }

    private Path createRandomChildDir(Path dir) throws IOException {
        while (true) {
            try {
                return dir.concat(String.valueOf(random())).createDir();
            } catch (AlreadyExist ignore) {
            }
        }
    }

    /**
     * Verifies when max user watches is reached, all existing watches will be released
     * but the root watch will be re added so that many of the modification to the root
     * dir will still be notified.
     */
    private static void verifyAllWatchesRemovedAndRootWatchAddedOnMaxUserWatchesReached(
            Tracker tracker, int maxUserWatches) {

        ArgumentCaptor<Integer> fd = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> wdsAdded = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> wdsRemoved = ArgumentCaptor.forClass(Integer.class);

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
        List<Integer> expectedWdsRemoved = new ArrayList<>(wdsAdded.getAllValues());
        expectedWdsRemoved.remove((Object) rootWd);
        assertValuesEqual(expectedWdsRemoved, wdsRemoved.getAllValues());
        order.verifyNoMoreInteractions();

    }

    private static <T extends Comparable<? super T>> void assertValuesEqual(
            List<T> xs,
            List<T> ys) {
        Collections.sort(xs);
        Collections.sort(ys);
        assertEquals(xs, ys);
    }

    public void test_releases_all_watches_on_close() throws Exception {

        Path a = dir1().concat("a").createDir();
        Path b = dir1().concat("b").createDir();
        Tracker tracker = registerMockTracker();
        try {

            dir1().observe(NOFOLLOW, mock(Observer.class)).close();

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
            verify(tracker).onWatchRemoved(fd.getValue(), wd.getAllValues().get(0));
            verify(tracker).onWatchRemoved(fd.getValue(), wd.getAllValues().get(1));
            verify(tracker).onClose(fd.getValue());

        } finally {
            tracker.close();
        }
    }

    public void test_releases_fd_on_close() throws Exception {
        ArgumentCaptor<Integer> fd = ArgumentCaptor.forClass(Integer.class);
        Tracker tracker = registerMockTracker();
        try {
            dir1().observe(NOFOLLOW, mock(Observer.class)).close();
            verify(tracker).onInit(fd.capture());
            verify(tracker).onClose(fd.getValue());
        } finally {
            tracker.close();
        }
    }

    private int maxUserInstances() throws IOException {
        ExtendedPath limitFile = ExtendedPath.wrap(Path.create(
                "/proc/sys/fs/inotify/max_user_instances"));
        return parseInt(limitFile.readAllUtf8().trim());
    }

    public void test_observe_on_link_no_follow() throws Exception {

        Path dir = dir1().concat("dir").createDir();
        Path link = dir1().concat("link").createSymbolicLink(dir);
        Path file = link.concat("file");
        Recorder observer = observe(link, NOFOLLOW);
        try {
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
        } finally {
            observer.close();
        }
    }

    public void test_observe_on_link_follow() throws Exception {

        Path dir = dir1().concat("dir").createDir();
        Path link = dir1().concat("link").createSymbolicLink(dir);
        Path child = link.concat("dir").createDir();
        Recorder observer = observe(link, FOLLOW);
        try {
            observer.awaitModifyByCreateFile(child, "a");
        } finally {
            observer.close();
        }
    }

    public void test_move_unreadable_dir_in_will_notify_incomplete_observation()
            throws Exception {

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
        Path src = dir1().concat("a").createDir();
        Path dst = dir1().concat("b");
        Recorder observer = observe(dir1());
        try {
            observer.awaitModifyByCreateFile(src, "1");
            observer.awaitMove(src, dst);
            observer.awaitModifyByCreateFile(dst, "2");
        } finally {
            observer.close();
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
        Path extra = dir2().concat("hello").createFile();
        testMoveDirIn(new PostActions().awaitMoveIn(extra));
    }

    public void test_move_dir_in_then_move_file_out_of_it() throws Exception {

        Path src = dir2().concat("a").createDir();
        Path dir = dir1().concat("a");
        Path child = dir.concat("b");
        Recorder observer = observe(dir1());
        try {
            observer.awaitMove(src, dir);
            observer.await(
                    asList(
                            event(MODIFY, dir),
                            event(MODIFY, dir)
                    ),
                    compose(
                            newCreateFile(child),
                            newMove(child, dir2().concat("b"))
                    ));
        } finally {
            observer.close();
        }
    }

    public void test_move_file_in() throws Exception {

        Path src = dir2().concat("a").createFile();
        Path dst = dir1().concat("b");
        Recorder observer = observe(dir1());
        try {
            observer.awaitMove(src, dst);
        } finally {
            observer.close();
        }
    }

    public void test_move_file_out() throws Exception {

        Path file = dir1().concat("a").createFile();
        Recorder observer = observe(dir1());
        try {
            observer.awaitMove(file, dir2().concat("a"));
        } finally {
            observer.close();
        }
    }

    public void test_move_self_out() throws Exception {
        Path file = dir1().concat("file").createFile();
        Path dir = dir1().concat("dir").createDir();
        testMoveSelfOut(file, dir2().concat("a"));
        testMoveSelfOut(dir, dir2().concat("b"));
    }

    private void testMoveSelfOut(
            Path src,
            Path dst) throws Exception {

        Recorder observer = observe(src);
        try {
            observer.awaitMove(src, dst);
        } finally {
            observer.close();
        }
    }

    public void test_modify_file_content() throws Exception {
        Path file = dir1().concat("a").createFile();
        testModifyFileContent(file, file);
        testModifyFileContent(file, dir1());
    }

    private void testModifyFileContent(
            Path file,
            Path observable) throws Exception {

        Recorder observer = observe(observable);
        try {
            observer.awaitModifyByAppend(file, "abc");
        } finally {
            observer.close();
        }
    }

    public void test_modify_permissions() throws Exception {
        Path file = dir1().concat("file").createFile();
        Path dir = dir1().concat("dir").createDir();
        testModifyPermission(file, file);
        testModifyPermission(file, dir1());
        testModifyPermission(dir, dir);
        testModifyPermission(dir, dir1());
    }

    private void testModifyPermission(
            Path target,
            Path observable
    ) throws Exception {

        Set<Permission> oldPerms = target.stat(NOFOLLOW).permissions();
        Set<Permission> newPerms = EnumSet.copyOf(oldPerms);

        if (newPerms.equals(oldPerms)) {
            newPerms.addAll(Permission.write());
        }

        if (newPerms.equals(oldPerms)) {
            newPerms.removeAll(Permission.write());
        }

        Recorder observer = observe(observable);
        try {
            observer.awaitModify(target, newSetPermissions(target, newPerms));
        } finally {
            observer.close();
        }
    }

    public void test_modify_mtime() throws Exception {
        Path file = dir1().concat("file").createFile();
        Path dir = dir1().concat("dir").createDir();
        testModifyLastModifiedTime(file, file);
        testModifyLastModifiedTime(file, dir1());
        testModifyLastModifiedTime(dir, dir);
        testModifyLastModifiedTime(dir, dir1());
    }

    private void testModifyLastModifiedTime(
            Path target,
            Path observable) throws Exception {

        Instant old = target.stat(NOFOLLOW).lastModifiedTime();
        Instant t = Instant.of(old.seconds() - 1, old.nanos());
        Recorder observer = observe(observable);
        try {
            observer.awaitModifyBySetLastModifiedTime(target, t);
        } finally {
            observer.close();
        }
    }

    public void test_delete() throws Exception {
        Path file = dir1().concat("file");
        Path dir = dir1().concat("dir");
        testDelete(file.createFile(), file);
        testDelete(file.createFile(), dir1());
        testDelete(dir.createDir(), dir);
        testDelete(dir.createDir(), dir1());
    }

    private void testDelete(Path target, Path observable) throws Exception {
        boolean file = target.stat(NOFOLLOW).isRegularFile();
        Recorder observer = observe(observable);
        try {
            List<WatchEvent> expected = new ArrayList<>();
            // If target is file and observing on the file itself, an IN_ATTRIB
            // event is first sent in addition to IN_DELETE when deleting
            if (file && target.equals(observable)) {
                expected.add(event(MODIFY, target));
            }
            expected.add(event(DELETE, target));
            observer.await(expected, newDelete(target));
        } finally {
            observer.close();
        }
    }

    public void test_delete_recreate_dir_will_be_observed() throws Exception {
        Path dir = dir1().concat("dir");
        Path file = dir.concat("file");
        Recorder observer = observe(dir1());
        try {
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
                        ));
            }
        } finally {
            observer.close();
        }
    }

    public void test_create() throws Exception {
        Path file = dir1().concat("file");
        Path dir = dir1().concat("dir");
        Path link = dir1().concat("link");
        testCreateFile(file, dir1());
        testCreateDir(dir, dir1());
        testCreateSymbolicLink(link, dir1(), dir1());
    }

    private void testCreateFile(
            Path target,
            Path observable) throws Exception {

        Recorder observer = observe(observable);
        try {
            observer.awaitCreateFile(target);
        } finally {
            observer.close();
        }
    }

    private void testCreateDir(
            Path target,
            Path observable) throws Exception {

        Recorder observer = observe(observable);
        try {
            observer.awaitCreateDir(target);
        } finally {
            observer.close();
        }
    }

    private void testCreateSymbolicLink(
            Path link,
            Path target,
            Path observable) throws Exception {

        Recorder observer = observe(observable);
        try {
            observer.awaitCreateSymbolicLink(link, target);
        } finally {
            observer.close();
        }
    }

    public void test_observe_unreadable_child_dir_will_notify_incomplete_observation()
            throws Exception {

        ExtendedPath dir = dir1().concat("dir").createDir();
        dir.removePermissions(Permission.read());
        Recorder observer = observe(dir1());
        try {
            observer.awaitOnIncompleteObservation();
            observer.awaitCreateFile(dir1().concat("parent watch still works"));
        } finally {
            observer.close();
        }
    }

    public void test_create_dir_then_make_it_unreadable() throws Exception {
        Path dir = dir1().concat("dir");
        Recorder observer = observe(dir1());
        try {
            observer.awaitCreateDir(dir);
            observer.awaitModifyBySetPermissions(
                    dir,
                    EnumSet.of(OWNER_WRITE, OWNER_EXECUTE));
            observer.awaitModifyByCreateFile(dir, "a");
        } finally {
            observer.close();
        }
    }

    public void test_create_dir_then_create_items_into_it() throws Exception {
        Path dir = dir1().concat("dir");
        Recorder observer = observe(dir1());
        try {
            observer.awaitCreateDir(dir);
            observer.await(
                    asList(
                            event(MODIFY, dir),
                            event(MODIFY, dir),
                            event(MODIFY, dir)
                    ),
                    compose(
                            newCreateFile(dir.concat("file")),
                            newCreateDir(dir.concat("dir2()")),
                            newCreateSymbolicLink(dir.concat("link"), dir1())
                    ));
        } finally {
            observer.close();
        }
    }

    public void test_create_dir_then_delete_items_from_it() throws Exception {
        Path parent = dir1().concat("parent");
        Path file = parent.concat("file");
        Path dir = parent.concat("dir");
        Recorder observer = observe(dir1());
        try {
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
                    ));
        } finally {
            observer.close();
        }
    }

    public void test_create_dir_then_move_items_out_of_it() throws Exception {
        Path parent = dir1().concat("parent");
        Path file = parent.concat("file");
        Path dir = parent.concat("dir");
        Recorder observer = observe(dir1());
        try {
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
                            newMove(file, dir2().concat("file")),
                            newMove(dir, dir2().concat("dir"))
                    ));
        } finally {
            observer.close();
        }
    }

    public void test_create_dir_then_move_file_into_it() throws Exception {
        Path parent = dir1().concat("parent");
        Path file = dir2().concat("file").createFile();
        Path dir = dir2().concat("dir").createDir();
        Recorder observer = observe(dir1());
        try {
            observer.awaitCreateDir(parent);
            observer.await(
                    asList(
                            event(MODIFY, parent),
                            event(MODIFY, parent)
                    ),
                    compose(
                            newMove(file, parent.concat("file")),
                            newMove(dir, parent.concat("dir"))
                    ));
        } finally {
            observer.close();
        }
    }

    public void test_multiple_operations() throws Exception {
        Path a = dir1().concat("a");
        Path b = dir1().concat("b");
        Path c = dir1().concat("c");
        Path d = dir1().concat("d");
        Recorder observer = observe(dir1());
        try {
            observer.awaitCreateDir(a);
            observer.awaitCreateDir(b);
            observer.awaitModify(a, newCreateFile(a.concat("1")));
            observer.awaitMove(dir2().concat("c").createFile(), c);
            observer.awaitMove(c, dir2().concat("2"));
            observer.awaitDelete(b);
            observer.awaitCreateFile(d);
        } finally {
            observer.close();
        }
    }

    private static WatchEvent event(Event kind, Path file) {
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

    private static Callable<Void> newMove(final Path src, final Path dst) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                src.move(dst);
                return null;
            }
        };
    }

    private static Callable<Void> newDelete(final Path file) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                file.delete();
                return null;
            }
        };
    }

    private static Callable<Void> newCreateFile(final Path file) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                file.createFile();
                return null;
            }
        };
    }

    private static Callable<Void> newCreateDir(final Path dir) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                dir.createDir();
                return null;
            }
        };
    }

    private static Callable<Void> newCreateDir(
            final Path dir,
            final Set<Permission> permissions
    ) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                dir.createDir(permissions);
                return null;
            }
        };
    }

    private static Callable<Void> newCreateSymbolicLink(
            final Path link,
            final Path target
    ) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                link.createSymbolicLink(target);
                return null;
            }
        };
    }

    private static Callable<Void> newAppend(
            final Path file,
            final CharSequence content
    ) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ExtendedPath.wrap(file).writeUtf8(content);
                return null;
            }
        };
    }

    private static Callable<Void> newSetPermissions(
            final Path file,
            final Set<Permission> permissions
    ) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                file.setPermissions(permissions);
                return null;
            }
        };
    }

    private static Callable<Void> newSetLastModifiedTime(
            final Path file,
            final LinkOption option,
            final Instant instant
    ) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                file.setLastModifiedTime(option, instant);
                return null;
            }
        };
    }

    static class Recorder extends Tracker implements Observer {

        private final Path root;

        private final Observer observer = mock(Observer.class);

        private Observation observation;
        private int fd;
        private int wd;
        private final Map<Path, Integer> allChildWds = new ConcurrentHashMap<>();
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

            Tracker tracker = registerMockTracker();
            try {
                Recorder observer = new Recorder(file);
                observer.observation = ExtendedPath.wrap(file).observe(option, observer);
                if (verifyTracker) {
                    assertFalse(observer.observation.isClosed());
                    verifyTracker(observer, tracker, file, option);
                }

                InotifyTracker.get().registerTracker(observer);
                return observer;
            } finally {
                tracker.close();
            }
        }

        private static void verifyTracker(
                final Recorder observer,
                final Tracker tracker,
                final Path file,
                final LinkOption option
        ) throws IOException {

            final ArgumentCaptor<Integer> fd = ArgumentCaptor.forClass(Integer.class);
            final ArgumentCaptor<Integer> wd = ArgumentCaptor.forClass(Integer.class);
            final InOrder order = inOrder(tracker);
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

                ExtendedPath.wrap(file).listDirs(option, new Consumer() {
                    @Override
                    public boolean accept(Path dir) throws IOException {
                        if (dir.isReadable()) {
                            order.verify(tracker).onWatchAdded(
                                    eq(fd.getValue()),
                                    aryEq(dir.toByteArray()),
                                    anyInt(),
                                    wd.capture()
                            );
                        }
                        observer.allChildWds.put(dir, wd.getValue());
                        observer.validChildWds.add(wd.getValue());
                        return true;
                    }
                });

            }

            order.verifyNoMoreInteractions();

        }

        @Override
        public void onWatchAdded(int fd, byte[] path, int mask, int wd) {
            super.onWatchAdded(fd, path, mask, wd);
            if (this.fd == fd) {
                this.allChildWds.put(Path.create(path), wd);
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
        public void onEvent(Event event, Name child) {
            observer.onEvent(event, child);
            Path target = child == null ? root : root.concat(child.toPath());
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

        void await(Event kind, Path file, Callable<?> action) throws Exception {
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
            if (!success.await(5, SECONDS)) {
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

        void awaitCreateFile(Path target) throws Exception {
            Tracker tracker = registerMockTracker();
            try {
                await(CREATE, target, newCreateFile(target));
                verifyZeroInteractions(tracker);
            } finally {
                tracker.close();
            }
        }

        void awaitCreateDir(Path target) throws Exception {
            Tracker tracker = registerMockTracker();
            try {
                await(CREATE, target, newCreateDir(target));
                verify(tracker).onWatchAdded(
                        eq(fd),
                        aryEq(target.toByteArray()),
                        anyInt(),
                        anyInt());
            } finally {
                tracker.close();
            }
        }

        void awaitCreateSymbolicLink(Path link, Path target) throws Exception {
            Tracker tracker = registerMockTracker();
            try {
                await(CREATE, link, newCreateSymbolicLink(link, target));
                verifyZeroInteractions(tracker);
            } finally {
                tracker.close();
            }
        }

        void awaitDelete(Path target) throws Exception {
            await(DELETE, target, newDelete(target));
        }

        void awaitMove(Path src, Path dst) throws Exception {
            Tracker tracker = registerMockTracker();
            try {
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
                    fail("\nroot=" + root +
                            "\nsrc=" + src +
                            "\ndst=" + dst);
                }
            } finally {
                tracker.close();
            }
        }

        private void awaitMoveWithinSameDir(
                Tracker tracker,
                Path src,
                Path dst) throws Exception {

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
                order.verify(tracker).onWatchAdded(
                        eq(fd),
                        aryEq(dst.toByteArray()),
                        anyInt(),
                        anyInt()
                );
            }
            order.verifyNoMoreInteractions();
        }

        private void awaitMoveFrom(Tracker tracker, Path src, Path dst) throws Exception {
            boolean srcIsDir = src.stat(NOFOLLOW).isDirectory();
            await(DELETE, src, newMove(src, dst));
            if (srcIsDir) {
                verify(tracker).onWatchRemoved(fd, allChildWds.get(src));
            }
            verifyNoMoreInteractions(tracker);
        }

        private void awaitMoveTo(Tracker tracker, Path src, Path dst) throws Exception {
            boolean srcIsDir = src.stat(NOFOLLOW).isDirectory();
            boolean readable = src.isReadable();
            await(CREATE, dst, newMove(src, dst));

            if (srcIsDir) {
                if (readable) {
                    verify(tracker).onWatchAdded(
                            eq(fd),
                            aryEq(dst.toByteArray()),
                            anyInt(),
                            anyInt()
                    );
                    verify(observer, never()).onIncompleteObservation(any(IOException.class));
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
                verify(observer, never()).onIncompleteObservation(any(IOException.class));
            }
            verifyNoMoreInteractions(tracker);
        }

        private void awaitMoveSelf(Tracker tracker, Path dst) throws Exception {
            await(DELETE, root, newMove(root, dst));
            for (int wd : validChildWds) {
                verify(tracker).onWatchRemoved(fd, wd);
            }
            verify(tracker).onWatchRemoved(fd, wd);
            verify(tracker).onClose(fd);
            verifyNoMoreInteractions(tracker);
        }

        void awaitModify(Path target, Callable<Void> action) throws Exception {
            Tracker tracker = registerMockTracker();
            try {
                await(MODIFY, target, action);
                verifyZeroInteractions(tracker);
            } finally {
                tracker.close();
            }
        }

        void awaitModifyByCreateFile(Path target, String child) throws Exception {
            awaitModify(target, newCreateFile(target.concat(child)));
        }

        void awaitModifyByCreateDir(Path target, String child) throws Exception {
            awaitModify(target, newCreateDir(target.concat(child)));
        }

        void awaitModifyBySetPermissions(Path target, Set<Permission> perms) throws Exception {
            awaitModify(target, newSetPermissions(target, perms));
        }

        void awaitModifyByDelete(Path target, String child) throws Exception {
            awaitModify(target, newDelete(target.concat(child)));
        }

        void awaitModifyBySetLastModifiedTime(Path target, Instant time) throws Exception {
            awaitModify(target, newSetLastModifiedTime(target, NOFOLLOW, time));
        }

        void awaitModifyByAppend(Path target, CharSequence content) throws Exception {
            awaitModify(target, newAppend(target, content));
        }
    }

    private static Tracker registerMockTracker() {
        final Tracker tracker = mock(Tracker.class);
        try {
            doAnswer(new Answer<Void>() {
                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    InotifyTracker.get().unregisterTracker(tracker);
                    return null;
                }
            }).when(tracker).close();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        InotifyTracker.get().registerTracker(tracker);
        return tracker;
    }

    public static abstract class Tracker implements InotifyTracker.Tracker, Closeable {

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
            return add(new PreAction() {
                @Override
                public void action(Path src) throws Exception {
                    ExtendedPath.wrap(src).removePermissions(Permission.read());
                }
            });
        }

        PreActions createFile(final String name) {
            return add(new PreAction() {
                @Override
                public void action(Path src) throws Exception {
                    src.concat(name).createFile();
                }
            });
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

        PostActions awaitCreateFileInParent(final String name) {
            return add(new PostAction() {
                @Override
                public void action(Path dst, Recorder observer) throws Exception {
                    observer.awaitCreateFile(dst.parent().concat(name));
                }
            });
        }

        PostActions awaitDelete(final String name) {
            return add(new PostAction() {
                @Override
                public void action(Path dst, Recorder observer) throws Exception {
                    observer.awaitModifyByDelete(dst, name);
                }
            });
        }

        PostActions awaitRemoveAllPermissions() {
            return add(new PostAction() {
                @Override
                public void action(Path dst, Recorder observer) throws Exception {
                    observer.awaitModifyBySetPermissions(dst, Permission.none());
                }
            });
        }

        PostActions awaitCreateDir(final String name) {
            return add(new PostAction() {
                @Override
                public void action(Path dst, Recorder observer) throws Exception {
                    observer.awaitModifyByCreateDir(dst, name);
                }
            });
        }

        PostActions awaitMoveIn(final Path src) {
            return add(new PostAction() {
                @Override
                public void action(Path dst, Recorder observer) throws Exception {
                    observer.awaitMove(src, dst.concat(src.name().toPath()));
                }
            });
        }

        PostActions awaitOnIncompleteObservation() {
            return add(new PostAction() {
                @Override
                public void action(Path dst, Recorder observer) throws Exception {
                    observer.awaitOnIncompleteObservation();
                }
            });
        }

    }

    private void testMoveDirIn(PostAction post) throws Exception {
        testMoveDirIn(new PreActions(), post);
    }

    private void testMoveDirIn(PreAction pre, PostAction post) throws Exception {
        Path dst = dir1().concat("a");
        Path src = dir2().concat("a").createDir();
        pre.action(src);
        Recorder observer = observe(dir1());
        try {
            observer.awaitMove(src, dst);
            post.action(dst, observer);
        } finally {
            observer.close();
        }
    }

}
