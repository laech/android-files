package l.files.fs.local;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import l.files.fs.Event;
import l.files.fs.File;
import l.files.fs.Instant;
import l.files.fs.LinkOption;
import l.files.fs.Observer;
import l.files.fs.Permission;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.Event.CREATE;
import static l.files.fs.Event.DELETE;
import static l.files.fs.Event.MODIFY;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Permission.OWNER_WRITE;
import static l.files.fs.local.LocalFile_observe_Test.Recorder.observe;

/**
 * @see File#observe(LinkOption, Observer)
 */
public final class LocalFile_observe_Test extends FileBaseTest {

  public void testObserveOnLinkNoFollow() throws Exception {
    File dir = dir1().resolve("dir").createDirectory();
    File link = dir1().resolve("link").createLink(dir);
    File file = link.resolve("file");
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
    File dir = dir1().resolve("dir").createDirectory();
    File link = dir1().resolve("link").createLink(dir);
    File child = link.resolve("dir").createDirectory();
    try (Recorder observer = observe(link, FOLLOW)) {
      observer.await(MODIFY, child, newCreateFile(child.resolve("a")));
    }
  }

  public void testMoveDirectoryInThenAddFileIntoIt() throws Exception {
    File dst = dir1().resolve("a");
    File src = dir2().resolve("a").createDirectory();
    try (Recorder observer = observe(dir1())) {
      observer.await(CREATE, dst, newMove(src, dst));
      observer.await(MODIFY, dst, newCreateDirectory(dst.resolve("b")));
    }
  }

  public void testMoveDirectoryInThenDeleteFileFromIt() throws Exception {
    File dstDir = dir1().resolve("a");
    File srcDir = dir2().resolve("a").createDirectory();
    srcDir.resolve("b").createFile();
    try (Recorder observer = observe(dir1())) {
      observer.await(CREATE, dstDir, newMove(srcDir, dstDir));
      observer.await(MODIFY, dstDir, newDelete(dstDir.resolve("b")));
    }
  }

  public void testMoveDirectoryInThenMoveFileIntoIt() throws Exception {
    File dir = dir1().resolve("a");
    File src1 = dir2().resolve("a").createDirectory();
    File src2 = dir2().resolve("b").createFile();
    try (Recorder observer = observe(dir1())) {
      observer.await(CREATE, dir, newMove(src1, dir));
      observer.await(MODIFY, dir, newMove(src2, dir.resolve("b")));
    }
  }

  public void testMoveDirectoryInThenMoveFileOutOfIt() throws Exception {
    File src = dir2().resolve("a").createDirectory();
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

  public void testMoveFileIn() throws Exception {
    File src = dir2().resolve("a").createFile();
    File dst = dir1().resolve("b");
    try (Recorder observer = observe(dir1())) {
      observer.await(CREATE, dst, newMove(src, dst));
    }
  }

  public void testMoveFileOut() throws Exception {
    File file = dir1().resolve("a").createFile();
    try (Recorder observer = observe(dir1())) {
      observer.await(DELETE, file, newMove(file, dir2().resolve("a")));
    }
  }

  public void testMoveSelfOut() throws Exception {
    File file = dir1().resolve("file").createFile();
    File dir = dir1().resolve("dir").createDirectory();
    testMoveSelfOut(file, dir2().resolve("a"));
    testMoveSelfOut(dir, dir2().resolve("b"));
  }

  private static void testMoveSelfOut(
      File src,
      File dst) throws Exception {
    try (Recorder observer = observe(src)) {
      observer.await(DELETE, src, newMove(src, dst));
    }
  }

  public void testModifyFileContent() throws Exception {
    File file = dir1().resolve("a").createFile();
    testModifyFileContent(file, file);
    testModifyFileContent(file, dir1());
  }

  private static void testModifyFileContent(
      File file,
      File observable) throws Exception {
    try (Recorder observer = observe(observable)) {
      observer.await(MODIFY, file, newAppend(file, "abc"));
    }
  }

  public void testModifyPermissions() throws Exception {
    File file = dir1().resolve("file").createFile();
    File dir = dir1().resolve("directory").createDirectory();
    testModifyPermission(file, file);
    testModifyPermission(file, dir1());
    testModifyPermission(dir, dir);
    testModifyPermission(dir, dir1());
  }

  private static void testModifyPermission(
      File target,
      File observable) throws Exception {
    Set<Permission> oldPerms = target.stat(NOFOLLOW).permissions();
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
    File file = dir1().resolve("file").createFile();
    File dir = dir1().resolve("dir").createDirectory();
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
      observer.await(MODIFY, target,
          newSetModificationTime(target, NOFOLLOW, t));
    }
  }

