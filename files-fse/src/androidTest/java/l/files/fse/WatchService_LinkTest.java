package l.files.fse;

import android.annotation.SuppressLint;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.System.nanoTime;
import static l.files.fse.WatchEvent.Kind.CREATE;
import static l.files.fse.WatchServiceBaseTest.FileType.DIR;
import static l.files.fse.WatchServiceBaseTest.FileType.FILE;
import static l.files.io.os.Stat.stat;
import static l.files.io.os.Unistd.symlink;

public class WatchService_LinkTest extends WatchServiceBaseTest {

  /**
   * These paths may all be mount points of external storage, different paths,
   * but all point to the same inode.
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
    List<File> points = findSameMountPoints();
    File a = points.get(0);
    File b = points.get(1);
    assertEquals(stat(a.getPath()).ino, stat(b.getPath()).ino);

    File[] files = {
        new File(a, "1-" + nanoTime()),
        new File(b, "2-" + nanoTime()),
        new File(a, "3-" + nanoTime()),
        new File(b, "4-" + nanoTime()),
    };

    try {
      for (File file : files) {
        await(event(CREATE, file), newCreate(file, FILE), listen(file.getParentFile()));
      }
    } finally {
      for (File file : files) {
        assertTrue(file.delete());
      }
    }
  }

  private List<File> findSameMountPoints() {
    List<File> points = newArrayList(MOUNT_POINTS);
    for (Iterator<File> it = points.iterator(); it.hasNext(); ) {
      if (!it.next().exists()) {
        it.remove();
      }
    }
    if (points.size() < 2) {
      fail("Not enough mount points with same inode for testing.");
    }
    return points;
  }

  public void testSymlinksPointingToSameDir() throws Exception {
    String path1 = tmp().get().getPath();
    String path2 = helper().get("test").getPath();
    symlink(path1, path2);

    tmp().createFile("a");
    assertTrue(tmp().get("a").exists());
    assertTrue(helper().get("test/a").exists());

    await(event(CREATE, "b"), newCreate("b", FILE));

    helper().createFile("test/c");
    assertTrue(tmp().get("c").exists());
    await(event(CREATE, "d"), newCreate("d", DIR));
  }
}
