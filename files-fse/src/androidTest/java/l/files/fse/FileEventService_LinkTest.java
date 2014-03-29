package l.files.fse;

import android.annotation.SuppressLint;

import java.io.File;

import static java.lang.System.nanoTime;
import static l.files.os.Stat.stat;
import static l.files.os.Unistd.symlink;

public class FileEventService_LinkTest extends FileEventServiceBaseTest {

  /**
   * These paths all mount points of external storage, different paths, but all
   * point to the same inode.
   */
  @SuppressLint("SdCardPath")
  private static final File[] MOUNT_POINTS = {
      new File("/sdcard"),
      new File("/storage/emulated/0"),
      new File("/storage/emulated/legacy"),
      new File("/storage/sdcard0"),
  };

  /**
   * Make sure the internal FileObserver instances are allocated one per inode
   * instead of one per path, because inotify operates on inodes, not paths.
   */
  public void testMountPointsOfSameInodes() throws Exception {
    File a = MOUNT_POINTS[0];
    File b = MOUNT_POINTS[1];
    assertEquals(stat(a.getPath()).ino, stat(b.getPath()).ino);

    String[] names = {
        "1-" + nanoTime(),
        "2-" + nanoTime(),
        "3-" + nanoTime(),
        "4-" + nanoTime(),
    };

    try {

      tester()
          .monitor(a)
          .monitor(b)
          .awaitCreateFile(names[0], a)
          .awaitCreateFile(names[1], b)
          .awaitCreateFile(names[2], a)
          .awaitCreateFile(names[3], b);

    } finally {
      for (String name : names) {
        assertTrue(new File(a, name).delete());
      }
    }
  }

  public void testSymlinksPointingToSameDir() throws Exception {
    String path1 = tmp().get().getPath();
    String path2 = helper().get("test").getPath();
    symlink(path1, path2);

    tmp().createFile("a");
    assertTrue(tmp().get("a").exists());
    assertTrue(helper().get("test/a").exists());

    tester().awaitCreateFile("b");

    helper().createFile("test/c");
    assertTrue(tmp().get("c").exists());
    tester().awaitCreateDir("d");
  }
}
