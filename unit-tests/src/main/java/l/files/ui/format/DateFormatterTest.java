package l.files.ui.format;

import android.test.AndroidTestCase;

import java.util.Date;

import static android.text.format.DateFormat.getDateFormat;
import static android.text.format.DateFormat.getTimeFormat;
import static java.lang.System.currentTimeMillis;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.joda.time.DateTimeConstants.MILLIS_PER_DAY;

public final class DateFormatterTest extends AndroidTestCase {

  private DateFormatter formatter;

  @Override protected void setUp() throws Exception {
    super.setUp();
    formatter = new DateFormatter(getContext());
  }

  public void testFormatsTimestampAsDateWithoutTimeWhenTimestampIsBeforeToday() {
    Date yesterday = new Date(currentTimeMillis() - MILLIS_PER_DAY);
    String expected = getDateFormat(getContext()).format(yesterday);
    String actual = formatter.apply(yesterday.getTime());
    assertThat(actual).isEqualTo(expected);
  }

  public void testFormatsTimestampAsTimeWithoutDateWhenTimestampIsWithinToday() {
    Date today = new Date();
    String expected = getTimeFormat(getContext()).format(today);
    String actual = formatter.apply(today.getTime());
    assertThat(actual).isEqualTo(expected);
  }
}
