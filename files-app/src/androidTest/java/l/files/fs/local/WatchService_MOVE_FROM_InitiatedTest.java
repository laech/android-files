package l.files.fs.local;

import static l.files.fs.WatchEvent.Kind.DELETE;

/**
 * Tests file system operations started with moving files/directories from the
 * monitored directory.
 *
 * @see android.os.FileObserver#MOVED_FROM
 */
public class WatchService_MOVE_FROM_InitiatedTest extends WatchServiceBaseTest {

  public void testMoveFileOut() throws Exception {
    tmp().createFile("a");
    tmp().createFile("b");
    await(event(DELETE, "a"), newMoveFrom("a", helper().get("a")));
  }
}