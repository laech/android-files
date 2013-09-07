package l.files.app.format;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.google.common.base.Function;
import java.io.File;
import l.files.R;
import l.files.test.BaseTest;

public final class FileSummaryFunctionTest extends BaseTest {

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

    String expected = getContext().getString(R.string.file_summary, "b", "a");
    String actual = summary.apply(file);
    assertEquals(expected, actual);
  }

  public void testShowsModifiedTimeForDir() {
    given(file.isDirectory()).willReturn(true);
    given(file.lastModified()).willReturn(1L);
    given(date.apply(1L)).willReturn("a");
    assertEquals("a", summary.apply(file));
  }
}
