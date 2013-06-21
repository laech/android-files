package l.files.util;

import static android.text.format.DateFormat.getDateFormat;
import static android.text.format.DateFormat.getTimeFormat;
import static java.lang.System.currentTimeMillis;

import java.util.Date;

import android.test.AndroidTestCase;

public final class DateTimeFormatTest extends AndroidTestCase {

  private static final long ONE_DAY_MILLIS = 1000 * 60 * 60 * 24;

  private DateTimeFormat format;

  @Override protected void setUp() throws Exception {
    super.setUp();
    format = new DateTimeFormat(getContext());
  }

  public void testFormatsTimestampAsDateWithoutTimeWhenTimestampIsBeforeToday() {
    Date yesterday = new Date(currentTimeMillis() - ONE_DAY_MILLIS);
    String expected = getDateFormat(getContext()).format(yesterday);
    String actual = format.format(yesterday.getTime());
    assertEquals(expected, actual);
  }

  public void testFormatsTimestampAsTimeWithoutDateWhenTimestampIsWithinToday() {
    Date today = new Date();
    String expected = getTimeFormat(getContext()).format(today);
    String actual = format.format(today.getTime());
    assertEquals(expected, actual);
  }

  public void testCanBeReused() { // Because we use some caching
    long millis = new Date().getTime();
    assertEquals(format.format(millis), format.format(millis));
  }
}
