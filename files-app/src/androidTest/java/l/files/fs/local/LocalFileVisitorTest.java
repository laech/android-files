package l.files.fs.local;

import com.google.common.collect.Sets;

import java.util.Set;

import l.files.common.testing.FileBaseTest;
import l.files.fs.PathEntry;

import static com.google.common.collect.Sets.newHashSet;

public final class LocalFileVisitorTest extends FileBaseTest {

  public void testTraversal() {
    tmp().createFile("a/b");
    tmp().createFile("a/c");
    LocalFileSystem.get().symlink(
        LocalPath.of(tmp().get("a/c")),
        LocalPath.of(tmp().get("a/d"))
    );

    Set<PathEntry> expected = Sets.<PathEntry>newHashSet(
        LocalPathEntry.stat(tmp().get()),
        LocalPathEntry.stat(tmp().get("a")),
        LocalPathEntry.stat(tmp().get("a/b")),
        LocalPathEntry.stat(tmp().get("a/c")),
        LocalPathEntry.stat(tmp().get("a/d"))
    );
    Set<PathEntry> actual = newHashSet(
        LocalFileVisitor.get()
            .breadthFirstTraversal(LocalPathEntry.stat(tmp().get()))
    );

    assertEquals(expected, actual);
  }
}
