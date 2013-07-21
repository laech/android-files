package l.files.ui.app.files.sort;

import static com.google.common.collect.Lists.transform;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;

import junit.framework.TestCase;
import l.files.setting.SortBy;
import android.content.res.Resources;

import com.google.common.base.Function;

public final class SortersTest extends TestCase {

  public void testGetsSortersInRightOrder() {
    List<Sorter> sorters = Sorters.get(mock(Resources.class));
    List<SortBy> ids = transform(sorters, new Function<Sorter, SortBy>() {
      @Override public SortBy apply(Sorter sorter) {
        return sorter.id();
      }
    });
    assertThat(ids).containsExactly(SortBy.NAME, SortBy.DATE_MODIFIED);
  }
}