  public void testDelete() throws Exception {
    File file = dir1().resolve("file");
    File dir = dir1().resolve("dir");
    testDelete(file.createFile(), file);
    testDelete(file.createFile(), dir1());
    testDelete(dir.createDirectory(), dir);
    testDelete(dir.createDirectory(), dir1());
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

  public void testDeleteRecreateDirectoryWillBeObserved() throws Exception {
    File dir = dir1().resolve("dir");
    File file = dir.resolve("file");
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
      observer.await(CREATE, target, newCreateFile(target));
    }
  }

  private static void testCreateDirectory(
      File target,
      File observable) throws Exception {
    try (Recorder observer = observe(observable)) {
      observer.await(CREATE, target, newCreateDirectory(target));
    }
  }

  private static void testCreateSymbolicLink(
      File link,
      File target,
      File observable) throws Exception {
    try (Recorder observer = observe(observable)) {
      observer.await(CREATE, link, newCreateSymbolicLink(link, target));
    }
  }

  public void testCreateDirectoryThenCreateItemsIntoIt() throws Exception {
    File dir = dir1().resolve("dir");
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
    File parent = dir1().resolve("parent");
    File file = parent.resolve("file");
    File dir = parent.resolve("dir");
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
    File parent = dir1().resolve("parent");
    File file = parent.resolve("file");
    File dir = parent.resolve("dir");
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
    File parent = dir1().resolve("parent");
    File file = dir2().resolve("file").createFile();
    File dir = dir2().resolve("dir").createDirectory();
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
    File a = dir1().resolve("a");
    File b = dir1().resolve("b");
    File c = dir1().resolve("c");
    File d = dir1().resolve("d");
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

  private static WatchEvent event(Event kind, File file) {
    return WatchEvent.create(kind, file);
  }

  private static Callable<Void> compose(final Callable<?>... callables) {
    return new Callable<Void>() {
      @Override public Void call() throws Exception {
        for (Callable<?> callable : callables) {
          callable.call();
        }
        return null;
      }
    };
  }

  private static Callable<Void> newMove(final File src, final File dst) {
    return new Callable<Void>() {
      @Override public Void call() throws Exception {
        src.moveTo(dst);
        return null;
      }
    };
  }

  private static Callable<Void> newDelete(final File file) {
    return new Callable<Void>() {
      @Override public Void call() throws Exception {
        file.delete();
        return null;
      }
    };
  }

  private static Callable<Void> newCreateFile(final File file) {
    return new Callable<Void>() {
      @Override public Void call() throws Exception {
        file.createFile();
        return null;
      }
    };
  }

  private static Callable<Void> newCreateDirectory(final File directory) {
    return new Callable<Void>() {
      @Override public Void call() throws Exception {
        directory.createDirectory();
        return null;
      }
    };
  }

  private static Callable<Void> newCreateSymbolicLink(
      final File link,
      final File target) {
    return new Callable<Void>() {
      @Override public Void call() throws Exception {
        link.createLink(target);
        return null;
      }
    };
  }

  private static Callable<Void> newAppend(
      final File file,
      final CharSequence content) {
    return new Callable<Void>() {
      @Override public Void call() throws Exception {
        try (OutputStream out = file.output(true)) {
          out.write(content.toString().getBytes(UTF_8));
        }
        return null;
      }
    };
  }

  private static Callable<Void> newSetPermissions(
      final File file,
      final Set<Permission> permissions) {
    return new Callable<Void>() {
      @Override public Void call() throws Exception {
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
      @Override public Void call() throws Exception {
        file.setLastModifiedTime(option, instant);
        return null;
      }
    };
  }

  static class Recorder implements Observer, Closeable {

    private File file;
    private Closeable subscription;
    private volatile List<WatchEvent> expected;
    private volatile List<WatchEvent> actual;
    private volatile CountDownLatch success;

    Recorder(File file) {
      this.file = file;
    }

    static Recorder observe(File observable) throws IOException {
      return observe(observable, NOFOLLOW);
    }

    static Recorder observe(
        File observable,
        LinkOption option) throws IOException {
      Recorder observer = new Recorder(observable);
      observer.subscription = observable.observe(option, observer);
      return observer;
    }

    @Override public void close() throws IOException {
      subscription.close();
    }

    @Override public void onEvent(Event event, @Nullable String child) {
      File target = child == null
          ? file
          : file.resolve(child);
      actual.add(WatchEvent.create(event, target));
      if (expected.equals(actual)) {
        success.countDown();
      }
    }

    void await(
        Event kind,
        File file,
        Callable<?> action) throws Exception {
      await(WatchEvent.create(kind, file), action);
    }

    void await(
        WatchEvent expected,
        Callable<?> action) throws Exception {
      await(singletonList(expected), action);
    }

    void await(
        List<WatchEvent> expected,
        Callable<?> action) throws Exception {
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

  @AutoValue
  static abstract class WatchEvent {
    abstract Event kind();

    abstract File resource();

    static WatchEvent create(Event kind, File file) {
      return new AutoValue_LocalResource_observe_Test_WatchEvent(
          kind, file
      );
    }
  }
}
