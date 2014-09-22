package l.files.operations;

import junit.framework.TestCase;

import static java.util.Arrays.asList;

public final class TargetTest extends TestCase {

  public void testCreate() throws Exception {
    Target target = Target.create("src", "dst");
    assertEquals("src", target.source());
    assertEquals("dst", target.destination());
  }

  public void testFromPathsSource() throws Exception {
    Target target = Target.fromPaths(asList("/0/a/b", "/0/a/c"));
    assertEquals("a", target.source());
    assertEquals("a", target.destination());
  }

  public void testFromPathsSourceAndDestination() throws Exception {
    Target target = Target.fromPaths(asList("/0/a/b", "/0/a/c"), "/a/b/c/d");
    assertEquals("a", target.source());
    assertEquals("d", target.destination());
  }

}
