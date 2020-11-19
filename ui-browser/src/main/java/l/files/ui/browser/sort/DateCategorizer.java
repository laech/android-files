package l.files.ui.browser.sort;

import android.content.res.Resources;
import l.files.ui.base.fs.FileInfo;
import l.files.ui.browser.R;

import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static java.util.Calendar.*;

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

    DateCategorizer(long now) {
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
        BasicFileAttributes attrs = file.selfAttrs();
        if (attrs == null) return R.string.__;

        long seconds = attrs.lastModifiedTime().toInstant().getEpochSecond();
        if (seconds >= startSecondOfTomorrow) return R.string.future;
        if (seconds >= startSecondOfToday) return R.string.today;
        if (seconds >= startSecondOfYesterday) return R.string.yesterday;
        if (seconds >= startSecondOf7Days) return R.string.previous_7_days;
        if (seconds >= startSecondOf30Days) return R.string.previous_30_days;

        timestamp.setTimeInMillis(seconds * 1000);
        return -timestamp.get(seconds >= startSecondOfThisYear ? MONTH : YEAR);
    }

    @Override
    public String label(Resources res, int id) {
        if (id > 0) return res.getString(id);
        if (id >= -12) return monthLabels[-id];
        return String.valueOf(-id);
    }

}
