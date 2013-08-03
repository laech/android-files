package l.files.app.format;

import android.test.AndroidTestCase;

import static android.text.format.Formatter.formatShortFileSize;

public final class SizeFormatTest extends AndroidTestCase {

  private SizeFormat formatter;

  @Override protected void setUp() throws Exception {
    super.setUp();
    formatter = new SizeFormat(getContext());
  }

  public void testFormat() {
    long size = 1039;
    assertEquals(formatShortFileSize(getContext(), size), formatter.apply(size));
  }
}
