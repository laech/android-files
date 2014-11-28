package l.files.fs.local;

import com.google.common.collect.Sets;

import java.util.Set;

import l.files.common.testing.FileBaseTest;
import l.files.fs.DirectoryEntry;

import static com.google.common.collect.Sets.newHashSet;
import static l.files.fs.Files.symlink;

public final class LocalDirectoryTreeTraverserTest extends FileBaseTest {

  public void testTraversal() {
    tmp().createFile("a/b");
    tmp().createFile("a/c");
    symlink(
        LocalPath.of(tmp().get("a/c")),
        LocalPath.of(tmp().get("a/d"))
    );

    Set<DirectoryEntry> expected = Sets.<DirectoryEntry>newHashSet(
        LocalDirectoryEntry.stat(tmp().get()),
        LocalDirectoryEntry.stat(tmp().get("a")),
        LocalDirectoryEntry.stat(tmp().get("a/b")),
        LocalDirectoryEntry.stat(tmp().get("a/c")),
        LocalDirectoryEntry.stat(tmp().get("a/d"))
    );
    Set<DirectoryEntry> actual = newHashSet(
        LocalDirectoryTreeTraverser.get()
            .breadthFirstTraversal(LocalDirectoryEntry.stat(tmp().get()))
    );

    assertEquals(expected, actual);
  }
}
