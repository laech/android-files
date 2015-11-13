package l.files.fs.local;

import com.google.auto.value.AutoValue;

import org.junit.Test;
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

import l.files.base.io.Closer;
import l.files.fs.AlreadyExist;
import l.files.fs.Event;
import l.files.fs.File;
import l.files.fs.FileConsumer;
import l.files.fs.Instant;
import l.files.fs.LinkOption;
import l.files.fs.Name;
import l.files.fs.Observation;
import l.files.fs.Observer;
import l.files.fs.Permission;
import l.files.fs.Stream;

import static android.os.Environment.getExternalStorageDirectory;
import static java.lang.Integer.parseInt;
import static java.lang.Math.random;
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.AdditionalMatchers.aryEq;
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

/**
 * @see File#observe(LinkOption, Observer)
 */
public final class LocalObservableTest extends FileBaseTest {

    @Test
    public void no_observe_on_procfs() throws Exception {

        Closer closer = Closer.create();
        try {

            Tracker tracker = closer.register(registerMockTracker());
            Recorder observer = closer.register(observe(
                    LocalFile.of("/proc/self"),
                    FOLLOW,
                    false
            ));
            assertTrue(observer.isClosed());
            verifyZeroInteractions(tracker);

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Test
    public void observe_on_regular_file() throws Exception {
        LocalFile file = dir1().resolve("file").createFile();
        Closer closer = Closer.create();
        try {
            Recorder observer = closer.register(observe(file, NOFOLLOW));
            observer.awaitModifyByAppend(file, "hello");
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Test
    public void observe_on_link() throws Exception {
        LocalFile file = dir1().resolve("link").createLink(dir2());
        Closer closer = Closer.create();
        try {
            Recorder observer = closer.register(observe(file, NOFOLLOW));
            observer.awaitModifyBySetLastModifiedTime(file, EPOCH);
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Test
    public void release_watch_when_dir_moves_out() throws Exception {

        LocalFile src = dir1().resolve("src").createDir();
        LocalFile dst = dir2().resolve("dst");

        Closer closer = Closer.create();
        try {

            Tracker tracker = closer.register(registerMockTracker());
            Recorder observer = closer.register(observe(dir1(), NOFOLLOW));

            observer.awaitModify(src, newCreateFile(src.resolve("b")));
            observer.awaitMove(src, dst);
            observer.awaitNoEvent(newCreateFile(dst.resolve("c")));

            ArgumentCaptor<Integer> fd = ArgumentCaptor.forClass(Integer.class);
            ArgumentCaptor<Integer> wd = ArgumentCaptor.forClass(Integer.class);
            verify(tracker).onWatchAdded(fd.capture(), aryEq(src.path().bytes()), anyInt(), wd.capture());
            verify(tracker).onWatchRemoved(fd.getValue(), wd.getValue());

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Test
    public void notifies_observer_on_max_user_instances_reached() throws Exception {
        int maxUserInstances = maxUserInstances();
        List<Observation> observations = new ArrayList<>(maxUserInstances);
        try {

            for (int i = 1; i < maxUserInstances + 10; i++) {
                LocalFile child = dir1().resolve(String.valueOf(i)).createFile();
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

    @Test
    public void notifies_observer_on_max_user_watches_reached_on_observe() throws Exception {

        LocalFile dir = linkToMaxUserWatchesTestDir();
        int maxUserWatches = maxUserWatches();
        int expectedCount = maxUserWatches + 10;
        ensureExactNumberOfChildDirs(dir, expectedCount);

        FileConsumer consumer = mock(FileConsumer.class);
        Observer observer = mock(Observer.class);

        Closer closer = Closer.create();
        try {

            Tracker tracker = closer.register(registerMockTracker());
            closer.register(dir.observe(FOLLOW, observer, consumer));
            verify(observer, atLeastOnce()).onIncompleteObservation();
            verify(consumer, times(expectedCount)).accept(notNull(LocalFile.class));
            verifyAllWatchesRemovedAndRootWatchAddedOnMaxUserWatchesReached(tracker, maxUserWatches);

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Test
    public void notifies_observer_on_max_user_watches_reached_during_observe() throws Exception {

        LocalFile dir = linkToMaxUserWatchesTestDir();
        int maxUserWatches = maxUserWatches();
        int expectedCount = maxUserWatches - 10;
        ensureExactNumberOfChildDirs(dir, expectedCount);

        FileConsumer consumer = mock(FileConsumer.class);
        Observer observer = mock(Observer.class);

        Closer closer = Closer.create();
        try {

            Tracker tracker = closer.register(registerMockTracker());
            Observation observation = closer.register(dir.observe(FOLLOW, observer, consumer));
            assertFalse(observation.isClosed());
            for (int i = 0; i < 20; i++) {
                createRandomChildDir(dir);
            }

            verify(observer, timeout(1000000).atLeastOnce()).onIncompleteObservation();
            verify(consumer, times(expectedCount)).accept(notNull(LocalFile.class));
            verifyAllWatchesRemovedAndRootWatchAddedOnMaxUserWatchesReached(tracker, maxUserWatches);

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    private void createRandomChildDir(LocalFile dir) throws IOException {
        while (true) {
            try {
                dir.resolve(String.valueOf(random())).createDir();
                break;
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

        order.verify(tracker, times(maxUserWatches)).onWatchRemoved(
                eq(fd.getValue()),
                wdsRemoved.capture()
        );

        assertValuesEqual(wdsAdded, wdsRemoved);

        order.verify(tracker).onWatchAdded(
                eq(fd.getValue()),
                any(byte[].class),
                anyInt(),
                anyInt()
        );
        order.verifyNoMoreInteractions();

    }

    private static <T extends Comparable<T>> void assertValuesEqual(
            ArgumentCaptor<T> captor1,
            ArgumentCaptor<T> captor2) {

        List<T> xs = new ArrayList<>(captor1.getAllValues());
        List<T> ys = new ArrayList<>(captor2.getAllValues());
        Collections.sort(xs);
        Collections.sort(ys);
        assertEquals(xs, ys);
    }

    private LocalFile linkToMaxUserWatchesTestDir() throws IOException {
        return linkToExternalDir("files-test-max-user-watches-exceeded");
    }

    private void ensureExactNumberOfChildDirs(
            LocalFile dir,
            int expectedCount) throws IOException {

        int actualCount = 0;
        Closer closer = Closer.create();
        try {

            Stream<File> children = closer.register(dir.listDirs(FOLLOW));
            for (File child : children) {
                actualCount++;
                if (actualCount > expectedCount) {
                    child.deleteRecursive();
                    actualCount--;
                }
            }

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
        while (actualCount < expectedCount) {
            createRandomChildDir(dir);
            actualCount++;
        }
    }


    private LocalFile linkToExternalDir(String name) throws IOException {
        return dir1().resolve(name).createLink(
                externalStorageDir()
                        .resolve(name)
                        .createDirs()
        );
    }

    private LocalFile externalStorageDir() {
        return LocalFile.of(getExternalStorageDirectory().getPath());
    }

    @Test
    public void releases_all_watches_on_close() throws Exception {

        LocalFile a = dir1().resolve("a").createDir();
        LocalFile b = dir1().resolve("b").createDir();
        Closer closer = Closer.create();
        try {

            Tracker tracker = closer.register(registerMockTracker());
            dir1().observe(NOFOLLOW, mock(Observer.class)).close();

            ArgumentCaptor<Integer> fd = ArgumentCaptor.forClass(Integer.class);
            ArgumentCaptor<Integer> wd = ArgumentCaptor.forClass(Integer.class);
            verify(tracker).onInit(fd.capture());
            verify(tracker).onWatchAdded(
                    eq(fd.getValue()),
                    aryEq(a.path().bytes()),
                    anyInt(),
                    wd.capture()
            );
            verify(tracker).onWatchAdded(
                    eq(fd.getValue()),
                    aryEq(b.path().bytes()),
                    anyInt(),
                    wd.capture()
            );
            verify(tracker).onWatchRemoved(fd.getValue(), wd.getAllValues().get(0));
            verify(tracker).onWatchRemoved(fd.getValue(), wd.getAllValues().get(1));
            verify(tracker).onClose(fd.getValue());

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Test
    public void releases_fd_on_close() throws Exception {
        ArgumentCaptor<Integer> fd = ArgumentCaptor.forClass(Integer.class);
        Closer closer = Closer.create();
        try {
            Tracker tracker = closer.register(registerMockTracker());
            dir1().observe(NOFOLLOW, mock(Observer.class)).close();
            verify(tracker).onInit(fd.capture());
            verify(tracker).onClose(fd.getValue());
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    private int maxUserInstances() throws IOException {
        LocalFile limitFile = LocalFile.of("/proc/sys/fs/inotify/max_user_instances");
        return parseInt(limitFile.readAllUtf8().trim());
    }

    private int maxUserWatches() throws IOException {
        LocalFile limitFile = LocalFile.of("/proc/sys/fs/inotify/max_user_watches");
        return parseInt(limitFile.readAllUtf8().trim());
    }

    @Test
    public void observe_on_link_no_follow() throws Exception {

        LocalFile dir = dir1().resolve("dir").createDir();
        LocalFile link = dir1().resolve("link").createLink(dir);
        LocalFile file = link.resolve("file");
        Closer closer = Closer.create();
        try {

            Recorder observer = closer.register(observe(link, NOFOLLOW));
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

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Test
    public void observe_on_link_follow() throws Exception {

        LocalFile dir = dir1().resolve("dir").createDir();
        LocalFile link = dir1().resolve("link").createLink(dir);
        LocalFile child = link.resolve("dir").createDir();
        Closer closer = Closer.create();
        try {

            Recorder observer = closer.register(observe(link, FOLLOW));
            observer.awaitModifyByCreateFile(child, "a");

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
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
        LocalFile src = dir1().resolve("a").createDir();
        LocalFile dst = dir1().resolve("b");
        Closer closer = Closer.create();
        try {
            Recorder observer = closer.register(observe(dir1()));
            observer.awaitModifyByCreateFile(src, "1");
            observer.awaitMove(src, dst);
            observer.awaitModifyByCreateFile(dst, "2");
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
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
        LocalFile extra = dir2().resolve("hello").createFile();
        testMoveDirIn(new PostActions().awaitMoveIn(extra));
    }

    @Test
    public void move_dir_in_then_move_file_out_of_it() throws Exception {

        LocalFile src = dir2().resolve("a").createDir();
        LocalFile dir = dir1().resolve("a");
        LocalFile child = dir.resolve("b");
        Closer closer = Closer.create();
        try {
            Recorder observer = closer.register(observe(dir1()));
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
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Test
    public void move_file_in() throws Exception {

        LocalFile src = dir2().resolve("a").createFile();
        LocalFile dst = dir1().resolve("b");
        Closer closer = Closer.create();
        try {

            Recorder observer = closer.register(observe(dir1()));
            observer.awaitMove(src, dst);

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Test
    public void move_file_out() throws Exception {

        LocalFile file = dir1().resolve("a").createFile();
        Closer closer = Closer.create();
        try {

            Recorder observer = closer.register(observe(dir1()));
            observer.awaitMove(file, dir2().resolve("a"));

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Test
    public void move_self_out() throws Exception {
        LocalFile file = dir1().resolve("file").createFile();
        LocalFile dir = dir1().resolve("dir").createDir();
        testMoveSelfOut(file, dir2().resolve("a"));
        testMoveSelfOut(dir, dir2().resolve("b"));
    }

    private static void testMoveSelfOut(
            LocalFile src,
            LocalFile dst) throws Exception {

        Closer closer = Closer.create();
        try {

            Recorder observer = closer.register(observe(src));
            observer.awaitMove(src, dst);

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Test
    public void modify_file_content() throws Exception {
        LocalFile file = dir1().resolve("a").createFile();
        testModifyFileContent(file, file);
        testModifyFileContent(file, dir1());
    }

    private static void testModifyFileContent(
            LocalFile file,
            LocalFile observable) throws Exception {

        Closer closer = Closer.create();
        try {

            Recorder observer = closer.register(observe(observable));
            observer.awaitModifyByAppend(file, "abc");

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Test
    public void modify_permissions() throws Exception {
        LocalFile file = dir1().resolve("file").createFile();
        LocalFile dir = dir1().resolve("dir").createDir();
        testModifyPermission(file, file);
        testModifyPermission(file, dir1());
        testModifyPermission(dir, dir);
        testModifyPermission(dir, dir1());
    }

    private static void testModifyPermission(
            LocalFile target,
            LocalFile observable) throws Exception {

        Set<Permission> oldPerms = target.stat(NOFOLLOW).permissions();
        Set<Permission> newPerms = EnumSet.copyOf(oldPerms);

        if (newPerms.equals(oldPerms)) {
            newPerms.addAll(Permission.write());
        }

        if (newPerms.equals(oldPerms)) {
            newPerms.removeAll(Permission.write());
        }

        Closer closer = Closer.create();
        try {

            Recorder observer = closer.register(observe(observable));
            observer.awaitModify(target, newSetPermissions(target, newPerms));

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Test
    public void modify_mtime() throws Exception {
        LocalFile file = dir1().resolve("file").createFile();
        LocalFile dir = dir1().resolve("dir").createDir();
        testModifyLastModifiedTime(file, file);
        testModifyLastModifiedTime(file, dir1());
        testModifyLastModifiedTime(dir, dir);
        testModifyLastModifiedTime(dir, dir1());
    }

    private void testModifyLastModifiedTime(
            LocalFile target,
            LocalFile observable) throws Exception {

        Instant old = target.stat(NOFOLLOW).lastModifiedTime();
        Instant t = Instant.of(old.seconds() - 1, old.nanos());
        Closer closer = Closer.create();
        try {

            Recorder observer = closer.register(observe(observable));
            observer.awaitModifyBySetLastModifiedTime(target, t);

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Test
    public void delete() throws Exception {
        LocalFile file = dir1().resolve("file");
        LocalFile dir = dir1().resolve("dir");
        testDelete(file.createFile(), file);
        testDelete(file.createFile(), dir1());
        testDelete(dir.createDir(), dir);
        testDelete(dir.createDir(), dir1());
    }

    private static void testDelete(LocalFile target, LocalFile observable) throws Exception {
        boolean file = target.stat(NOFOLLOW).isRegularFile();
        Closer closer = Closer.create();
        try {

            Recorder observer = closer.register(observe(observable));
            List<WatchEvent> expected = new ArrayList<>();
            // If target is file and observing on the file itself, an IN_ATTRIB
            // event is first sent in addition to IN_DELETE when deleting
            if (file && target.equals(observable)) {
                expected.add(event(MODIFY, target));
            }
            expected.add(event(DELETE, target));
            observer.await(expected, newDelete(target));

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Test
    public void delete_recreate_dir_will_be_observed() throws Exception {
        LocalFile dir = dir1().resolve("dir");
        LocalFile file = dir.resolve("file");
        Closer closer = Closer.create();
        try {
            Recorder observer = closer.register(observe(dir1()));
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
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Test
    public void create() throws Exception {
        LocalFile file = dir1().resolve("file");
        LocalFile dir = dir1().resolve("dir");
        LocalFile link = dir1().resolve("link");
        testCreateFile(file, dir1());
        testCreateDir(dir, dir1());
        testCreateLink(link, dir1(), dir1());
    }

    private static void testCreateFile(
            LocalFile target,
            LocalFile observable) throws Exception {

        Closer closer = Closer.create();
        try {
            Recorder observer = closer.register(observe(observable));
            observer.awaitCreateFile(target);
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    private static void testCreateDir(
            LocalFile target,
            LocalFile observable) throws Exception {

        Closer closer = Closer.create();
        try {
            Recorder observer = closer.register(observe(observable));
            observer.awaitCreateDir(target);
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    private static void testCreateLink(
            LocalFile link,
            LocalFile target,
            LocalFile observable) throws Exception {

        Closer closer = Closer.create();
        try {
            Recorder observer = closer.register(observe(observable));
            observer.awaitCreateLink(link, target);
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Test
    public void observe_unreadable_child_dir_will_notify_incomplete_observation()
            throws Exception {

        LocalFile dir = dir1().resolve("dir").createDir();
        dir.removePermissions(Permission.read());
        Closer closer = Closer.create();
        try {
            Recorder observer = closer.register(observe(dir1()));
            observer.awaitOnIncompleteObservation();
            observer.awaitCreateFile(dir1().resolve("parent watch still works"));
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Test
    public void create_dir_then_make_it_unreadable() throws Exception {
        LocalFile dir = dir1().resolve("dir");
        Closer closer = Closer.create();
        try {

            Recorder observer = closer.register(observe(dir1()));
            observer.awaitCreateDir(dir);
            observer.awaitModifyBySetPermissions(
                    dir,
                    EnumSet.of(OWNER_WRITE, OWNER_EXECUTE)
            );
            observer.awaitModifyByCreateFile(dir, "a");

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Test
    public void create_dir_then_create_items_into_it() throws Exception {
        LocalFile dir = dir1().resolve("dir");
        Closer closer = Closer.create();
        try {
            Recorder observer = closer.register(observe(dir1()));
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
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Test
    public void create_dir_then_delete_items_from_it() throws Exception {
        LocalFile parent = dir1().resolve("parent");
        LocalFile file = parent.resolve("file");
        LocalFile dir = parent.resolve("dir");
        Closer closer = Closer.create();
        try {
            Recorder observer = closer.register(observe(dir1()));
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
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Test
    public void create_dir_then_move_items_out_of_it() throws Exception {
        LocalFile parent = dir1().resolve("parent");
        LocalFile file = parent.resolve("file");
        LocalFile dir = parent.resolve("dir");
        Closer closer = Closer.create();
        try {
            Recorder observer = closer.register(observe(dir1()));
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
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Test
    public void create_dir_then_move_file_into_it() throws Exception {
        LocalFile parent = dir1().resolve("parent");
        LocalFile file = dir2().resolve("file").createFile();
        LocalFile dir = dir2().resolve("dir").createDir();
        Closer closer = Closer.create();
        try {
            Recorder observer = closer.register(observe(dir1()));
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
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Test
    public void multiple_operations() throws Exception {
        LocalFile a = dir1().resolve("a");
        LocalFile b = dir1().resolve("b");
        LocalFile c = dir1().resolve("c");
        LocalFile d = dir1().resolve("d");
        Closer closer = Closer.create();
        try {

            Recorder observer = closer.register(observe(dir1()));
            observer.awaitCreateDir(a);
            observer.awaitCreateDir(b);
            observer.awaitModify(a, newCreateFile(a.resolve("1")));
            observer.awaitMove(dir2().resolve("c").createFile(), c);
            observer.awaitMove(c, dir2().resolve("2"));
            observer.awaitDelete(b);
            observer.awaitCreateFile(d);

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    private static WatchEvent event(Event kind, LocalFile file) {
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

    private static Callable<Void> newMove(final LocalFile src, final LocalFile dst) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                src.moveTo(dst);
                return null;
            }
        };
    }

    private static Callable<Void> newDelete(final LocalFile file) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                file.delete();
                return null;
            }
        };
    }

    private static Callable<Void> newCreateFile(final LocalFile file) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                file.createFile();
                return null;
            }
        };
    }

    private static Callable<Void> newCreateDir(final LocalFile dir) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                dir.createDir();
                return null;
            }
        };
    }

    private static Callable<Void> newCreateLink(
            final LocalFile link,
            final LocalFile target) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                link.createLink(target);
                return null;
            }
        };
    }

    private static Callable<Void> newAppend(
            final LocalFile file,
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
            final LocalFile file,
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
            final LocalFile file,
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

    static class Recorder extends Tracker implements Observer {

        private final LocalFile root;

        private final Observer observer = mock(Observer.class);

        private Observation observation;
        private int fd;
        private int wd;
        private final Map<File, Integer> allChildWds = new ConcurrentHashMap<>();
        private final Set<Integer> validChildWds = new CopyOnWriteArraySet<>();

        private final List<WatchEvent> expected = new CopyOnWriteArrayList<>();
        private final List<WatchEvent> actual = new CopyOnWriteArrayList<>();
        private volatile CountDownLatch success;

        Recorder(LocalFile root) {
            this.root = root;
        }

        static Recorder observe(LocalFile observable) throws Exception {
            return observe(observable, NOFOLLOW);
        }

        static Recorder observe(LocalFile file, LinkOption option) throws Exception {
            return observe(file, option, true);
        }

        static Recorder observe(
                LocalFile file,
                LinkOption option,
                boolean verifyTracker) throws Exception {

            Closer closer = Closer.create();
            try {

                Tracker tracker = closer.register(registerMockTracker());
                Recorder observer = new Recorder(file);
                observer.observation = file.observe(option, observer);
                if (verifyTracker) {
                    assertFalse(observer.observation.isClosed());
                    verifyTracker(observer, tracker, file, option);
                }

                Inotify.get().registerTracker(observer);
                return observer;

            } catch (Throwable e) {
                throw closer.rethrow(e);
            } finally {
                closer.close();
            }
        }

        private static void verifyTracker(
                Recorder observer,
                Tracker tracker,
                LocalFile file,
                LinkOption option) throws IOException {

            ArgumentCaptor<Integer> fd = ArgumentCaptor.forClass(Integer.class);
            ArgumentCaptor<Integer> wd = ArgumentCaptor.forClass(Integer.class);
            InOrder order = inOrder(tracker);
            order.verify(tracker).onInit(fd.capture());
            order.verify(tracker).onWatchAdded(
                    eq(fd.getValue()),
                    aryEq(file.path().bytes()),
                    anyInt(),
                    wd.capture()
            );

            observer.fd = fd.getValue();
            observer.wd = wd.getValue();

            if (file.stat(option).isDirectory()) {
                Closer closer = Closer.create();
                try {
                    Stream<File> dirs = closer.register(file.listDirs(option));
                    for (File dir : dirs) {
                        if (dir.isReadable()) {
                            order.verify(tracker).onWatchAdded(
                                    eq(fd.getValue()),
                                    aryEq(((LocalFile) dir).path().bytes()),
                                    anyInt(),
                                    wd.capture()
                            );
                        }
                        observer.allChildWds.put(dir, wd.getValue());
                        observer.validChildWds.add(wd.getValue());
                    }
                } catch (Throwable e) {
                    throw closer.rethrow(e);
                } finally {
                    closer.close();
                }
            }

            order.verifyNoMoreInteractions();

        }

        @Override
        public void onWatchAdded(int fd, byte[] path, int mask, int wd) {
            super.onWatchAdded(fd, path, mask, wd);
            if (this.fd == fd) {
                this.allChildWds.put(LocalFile.of(LocalPath.of(path)), wd);
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
            LocalFile target = child == null ? root : root.resolve(child);
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

        void await(Event kind, LocalFile file, Callable<?> action) throws Exception {
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

        void awaitCreateFile(LocalFile target) throws Exception {
            Closer closer = Closer.create();
            try {
                Tracker tracker = closer.register(registerMockTracker());
                await(CREATE, target, newCreateFile(target));
                verifyZeroInteractions(tracker);
            } catch (Throwable e) {
                throw closer.rethrow(e);
            } finally {
                closer.close();
            }
        }

        void awaitCreateDir(LocalFile target) throws Exception {
            Closer closer = Closer.create();
            try {
                Tracker tracker = closer.register(registerMockTracker());
                await(CREATE, target, newCreateDir(target));
                verify(tracker).onWatchAdded(
                        eq(fd),
                        aryEq(target.path().bytes()),
                        anyInt(),
                        anyInt()
                );
            } catch (Throwable e) {
                throw closer.rethrow(e);
            } finally {
                closer.close();
            }
        }

        void awaitCreateLink(LocalFile link, LocalFile target) throws Exception {
            Closer closer = Closer.create();
            try {
                Tracker tracker = closer.register(registerMockTracker());
                await(CREATE, link, newCreateLink(link, target));
                verifyZeroInteractions(tracker);
            } catch (Throwable e) {
                throw closer.rethrow(e);
            } finally {
                closer.close();
            }
        }

        void awaitDelete(LocalFile target) throws Exception {
            await(DELETE, target, newDelete(target));
        }

        void awaitMove(LocalFile src, LocalFile dst) throws Exception {
            Closer closer = Closer.create();
            try {
                Tracker tracker = closer.register(registerMockTracker());

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
            } catch (Throwable e) {
                throw closer.rethrow(e);
            } finally {
                closer.close();
            }
        }

        private void awaitMoveWithinSameDir(
                Tracker tracker,
                LocalFile src,
                LocalFile dst) throws Exception {

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
                        aryEq(dst.path().bytes()),
                        anyInt(),
                        anyInt()
                );
            }
            order.verifyNoMoreInteractions();
        }

        private void awaitMoveFrom(Tracker tracker, LocalFile src, LocalFile dst) throws Exception {
            boolean srcIsDir = src.stat(NOFOLLOW).isDirectory();
            await(DELETE, src, newMove(src, dst));
            if (srcIsDir) {
                verify(tracker).onWatchRemoved(fd, allChildWds.get(src));
            }
            verifyNoMoreInteractions(tracker);
        }

        private void awaitMoveTo(Tracker tracker, LocalFile src, LocalFile dst) throws Exception {
            boolean srcIsDir = src.stat(NOFOLLOW).isDirectory();
            boolean readable = src.isReadable();
            await(CREATE, dst, newMove(src, dst));

            if (srcIsDir) {
                if (readable) {
                    verify(tracker).onWatchAdded(
                            eq(fd),
                            aryEq(dst.path().bytes()),
                            anyInt(),
                            anyInt()
                    );
                    verify(observer, never()).onIncompleteObservation();
                } else {
                    verify(tracker, never()).onWatchAdded(
                            eq(fd),
                            aryEq(dst.path().bytes()),
                            anyInt(),
                            anyInt()
                    );
                    verify(observer).onIncompleteObservation();
                }
            } else {
                verify(tracker, never()).onWatchAdded(
                        eq(fd),
                        aryEq(dst.path().bytes()),
                        anyInt(),
                        anyInt()
                );
                verify(observer, never()).onIncompleteObservation();
            }
            verifyNoMoreInteractions(tracker);
        }

        private void awaitMoveSelf(Tracker tracker, LocalFile dst) throws Exception {
            await(DELETE, root, newMove(root, dst));
            for (int wd : validChildWds) {
                verify(tracker).onWatchRemoved(fd, wd);
            }
            verify(tracker).onWatchRemoved(fd, wd);
            verify(tracker).onClose(fd);
            verifyNoMoreInteractions(tracker);
        }

        void awaitModify(LocalFile target, Callable<Void> action) throws Exception {
            Closer closer = Closer.create();
            try {
                Tracker tracker = closer.register(registerMockTracker());
                await(MODIFY, target, action);
                verifyZeroInteractions(tracker);
            } catch (Throwable e) {
                throw closer.rethrow(e);
            } finally {
                closer.close();
            }
        }

        void awaitModifyByCreateFile(LocalFile target, String child) throws Exception {
            awaitModify(target, newCreateFile(target.resolve(child)));
        }

        void awaitModifyByCreateDir(LocalFile target, String child) throws Exception {
            awaitModify(target, newCreateDir(target.resolve(child)));
        }

        void awaitModifyBySetPermissions(LocalFile target, Set<Permission> perms) throws Exception {
            awaitModify(target, newSetPermissions(target, perms));
        }

        void awaitModifyByDelete(LocalFile target, String child) throws Exception {
            awaitModify(target, newDelete(target.resolve(child)));
        }

        void awaitModifyBySetLastModifiedTime(LocalFile target, Instant time) throws Exception {
            awaitModify(target, newSetLastModifiedTime(target, NOFOLLOW, time));
        }

        void awaitModifyByAppend(LocalFile target, CharSequence content) throws Exception {
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

    public static abstract class Tracker implements Inotify.Tracker, Closeable {

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

    @AutoValue
    static abstract class WatchEvent {
        abstract Event kind();

        abstract LocalFile resource();

        static WatchEvent create(Event kind, LocalFile file) {
            return new AutoValue_LocalObservableTest_WatchEvent(
                    kind, file
            );
        }
    }

    private interface PreAction {
        void action(LocalFile src) throws Exception;
    }

    private interface PostAction {
        void action(LocalFile dst, Recorder observer) throws Exception;
    }

    private static final class PreActions implements PreAction {

        private final List<PreAction> moves = new ArrayList<>();

        @Override
        public void action(LocalFile src) throws Exception {
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
                public void action(LocalFile src) throws Exception {
                    src.removePermissions(Permission.read());
                }
            });
        }

        PreActions createFile(final String name) {
            return add(new PreAction() {
                @Override
                public void action(LocalFile src) throws Exception {
                    src.resolve(name).createFile();
                }
            });
        }

    }

    private static final class PostActions implements PostAction {

        private final List<PostAction> moves = new ArrayList<>();

        @Override
        public void action(LocalFile dst, Recorder observer) throws Exception {
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
                public void action(LocalFile dst, Recorder observer) throws Exception {
                    observer.awaitCreateFile(dst.parent().resolve(name));
                }
            });
        }

        PostActions awaitDelete(final String name) {
            return add(new PostAction() {
                @Override
                public void action(LocalFile dst, Recorder observer) throws Exception {
                    observer.awaitModifyByDelete(dst, name);
                }
            });
        }

        PostActions awaitRemoveAllPermissions() {
            return add(new PostAction() {
                @Override
                public void action(LocalFile dst, Recorder observer) throws Exception {
                    observer.awaitModifyBySetPermissions(dst, Permission.none());
                }
            });
        }

        PostActions awaitCreateDir(final String name) {
            return add(new PostAction() {
                @Override
                public void action(LocalFile dst, Recorder observer) throws Exception {
                    observer.awaitModifyByCreateDir(dst, name);
                }
            });
        }

        PostActions awaitMoveIn(final LocalFile src) {
            return add(new PostAction() {
                @Override
                public void action(LocalFile dst, Recorder observer) throws Exception {
                    observer.awaitMove(src, dst.resolve(src.name()));
                }
            });
        }

        PostActions awaitOnIncompleteObservation() {
            return add(new PostAction() {
                @Override
                public void action(LocalFile dst, Recorder observer) throws Exception {
                    observer.awaitOnIncompleteObservation();
                }
            });
        }

    }

    private void testMoveDirIn(PostAction post) throws Exception {
        testMoveDirIn(new PreActions(), post);
    }

    private void testMoveDirIn(PreAction pre, PostAction post) throws Exception {
        LocalFile dst = dir1().resolve("a");
        LocalFile src = dir2().resolve("a").createDir();
        pre.action(src);
        Closer closer = Closer.create();
        try {
            Recorder observer = closer.register(observe(dir1()));
            observer.awaitMove(src, dst);
            post.action(dst, observer);
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

}
