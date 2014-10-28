package l.files.fs.local;

import java.util.Set;

import l.files.common.testing.FileBaseTest;

import static com.google.common.collect.Sets.newHashSet;
import static l.files.fs.local.LocalDirectoryTreeTraverser.Entry;

public final class LocalDirectoryTreeTraverserTest extends FileBaseTest {

  public void testTraversal() {
    tmp().createFile("a/b");
    tmp().createFile("a/c");

    Set<Entry> expected = newHashSet(
        Entry.create(tmp().get().getPath()),
        Entry.create(tmp().get("a").getPath()),
        Entry.create(tmp().get("a/b").getPath()),
        Entry.create(tmp().get("a/c").getPath())
    );
    Set<Entry> actual = newHashSet(
        LocalDirectoryTreeTraverser.get()
            .breadthFirstTraversal(Entry.create(tmp().get().getPath()))
    );

    assertEquals(expected, actual);
  }
}
