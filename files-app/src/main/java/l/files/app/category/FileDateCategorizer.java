package l.files.app.category;

import android.content.res.Resources;
import android.database.Cursor;

import org.joda.time.DateMidnight;
import org.joda.time.MutableDateTime;

import l.files.R;
import l.files.provider.FilesContract;

import static l.files.provider.FileCursors.getLastModified;
import static org.joda.time.DateTimeConstants.MILLIS_PER_DAY;

/**
 * Categories files by their last modified date.
 *
 * @see FilesContract.FileInfo
 */
final class FileDateCategorizer implements Categorizer {

  private final MutableDateTime timestamp = new MutableDateTime();
  private final long startOfToday;
  private final long startOfTomorrow;
  private final long startOfYesterday;
  private final long startOf7Days;
  private final long startOf30Days;

  public FileDateCategorizer(long now) {
    startOfToday = new DateMidnight(now).getMillis();
    startOfTomorrow = startOfToday + MILLIS_PER_DAY;
    startOfYesterday = startOfToday - MILLIS_PER_DAY;
    startOf7Days = startOfToday - MILLIS_PER_DAY * 7L;
    startOf30Days = startOfToday - MILLIS_PER_DAY * 30L;
  }

  @Override public String getCategory(Resources res, Cursor cursor) {

    long modified = getLastModified(cursor);
    if (modified >= startOfTomorrow)
      return res.getString(R.string.unknown);
    if (modified >= startOfToday)
      return res.getString(R.string.today);
    if (modified >= startOfYesterday)
      return res.getString(R.string.yesterday);
    if (modified >= startOf7Days)
      return res.getString(R.string.previous_7_days);
    if (modified >= startOf30Days)
      return res.getString(R.string.previous_30_days);

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
