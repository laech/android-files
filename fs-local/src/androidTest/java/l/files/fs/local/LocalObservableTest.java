package l.files.fs.local;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.net.Uri;

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
import l.files.fs.FileSystem.Consumer;
import l.files.fs.Files;
import l.files.fs.Instant;
import l.files.fs.LinkOption;
import l.files.fs.Name;
import l.files.fs.Observation;
import l.files.fs.Observer;
import l.files.fs.Path;
import l.files.fs.Paths;
import l.files.fs.Permission;
import l.files.testing.Executable;
import l.files.testing.Tests;

import static android.content.Context.DOWNLOAD_SERVICE;
import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.Environment.getExternalStorageDirectory;
import static android.os.Environment.getExternalStoragePublicDirectory;
import static java.lang.Integer.parseInt;
import static java.lang.Math.random;
import static java.lang.System.currentTimeMillis;
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
import static org.junit.Assume.assumeTrue;
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

public final class LocalObservableTest extends PathBaseTest {

    /*
     * New bug affecting Android M (API 23) inotify, meaning some events will
     * not be delivered.
     *
     * Examples:
     *
     *  - File download via DownloadManager
     *  - 'touch file' using adb shell
     *
     * Issues:
     *
     *  - https://code.google.com/p/android/issues/detail?id=189231
     *  - https://code.google.com/p/android-developer-preview/issues/detail?id=3099
     */
    @Test
    public void notifies_files_downloaded_by_download_manager() throws Exception {

        final Path downloadDir = downloadsDir();
        final Path downloadFile = downloadDir.resolve(uniqueTestName());
        final Closer closer = Closer.create();
        try {

            closer.register(new Closeable() {
                @Override
                public void close() throws IOException {
                    Files.deleteIfExists(downloadFile);
                }
            });

            Recorder observer = closer.register(observe(downloadDir));
            observer.await(CREATE, downloadFile, new Callable<Void>() {

                @Override
                public Void call() throws Exception {

                    assertFalse(Files.exists(downloadFile, NOFOLLOW));

                    downloadManager().enqueue(new Request(Uri.parse("https://www.google.com"))
                            .setDestinationUri(Uri.parse(downloadFile.toUri().toString())));

                    Tests.timeout(5, SECONDS, new Executable() {
                        @Override
                        public void execute() throws Exception {
                            assertTrue(Files.exists(downloadFile, NOFOLLOW));
                        }
                    });

                    return null;

                }

            });

        } catch (AssertionError e) {
            /*
             * Check file is downloaded but failed to receive event.
             * Use assume here because this will fail on API 23,
             * the goal of this is to create visibility of this issue but
             * not fail the build.
             */
            assertTrue(Files.exists(downloadFile, NOFOLLOW));
            assumeTrue(false);

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    private DownloadManager downloadManager() {
        return (DownloadManager) getContext().getSystemService(DOWNLOAD_SERVICE);
    }

    private Path downloadsDir() {
        return Paths.get(getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS));
    }

    private String uniqueTestName() {
        return testName.getMethodName() + "-" + currentTimeMillis();
    }

    @Test
    public void able_to_observe_the_rest_of_the_files_when_some_are_not_observable()
            throws Exception {

        List<Path> observables = new ArrayList<>();
        observables.add(createRandomChildDir(dir1()));
        observables.add(createRandomChildDir(dir1()));

        Path unobservable = Files.createDir(dir1().resolve("unobservable"));
        Files.removePermissions(unobservable, Permission.read());

        observables.add(createRandomChildDir(dir1()));
        observables.add(createRandomChildDir(dir1()));

        Closer closer = Closer.create();
        try {

            Recorder recorder = closer.register(observe(dir1(), FOLLOW));
            recorder.awaitCreateFile(dir1().resolve("1"));
            for (Path observable : observables) {
                recorder.awaitModifyByCreateFile(observable, "1");
            }
            recorder.awaitNoEvent(newCreateFile(unobservable.resolve("1")));

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Test
    public void no_observe_on_procfs() throws Exception {

        Closer closer = Closer.create();
        try {

            Tracker tracker = closer.register(registerMockTracker());
            Recorder observer = closer.register(observe(
                    Paths.get("/proc/self"),
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
        Path file = Files.createFile(dir1().resolve("file"));
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
        Path file = Files.createSymbolicLink(dir1().resolve("link"), dir2());
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

        Path src = Files.createDir(dir1().resolve("src"));
        Path dst = dir2().resolve("dst");

        Closer closer = Closer.create();
        try {

            Tracker tracker = closer.register(registerMockTracker());
            Recorder observer = closer.register(observe(dir1(), NOFOLLOW));

            observer.awaitModify(src, newCreateFile(src.resolve("b")));
            observer.awaitMove(src, dst);
            observer.awaitNoEvent(newCreateFile(dst.resolve("c")));

            ArgumentCaptor<Integer> fd = ArgumentCaptor.forClass(Integer.class);
            ArgumentCaptor<Integer> wd = ArgumentCaptor.forClass(Integer.class);
            verify(tracker).onWatchAdded(fd.capture(), aryEq(src.toByteArray()), anyInt(), wd.capture());
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
                Path child = Files.createFile(dir1().resolve(String.valueOf(i)));
                Observer observer = mock(Observer.class);
                Observation observation = Files.observe(child, NOFOLLOW, observer);
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

        Path dir = linkToMaxUserWatchesTestDir();
        int maxUserWatches = maxUserWatches();
        int expectedCount = maxUserWatches + 10;
        ensureExactNumberOfChildDirs(dir, expectedCount);

        @SuppressWarnings("unchecked")
        Consumer<Path> consumer = mock(Consumer.class);
        given(consumer.accept(any(Path.class))).willReturn(true);
        Observer observer = mock(Observer.class);

        Closer closer = Closer.create();
        try {

            Tracker tracker = closer.register(registerMockTracker());
            closer.register(Files.observe(dir, FOLLOW, observer, consumer));
            verify(observer, atLeastOnce()).onIncompleteObservation();
            verify(consumer, times(expectedCount)).accept(notNull(Path.class));
            verifyAllWatchesRemovedAndRootWatchAddedOnMaxUserWatchesReached(tracker, maxUserWatches);

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Test
    public void notifies_observer_on_max_user_watches_reached_during_observe() throws Exception {

        Path dir = linkToMaxUserWatchesTestDir();
        int maxUserWatches = maxUserWatches();
        int expectedCount = maxUserWatches - 10;
        ensureExactNumberOfChildDirs(dir, expectedCount);

        @SuppressWarnings("unchecked")
        Consumer<Path> consumer = mock(Consumer.class);
        given(consumer.accept(any(Path.class))).willReturn(true);
        Observer observer = mock(Observer.class);

        Closer closer = Closer.create();
        try {

            Tracker tracker = closer.register(registerMockTracker());
            Observation observation = closer.register(Files.observe(dir, FOLLOW, observer, consumer));
            assertFalse(observation.isClosed());
            for (int i = 0; i < 20; i++) {
                createRandomChildDir(dir);
            }

            verify(observer, timeout(1000000).atLeastOnce()).onIncompleteObservation();
            verify(consumer, times(expectedCount)).accept(notNull(Path.class));
            verifyAllWatchesRemovedAndRootWatchAddedOnMaxUserWatchesReached(tracker, maxUserWatches);

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    private Path createRandomChildDir(Path dir) throws IOException {
        while (true) {
            try {
                return Files.createDir(dir.resolve(String.valueOf(random())));
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

    private Path linkToMaxUserWatchesTestDir() throws IOException {
        return linkToExternalDir("files-test-max-user-watches-exceeded");
    }

    private void ensureExactNumberOfChildDirs(
            final Path dir,
            final int expectedCount) throws IOException {

        final int[] actualCount = {0};

        Files.listDirs(dir, FOLLOW, new Consumer<Path>() {
            @Override
            public boolean accept(Path child) throws IOException {
                actualCount[0]++;
                if (actualCount[0] > expectedCount) {
                    Files.deleteRecursive(child);
                    actualCount[0]--;
                }
                return true;
            }
        });

        while (actualCount[0] < expectedCount) {
            createRandomChildDir(dir);
            actualCount[0]++;
        }
    }


    private Path linkToExternalDir(String name) throws IOException {
        return Files.createSymbolicLink(
                dir1().resolve(name),
                Files.createDirs(externalStorageDir().resolve(name))
        );
    }

    private Path externalStorageDir() {
        return Paths.get(getExternalStorageDirectory().getPath());
    }

    @Test
    public void releases_all_watches_on_close() throws Exception {

        Path a = Files.createDir(dir1().resolve("a"));
        Path b = Files.createDir(dir1().resolve("b"));
        Closer closer = Closer.create();
        try {

            Tracker tracker = closer.register(registerMockTracker());
            Files.observe(dir1(), NOFOLLOW, mock(Observer.class)).close();

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
            Files.observe(dir1(), NOFOLLOW, mock(Observer.class)).close();
            verify(tracker).onInit(fd.capture());
            verify(tracker).onClose(fd.getValue());
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    private int maxUserInstances() throws IOException {
        Path limitFile = Paths.get("/proc/sys/fs/inotify/max_user_instances");
        return parseInt(Files.readAllUtf8(limitFile).trim());
    }

    private int maxUserWatches() throws IOException {
        Path limitFile = Paths.get("/proc/sys/fs/inotify/max_user_watches");
        return parseInt(Files.readAllUtf8(limitFile).trim());
    }

    @Test
    public void observe_on_link_no_follow() throws Exception {

        Path dir = Files.createDir(dir1().resolve("dir"));
        Path link = Files.createSymbolicLink(dir1().resolve("link"), dir);
        Path file = link.resolve("file");
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

        Path dir = Files.createDir(dir1().resolve("dir"));
        Path link = Files.createSymbolicLink(dir1().resolve("link"), dir);
        Path child = Files.createDir(link.resolve("dir"));
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
        Path src = Files.createDir(dir1().resolve("a"));
        Path dst = dir1().resolve("b");
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
        Path extra = Files.createFile(dir2().resolve("hello"));
        testMoveDirIn(new PostActions().awaitMoveIn(extra));
    }

    @Test
    public void move_dir_in_then_move_file_out_of_it() throws Exception {

        Path src = Files.createDir(dir2().resolve("a"));
        Path dir = dir1().resolve("a");
        Path child = dir.resolve("b");
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

        Path src = Files.createFile(dir2().resolve("a"));
        Path dst = dir1().resolve("b");
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

        Path file = Files.createFile(dir1().resolve("a"));
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
        Path file = Files.createFile(dir1().resolve("file"));
        Path dir = Files.createDir(dir1().resolve("dir"));
        testMoveSelfOut(file, dir2().resolve("a"));
        testMoveSelfOut(dir, dir2().resolve("b"));
    }

    private static void testMoveSelfOut(
            Path src,
            Path dst) throws Exception {

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
        Path file = Files.createFile(dir1().resolve("a"));
        testModifyFileContent(file, file);
        testModifyFileContent(file, dir1());
    }

    private static void testModifyFileContent(
            Path file,
            Path observable) throws Exception {

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
        Path file = Files.createFile(dir1().resolve("file"));
        Path dir = Files.createDir(dir1().resolve("dir"));
        testModifyPermission(file, file);
        testModifyPermission(file, dir1());
        testModifyPermission(dir, dir);
        testModifyPermission(dir, dir1());
    }

    private static void testModifyPermission(
            Path target,
            Path observable) throws Exception {

        Set<Permission> oldPerms = Files.stat(target, NOFOLLOW).permissions();
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
        Path file = Files.createFile(dir1().resolve("file"));
        Path dir = Files.createDir(dir1().resolve("dir"));
        testModifyLastModifiedTime(file, file);
        testModifyLastModifiedTime(file, dir1());
        testModifyLastModifiedTime(dir, dir);
        testModifyLastModifiedTime(dir, dir1());
    }

    private void testModifyLastModifiedTime(
            Path target,
            Path observable) throws Exception {

        Instant old = Files.stat(target, NOFOLLOW).lastModifiedTime();
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
        Path file = dir1().resolve("file");
        Path dir = dir1().resolve("dir");
        testDelete(Files.createFile(file), file);
        testDelete(Files.createFile(file), dir1());
        testDelete(Files.createDir(dir), dir);
        testDelete(Files.createDir(dir), dir1());
    }

    private static void testDelete(Path target, Path observable) throws Exception {
        boolean file = Files.stat(target, NOFOLLOW).isRegularFile();
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
        Path dir = dir1().resolve("dir");
        Path file = dir.resolve("file");
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
        Path file = dir1().resolve("file");
        Path dir = dir1().resolve("dir");
        Path link = dir1().resolve("link");
        testCreateFile(file, dir1());
        testCreateDir(dir, dir1());
        testCreateSymbolicLink(link, dir1(), dir1());
    }

    private static void testCreateFile(
            Path target,
            Path observable) throws Exception {

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
            Path target,
            Path observable) throws Exception {

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

    private static void testCreateSymbolicLink(
            Path link,
            Path target,
            Path observable) throws Exception {

        Closer closer = Closer.create();
        try {
            Recorder observer = closer.register(observe(observable));
            observer.awaitCreateSymbolicLink(link, target);
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Test
    public void observe_unreadable_child_dir_will_notify_incomplete_observation()
            throws Exception {

        Path dir = Files.createDir(dir1().resolve("dir"));
        Files.removePermissions(dir, Permission.read());
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
        Path dir = dir1().resolve("dir");
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
        Path dir = dir1().resolve("dir");
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
                            newCreateSymbolicLink(dir.resolve("link"), dir1())
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
        Path parent = dir1().resolve("parent");
        Path file = parent.resolve("file");
        Path dir = parent.resolve("dir");
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
        Path parent = dir1().resolve("parent");
        Path file = parent.resolve("file");
        Path dir = parent.resolve("dir");
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
        Path parent = dir1().resolve("parent");
        Path file = Files.createFile(dir2().resolve("file"));
        Path dir = Files.createDir(dir2().resolve("dir"));
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
        Path a = dir1().resolve("a");
        Path b = dir1().resolve("b");
        Path c = dir1().resolve("c");
        Path d = dir1().resolve("d");
        Closer closer = Closer.create();
        try {

            Recorder observer = closer.register(observe(dir1()));
            observer.awaitCreateDir(a);
            observer.awaitCreateDir(b);
            observer.awaitModify(a, newCreateFile(a.resolve("1")));
            observer.awaitMove(Files.createFile(dir2().resolve("c")), c);
            observer.awaitMove(c, dir2().resolve("2"));
            observer.awaitDelete(b);
            observer.awaitCreateFile(d);

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
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
                Files.move(src, dst);
                return null;
            }
        };
    }

    private static Callable<Void> newDelete(final Path file) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Files.delete(file);
                return null;
            }
        };
    }

    private static Callable<Void> newCreateFile(final Path file) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Files.createFile(file);
                return null;
            }
        };
    }

    private static Callable<Void> newCreateDir(final Path dir) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Files.createDir(dir);
                return null;
            }
        };
    }

    private static Callable<Void> newCreateSymbolicLink(
            final Path link,
            final Path target) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Files.createSymbolicLink(link, target);
                return null;
            }
        };
    }

    private static Callable<Void> newAppend(
            final Path file,
            final CharSequence content) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Files.writeUtf8(file, content);
                return null;
            }
        };
    }

    private static Callable<Void> newSetPermissions(
            final Path file,
            final Set<Permission> permissions) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Files.setPermissions(file, permissions);
                return null;
            }
        };
    }

