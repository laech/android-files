package l.files.sort;

import android.content.res.Resources;
import junit.framework.TestCase;
import l.files.R;

import java.io.File;

import static java.util.Arrays.asList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public final class NameSorterTest extends TestCase {

  private NameSorter sorter;

  @Override protected void setUp() throws Exception {
    super.setUp();
    sorter = new NameSorter();
  }

  public void testSortsByNameIgnoringCase() {
    File x = new File("/x");
    File y = new File("/Y");
    assertEquals(asList(x, y), sorter.apply(null, y, x));
  }

  public void testSortsByNameComparingNamePartsOnly() {
    File x = new File("/1/x");
    File y = new File("/0/y");
    assertEquals(asList(x, y), sorter.apply(null, y, x));
  }

  public void testName() {
    Resources res = mock(Resources.class);
    given(res.getString(R.string.name)).willReturn("a");
    assertEquals("a", sorter.name(res));
  }
}
