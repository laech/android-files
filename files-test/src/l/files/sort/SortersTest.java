package l.files.sort;

import com.google.common.base.Optional;
import junit.framework.TestCase;

import java.io.File;
import java.util.List;

import static java.util.Arrays.asList;
import static l.files.sort.Sort.DATE_MODIFIED;
import static l.files.sort.Sort.NAME;

public final class SortersTest extends TestCase {

  public void testGet_returnsSortersInRightOrder() {
    assertEquals(asList(NAME, DATE_MODIFIED), Sorters.get());
  }

  public void testApply_returnsListSortedByNameOnUnknownSorter() {
    File x = new File("x");
    File y = new File("y");
    File z = new File("z");
    List<?> expected = asList(x, y, z);
    List<?> actual = Sorters.apply(Optional.of("unknown-sorter-id"), null, x, z, y);
    assertEquals(expected, actual);
  }
}
