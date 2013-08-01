package l.files.app.format;

import android.test.AndroidTestCase;
import com.google.common.base.Function;
import l.files.R;

import java.io.File;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public final class FileSummaryFunctionTest extends AndroidTestCase {

  private File file;
  private Function<Long, String> date;
  private Function<Long, String> size;
  private FileSummaryFunction summary;

  @SuppressWarnings("unchecked")
  @Override protected void setUp() throws Exception {
    super.setUp();
    file = mock(File.class);
    date = mock(Function.class);
    size = mock(Function.class);
    summary = new FileSummaryFunction(getContext().getResources(), date, size);
  }

  public void testShowsSizeAndModifiedTimeForFile() {
    given(file.isFile()).willReturn(true);

    given(file.length()).willReturn(1L);
    given(size.apply(1L)).willReturn("a");

    given(file.lastModified()).willReturn(2L);
    given(date.apply(2L)).willReturn("b");

    assertThat(summary.apply(file)).isEqualTo(
        getContext().getString(R.string.file_size_updated, "a", "b"));
  }

  public void testShowsModifiedTimeForDir() {
    given(file.isDirectory()).willReturn(true);
    given(file.lastModified()).willReturn(1L);
    given(date.apply(1L)).willReturn("a");
    assertThat(summary.apply(file)).isEqualTo("a");
  }
}
