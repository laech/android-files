package l.files.ui.browser;

import android.content.res.Resources;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.GregorianCalendar;

import l.files.fs.Stat;
import l.files.ui.base.fs.FileInfo;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.JANUARY;
import static java.util.Calendar.MILLISECOND;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.SECOND;
import static java.util.Calendar.YEAR;

/**
 * Categories files by their last modified date.
 */
final class DateCategorizer extends BaseCategorizer {

    private static final long SECONDS_PER_MINUTE = 60;
    private static final long SECONDS_PER_DAY = 24 * 60 * SECONDS_PER_MINUTE;

    private final String[] monthLabels = new DateFormatSymbols().getMonths();
    private final Calendar timestamp = new GregorianCalendar();
    private final long startSecondOfToday;
    private final long startSecondOfTomorrow;
    private final long startSecondOfYesterday;
    private final long startSecondOf7Days;
    private final long startSecondOf30Days;
    private final long startSecondOfThisYear;

    public DateCategorizer(long now) {
        timestamp.setTimeInMillis(now);
        timestamp.set(HOUR_OF_DAY, 0);
        timestamp.set(MINUTE, 0);
        timestamp.set(SECOND, 0);
        timestamp.set(MILLISECOND, 0);

        startSecondOfToday = timestamp.getTimeInMillis() / 1000;
        startSecondOfTomorrow = startSecondOfToday + SECONDS_PER_DAY;
        startSecondOfYesterday = startSecondOfToday - SECONDS_PER_DAY;
        startSecondOf7Days = startSecondOfToday - SECONDS_PER_DAY * 7L;
        startSecondOf30Days = startSecondOfToday - SECONDS_PER_DAY * 30L;

        timestamp.set(MONTH, JANUARY);
        timestamp.set(DAY_OF_MONTH, 1);
        startSecondOfThisYear = timestamp.getTimeInMillis() / 1000;
    }

    @Override
    public int id(FileInfo file) {
        Stat stat = file.selfStat();
        if (stat == null) {
            return R.string.__;
        }

        long seconds = stat.lastModifiedEpochSecond();
        if (seconds < SECONDS_PER_MINUTE) return R.string.__;
        if (seconds >= startSecondOfTomorrow) return R.string.future;
        if (seconds >= startSecondOfToday) return R.string.today;
        if (seconds >= startSecondOfYesterday) return R.string.yesterday;
        if (seconds >= startSecondOf7Days) return R.string.previous_7_days;
        if (seconds >= startSecondOf30Days) return R.string.previous_30_days;

        timestamp.setTimeInMillis(seconds * 1000);
        if (seconds >= startSecondOfThisYear) {
            return -timestamp.get(MONTH);
        } else {
            return -timestamp.get(YEAR);
        }
    }

    @Override
    public String label(FileInfo file, Resources res, int id) {
        if (id > 0) {
            try {
                return res.getString(id);
            } catch (Resources.NotFoundException e) {
                return null;
            }
        }
        if (id >= -12) {
            return monthLabels[-id];
        }
        return String.valueOf(-id);
    }

}
