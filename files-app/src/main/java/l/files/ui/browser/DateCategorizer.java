package l.files.ui.browser;

import android.annotation.SuppressLint;
import android.content.res.Resources;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import l.files.R;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Categories files by their last modified date.
 */
final class DateCategorizer implements Categorizer
{

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

    public DateCategorizer(final long now)
    {
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
    public String get(final Resources res, final FileListItem.File file)
    {
        if (file.stat() == null)
        {
            return res.getString(R.string.__);
        }

        final long t = file.stat().modificationTime().to(MILLISECONDS);
        if (t < MILLIS_PER_MINUTE) return res.getString(R.string.__);
        if (t >= startOfTomorrow) return res.getString(R.string.unknown);
        if (t >= startOfToday) return res.getString(R.string.today);
        if (t >= startOfYesterday) return res.getString(R.string.yesterday);
        if (t >= startOf7Days) return res.getString(R.string.previous_7_days);
        if (t >= startOf30Days) return res.getString(R.string.previous_30_days);

        timestamp.setTimeInMillis(startOfToday);
        final int currentYear = timestamp.get(Calendar.YEAR);

        timestamp.setTimeInMillis(t);
        final int thatYear = timestamp.get(Calendar.YEAR);

        if (currentYear != thatYear)
        {
            return String.valueOf(thatYear);
        }

        final int month = timestamp.get(Calendar.MONTH);
        String format = monthCache.get(month);
        if (format == null)
        {
            format = monthFormat.format(timestamp.getTime());
            monthCache.put(month, format);
        }
        return format;
    }
}
