package l.files.ui;

import android.content.res.Resources;

import org.joda.time.DateMidnight;
import org.joda.time.MutableDateTime;

import l.files.R;
import l.files.fs.FileStatus;

import static org.joda.time.DateTimeConstants.MILLIS_PER_DAY;

/**
 * Categories files by their last modified date.
 */
final class DateCategorizer implements Categorizer {

  private final MutableDateTime timestamp = new MutableDateTime();
  private final long startOfToday;
  private final long startOfTomorrow;
  private final long startOfYesterday;
  private final long startOf7Days;
  private final long startOf30Days;

  public DateCategorizer(long now) {
    startOfToday = new DateMidnight(now).getMillis();
    startOfTomorrow = startOfToday + MILLIS_PER_DAY;
    startOfYesterday = startOfToday - MILLIS_PER_DAY;
    startOf7Days = startOfToday - MILLIS_PER_DAY * 7L;
    startOf30Days = startOfToday - MILLIS_PER_DAY * 30L;
  }

  @Override public String get(Resources res, FileStatus file) {

    long modified = file.lastModifiedTime();
    if (modified <= 0) {
      return res.getString(R.string.__);
    }
    if (modified >= startOfTomorrow) {
      return res.getString(R.string.unknown);
    }
    if (modified >= startOfToday) {
      return res.getString(R.string.today);
    }
    if (modified >= startOfYesterday) {
      return res.getString(R.string.yesterday);
    }
    if (modified >= startOf7Days) {
      return res.getString(R.string.previous_7_days);
    }
    if (modified >= startOf30Days) {
      return res.getString(R.string.previous_30_days);
    }

    timestamp.setMillis(startOfToday);
    int currentYear = timestamp.getYear();

    timestamp.setMillis(modified);
    int thatYear = timestamp.getYear();

    if (currentYear != thatYear) {
      return String.valueOf(thatYear);
    }

    return timestamp.monthOfYear().getAsText();
  }
}
