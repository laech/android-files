package l.files.operations;

import junit.framework.TestCase;

import static java.util.Arrays.asList;
import static l.files.operations.testing.TargetSubject.assertThat;

public final class TargetTest extends TestCase {

  public void testCreate() throws Exception {
    Target target = Target.create("src", "dst");
    assertThat(target).source("src");
    assertThat(target).destination("dst");
  }

  public void testFromPathsSource() throws Exception {
    Target target = Target.fromPaths(asList("/0/a/b", "/0/a/c"));
    assertThat(target).source("a");
    assertThat(target).destination("a");
  }

  public void testFromPathsSourceAndDestination() throws Exception {
    Target target = Target.fromPaths(asList("/0/a/b", "/0/a/c"), "/a/b/c/d");
    assertThat(target).source("a");
    assertThat(target).destination("d");
  }

}
