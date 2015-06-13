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
import l.files.fs.Stat;
import l.files.ui.browser.FileListItem.File;

import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Categories files by their last modified date.
 */
final class DateCategorizer implements Categorizer
{
    private static final long MILLIS_PER_MINUTE = 60 * 1000;
    private static final long MILLIS_PER_DAY = 24 * 60 * MILLIS_PER_MINUTE;
    private static final int ID_YEAR = -1;
    private static final int ID_MONTH = -2;

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
    public int id(final File file)
    {
        final Stat stat = file.stat();
        if (stat == null)
        {
            return R.string.__;
        }

        final long t = stat.modificationTime().to(MILLISECONDS);
        if (t < MILLIS_PER_MINUTE) return R.string.__;
        if (t >= startOfTomorrow) return R.string.unknown;
        if (t >= startOfToday) return R.string.today;
        if (t >= startOfYesterday) return R.string.yesterday;
        if (t >= startOf7Days) return R.string.previous_7_days;
        if (t >= startOf30Days) return R.string.previous_30_days;

        timestamp.setTimeInMillis(startOfToday);
        final int currentYear = timestamp.get(YEAR);

        timestamp.setTimeInMillis(t);
        final int thatYear = timestamp.get(YEAR);

        if (currentYear != thatYear)
        {
            return ID_YEAR;
        }
        return ID_MONTH;
    }

    @Override
    public String label(final File file, final Resources res, final int id)
    {
        if (id == ID_YEAR)
        {
            final Stat stat = requireNonNull(file.stat());
            timestamp.setTimeInMillis(stat.modificationTime().to(MILLISECONDS));
            return String.valueOf(timestamp.get(YEAR));
        }

        if (id == ID_MONTH)
        {
            final Stat stat = requireNonNull(file.stat());
            timestamp.setTimeInMillis(stat.modificationTime().to(MILLISECONDS));
            final int month = timestamp.get(MONTH);
            String format = monthCache.get(month);
            if (format == null)
            {
                format = monthFormat.format(timestamp.getTime());
                monthCache.put(month, format);
            }
            return format;
        }

        return res.getString(id);
    }
}
