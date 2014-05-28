package l.files.io.file;

import java.util.Set;

import l.files.common.testing.FileBaseTest;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public final class DirectoryTreeTraverserTest extends FileBaseTest {

  public void testTraversal() {
    tmp().createFile("a/b");
    tmp().createFile("a/c");

    Set<String> expected = newHashSet(
        tmp().get().getPath(),
        tmp().get("a").getPath(),
        tmp().get("a/b").getPath(),
        tmp().get("a/c").getPath()
    );
    Set<String> actual = newHashSet(
        DirectoryTreeTraverser.get().breadthFirstTraversal(tmp().get().getPath())
    );

    assertThat(actual, is(expected));
  }
}