    private static Callable<Void> newSetLastModifiedTime(
            final Path file,
            final LinkOption option,
            final Instant instant) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Files.setLastModifiedTime(file, option, instant);
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
                boolean verifyTracker) throws Exception {

            Closer closer = Closer.create();
            try {

                Tracker tracker = closer.register(registerMockTracker());
                Recorder observer = new Recorder(file);
                observer.observation = Files.observe(file, option, observer);
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
                final Recorder observer,
                final Tracker tracker,
                final Path file,
                final LinkOption option) throws IOException {

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

            if (Files.stat(file, option).isDirectory()) {

                Files.listDirs(file, option, new Consumer<Path>() {
                    @Override
                    public boolean accept(Path dir) throws IOException {
                        if (Files.isReadable(dir)) {
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
                this.allChildWds.put(Paths.get(path), wd);
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
            Path target = child == null ? root : root.resolve(child);
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

        void awaitCreateDir(Path target) throws Exception {
            Closer closer = Closer.create();
            try {
                Tracker tracker = closer.register(registerMockTracker());
                await(CREATE, target, newCreateDir(target));
                verify(tracker).onWatchAdded(
                        eq(fd),
                        aryEq(target.toByteArray()),
                        anyInt(),
                        anyInt()
                );
            } catch (Throwable e) {
                throw closer.rethrow(e);
            } finally {
                closer.close();
            }
        }

        void awaitCreateSymbolicLink(Path link, Path target) throws Exception {
            Closer closer = Closer.create();
            try {
                Tracker tracker = closer.register(registerMockTracker());
                await(CREATE, link, newCreateSymbolicLink(link, target));
                verifyZeroInteractions(tracker);
            } catch (Throwable e) {
                throw closer.rethrow(e);
            } finally {
                closer.close();
            }
        }

        void awaitDelete(Path target) throws Exception {
            await(DELETE, target, newDelete(target));
        }

        void awaitMove(Path src, Path dst) throws Exception {
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
                Path src,
                Path dst) throws Exception {

            boolean isDir = Files.stat(src, NOFOLLOW).isDirectory();
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
            boolean srcIsDir = Files.stat(src, NOFOLLOW).isDirectory();
            await(DELETE, src, newMove(src, dst));
            if (srcIsDir) {
                verify(tracker).onWatchRemoved(fd, allChildWds.get(src));
            }
            verifyNoMoreInteractions(tracker);
        }

        private void awaitMoveTo(Tracker tracker, Path src, Path dst) throws Exception {
            boolean srcIsDir = Files.stat(src, NOFOLLOW).isDirectory();
            boolean readable = Files.isReadable(src);
            await(CREATE, dst, newMove(src, dst));

            if (srcIsDir) {
                if (readable) {
                    verify(tracker).onWatchAdded(
                            eq(fd),
                            aryEq(dst.toByteArray()),
                            anyInt(),
                            anyInt()
                    );
                    verify(observer, never()).onIncompleteObservation();
                } else {
                    verify(tracker, never()).onWatchAdded(
                            eq(fd),
                            aryEq(dst.toByteArray()),
                            anyInt(),
                            anyInt()
                    );
                    verify(observer).onIncompleteObservation();
                }
            } else {
                verify(tracker, never()).onWatchAdded(
                        eq(fd),
                        aryEq(dst.toByteArray()),
                        anyInt(),
                        anyInt()
                );
                verify(observer, never()).onIncompleteObservation();
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

        void awaitModifyByCreateFile(Path target, String child) throws Exception {
            awaitModify(target, newCreateFile(target.resolve(child)));
        }

        void awaitModifyByCreateDir(Path target, String child) throws Exception {
            awaitModify(target, newCreateDir(target.resolve(child)));
        }

        void awaitModifyBySetPermissions(Path target, Set<Permission> perms) throws Exception {
            awaitModify(target, newSetPermissions(target, perms));
        }

        void awaitModifyByDelete(Path target, String child) throws Exception {
            awaitModify(target, newDelete(target.resolve(child)));
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

        abstract Path resource();

        static WatchEvent create(Event kind, Path file) {
            return new AutoValue_LocalObservableTest_WatchEvent(
                    kind, file
            );
        }
    }

    private interface PreAction {
        void action(Path src) throws Exception;
    }

    private interface PostAction {
        void action(Path dst, Recorder observer) throws Exception;
    }

    private static final class PreActions implements PreAction {

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
                    Files.removePermissions(src, Permission.read());
                }
            });
        }

        PreActions createFile(final String name) {
            return add(new PreAction() {
                @Override
                public void action(Path src) throws Exception {
                    Files.createFile(src.resolve(name));
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
                    observer.awaitCreateFile(dst.parent().resolve(name));
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
                    observer.awaitMove(src, dst.resolve(src.name()));
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
        Path dst = dir1().resolve("a");
        Path src = Files.createDir(dir2().resolve("a"));
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
