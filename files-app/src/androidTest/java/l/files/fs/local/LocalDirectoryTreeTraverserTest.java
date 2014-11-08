package l.files.fs.local;

import java.util.Set;

import l.files.common.testing.FileBaseTest;

import static com.google.common.collect.Sets.newHashSet;
import static l.files.fs.Files.symlink;
import static l.files.fs.local.LocalDirectoryTreeTraverser.Entry;

public final class LocalDirectoryTreeTraverserTest extends FileBaseTest {

  public void testTraversal() {
    tmp().createFile("a/b");
    tmp().createFile("a/c");
    symlink(
        LocalPath.of(tmp().get("a/c")),
        LocalPath.of(tmp().get("a/d"))
    );

    Set<Entry> expected = newHashSet(
        Entry.stat(tmp().get()),
        Entry.stat(tmp().get("a")),
        Entry.stat(tmp().get("a/b")),
        Entry.stat(tmp().get("a/c")),
        Entry.stat(tmp().get("a/d"))
    );
    Set<Entry> actual = newHashSet(
        LocalDirectoryTreeTraverser.get()
            .breadthFirstTraversal(Entry.stat(tmp().get()))
    );

    assertEquals(expected, actual);
  }
}
