package l.files.ui.browser;

import android.annotation.SuppressLint;
import android.content.res.Resources;

import com.google.auto.value.AutoValue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import l.files.fs.Stat;
import l.files.ui.browser.BrowserItem.FileItem;

import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Categories files by their last modified date.
 */
final class DateCategorizer extends BaseCategorizer {
    private static final long MILLIS_PER_MINUTE = 60 * 1000;
    private static final long MILLIS_PER_DAY = 24 * 60 * MILLIS_PER_MINUTE;

    @SuppressLint("SimpleDateFormat")
    private final DateFormat monthFormat = new SimpleDateFormat("MMMM");
    private final Map<Integer, String> monthCache = new HashMap<>();

    private final Calendar timestamp = new GregorianCalendar();
    private final long startOfToday;
    private final long startOfTomorrow;
    private final long startOfYesterday;
    private final long startOf7Days;
    private final long startOf30Days;

    public DateCategorizer(long now) {
        timestamp.setTimeInMillis(now);
        timestamp.set(Calendar.HOUR_OF_DAY, 0);
        timestamp.set(Calendar.MINUTE, 0);
        timestamp.set(Calendar.SECOND, 0);
        timestamp.set(Calendar.MILLISECOND, 0);

        startOfToday = timestamp.getTimeInMillis();
        startOfTomorrow = startOfToday + MILLIS_PER_DAY;
        startOfYesterday = startOfToday - MILLIS_PER_DAY;
        startOf7Days = startOfToday - MILLIS_PER_DAY * 7L;
        startOf30Days = startOfToday - MILLIS_PER_DAY * 30L;
    }

    @Override
    public Object id(FileItem file) {
        Stat stat = file.selfStat();
        if (stat == null) {
            return R.string.__;
        }

        long t = stat.lastModifiedTime().to(MILLISECONDS);
        if (t < MILLIS_PER_MINUTE) return R.string.__;
        if (t >= startOfTomorrow) return R.string.future;
        if (t >= startOfToday) return R.string.today;
        if (t >= startOfYesterday) return R.string.yesterday;
        if (t >= startOf7Days) return R.string.previous_7_days;
        if (t >= startOf30Days) return R.string.previous_30_days;

        timestamp.setTimeInMillis(startOfToday);
        int currentYear = timestamp.get(YEAR);

        timestamp.setTimeInMillis(t);
        int thatYear = timestamp.get(YEAR);

        if (currentYear != thatYear) {
            return Year.of(thatYear);
        }
        return Month.of(timestamp.get(MONTH));
    }

    @Override
    public String label(FileItem file, Resources res, Object id) {
        if (id instanceof Year) {
            Stat stat = requireNonNull(file.selfStat());
            timestamp.setTimeInMillis(stat.lastModifiedTime().to(MILLISECONDS));
            return String.valueOf(timestamp.get(YEAR));
        }

        if (id instanceof Month) {
            Stat stat = requireNonNull(file.selfStat());
            timestamp.setTimeInMillis(stat.lastModifiedTime().to(MILLISECONDS));
            int month = timestamp.get(MONTH);
            String format = monthCache.get(month);
            if (format == null) {
                format = monthFormat.format(timestamp.getTime());
                monthCache.put(month, format);
            }
            return format;
        }

        return res.getString((int) id);
    }

    @AutoValue
    static abstract class Year {
        abstract int value();

        static Year of(int value) {
            return new AutoValue_DateCategorizer_Year(value);
        }
    }

    @AutoValue
    static abstract class Month {
        abstract int value();

        static Month of(int value) {
            return new AutoValue_DateCategorizer_Month(value);
        }
    }
}
