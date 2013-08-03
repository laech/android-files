package l.files.app.format;

import android.test.AndroidTestCase;

import java.util.Date;

import static android.text.format.DateFormat.getDateFormat;
import static android.text.format.DateFormat.getTimeFormat;
import static java.lang.System.currentTimeMillis;
import static org.joda.time.DateTimeConstants.MILLIS_PER_DAY;

public final class DateFormatTest extends AndroidTestCase {

  private DateFormat formatter;

  @Override protected void setUp() throws Exception {
    super.setUp();
    formatter = new DateFormat(getContext());
  }

  public void testFormatsTimestampAsDateWithoutTimeWhenTimestampIsBeforeToday() {
    Date yesterday = new Date(currentTimeMillis() - MILLIS_PER_DAY);
    String expected = getDateFormat(getContext()).format(yesterday);
    String actual = formatter.apply(yesterday.getTime());
    assertEquals(expected, actual);
  }

  public void testFormatsTimestampAsTimeWithoutDateWhenTimestampIsWithinToday() {
    Date today = new Date();
    String expected = getTimeFormat(getContext()).format(today);
    String actual = formatter.apply(today.getTime());
    assertEquals(expected, actual);
  }
}
