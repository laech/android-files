package l.files.app.sort;

import android.content.res.Resources;
import junit.framework.TestCase;
import l.files.R;
import l.files.app.setting.Sort;

import java.io.File;

import static java.util.Arrays.asList;
import static org.fest.assertions.api.Assertions.assertThat;
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
    assertThat(sorter.apply(asList(y, x))).containsExactly(x, y);
  }

  public void testSortsByNameComparingNamePartsOnly() {
    File x = new File("/1/x");
    File y = new File("/0/y");
    assertThat(sorter.apply(asList(y, x))).containsExactly(x, y);
  }

  public void testId() {
    assertThat(sorter.id()).isEqualTo(Sort.NAME);
  }

  public void testName() {
    Resources res = mock(Resources.class);
    given(res.getString(R.string.name)).willReturn("a");
    assertThat(sorter.name(res)).isEqualTo("a");
  }
}
