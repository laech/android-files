package l.files.io.file;

import static l.files.io.file.WatchEvent.Kind.CREATE;
import static l.files.io.file.WatchEvent.Kind.MODIFY;

/**
 * Tests file system operations started with moving files/directories to the
 * monitored directory.
 *
 * @see android.os.FileObserver#MOVED_TO
 */
public class WatchService_MOVE_TO_InitiatedTest extends WatchServiceBaseTest {

  /**
   * Directory moved into the monitored directory  should be monitored for files
   * additions in that directory, as that will change the new directory's last
   * modified date.
   */
  public void testMoveDirInThenAddFileIntoIt() {
    await(event(CREATE, "a"), newMoveTo("a", helper().createDir("a")));
    await(event(MODIFY, "a"), newCreate("a/b", FileType.DIR));
  }

  /**
   * Directory moved into the monitored directory should be monitored for files
   * deletions in that directory, as that will change the new directory's last
   * modified date.
   */
  public void testMoveDirInThenDeleteFileFromIt() {
    helper().createFile("a/b");
    await(event(CREATE, "a"), newMoveTo("a", helper().get("a")));
    await(event(MODIFY, "a"), newDelete("a/b"));
  }

  /**
   * Directory moved into the monitored directory should be monitored for files
   * moving into that directory, as that will change the new directory's last
   * modified date.
   */
  public void testMoveDirInThenMoveFileIntoIt() {
    await(event(CREATE, "a"), newMoveTo("a", helper().createDir("a")));
    await(event(MODIFY, "a"), newMoveTo("a/b", helper().createFile("b")));
  }

  /**
   * Directory moved into the monitored directory should be monitored for files
   * moving out of the directory, as that will change the directory's last
   * modified date.
   */
  public void testMoveDirInThenMoveFileOutOfIt() {
    await(event(CREATE, "a"), newMoveTo("a", helper().createDir("a")));
    await(event(MODIFY, "a"), newCreate("a/b", FileType.FILE));
    await(event(MODIFY, "a"), newMoveFrom("a/b", helper().get("b")));
  }

  public void testMoveFileIn() {
    await(event(CREATE, "a"), newMoveTo("a", helper().createFile("a")));
  }
}
