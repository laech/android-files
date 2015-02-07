package l.files.operations;

import junit.framework.TestCase;

import l.files.fs.local.LocalPath;

import static java.util.Arrays.asList;

public final class TargetTest extends TestCase {

  public void testCreate() throws Exception {
    Target target = new Target("src", "dst");
    assertEquals("src", target.getSource());
    assertEquals("dst", target.getDestination());
  }

  public void testFromPathsSource() throws Exception {
    Target target = Target.fromPaths(
        asList(LocalPath.of("/0/a/b"), LocalPath.of("/0/a/c")));
    assertEquals("a", target.getSource());
    assertEquals("a", target.getDestination());
  }

  public void testFromPathsSourceAndDestination() throws Exception {
    Target target = Target.fromPaths(
        asList(LocalPath.of("/0/a/b"), LocalPath.of("/0/a/c")),
        LocalPath.of("/a/b/c/d"));
    assertEquals("a", target.getSource());
    assertEquals("d", target.getDestination());
  }

}
