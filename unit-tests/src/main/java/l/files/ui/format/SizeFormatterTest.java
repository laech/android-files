package l.files.ui.format;

import android.test.AndroidTestCase;

import static android.text.format.Formatter.formatShortFileSize;
import static org.fest.assertions.api.Assertions.assertThat;

public final class SizeFormatterTest extends AndroidTestCase {

  private SizeFormatter formatter;

  @Override protected void setUp() throws Exception {
    super.setUp();
    formatter = new SizeFormatter(getContext());
  }

  public void testFormat() {
    long size = 1039;
    assertThat(formatter.apply(size))
        .isEqualTo(formatShortFileSize(getContext(), size));
  }

}
