package l.files.ui.browser;

import android.annotation.SuppressLint;
import android.content.res.Resources;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import l.files.R;
import l.files.common.testing.BaseTest;
import l.files.fs.Instant;
import l.files.fs.Resource;
import l.files.fs.Stat;
import l.files.ui.browser.FileListItem.File;

import static java.util.Calendar.JUNE;
import static java.util.Calendar.YEAR;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public final class DateCategorizerTest extends BaseTest
{

    private Resources res;
    private Calendar midnight;
    private Calendar now;
    private DateCategorizer categorizer;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        midnight = new GregorianCalendar(2014, JUNE, 12);
        now = Calendar.getInstance();
        now.setTimeInMillis(millis(midnight));
        now.add(Calendar.HOUR_OF_DAY, 8);
        res = mock(Resources.class);
        categorizer = new DateCategorizer(now.getTimeInMillis());
    }

    private long millis(final Calendar calendar)
    {
        return calendar.getTimeInMillis();
    }

    public void testModifiedToday() throws Exception
    {
        final String today = "today";
        given(res.getString(R.string.today)).willReturn(today);
        assertCategory(today, mockStat(midnight), mockStat(now));
    }

    public void testModifiedYesterday() throws Exception
    {
        final String yesterday = "yesterday";
        given(res.getString(R.string.yesterday)).willReturn(yesterday);
        assertCategory(
                yesterday,
                mockStat(millis(midnight) - 1),
                mockStat(millis(midnight) - HOURS.toMillis(6)),
                mockStat(millis(midnight) - DAYS.toMillis(1))
        );
    }

    public void testModified7Days()
    {
        final String days7 = "7days";
        given(res.getString(R.string.previous_7_days)).willReturn(days7);
        assertCategory(
                days7,
                mockStat(millis(midnight) - DAYS.toMillis(1) - 1),
                mockStat(millis(midnight) - DAYS.toMillis(2)),
                mockStat(millis(midnight) - DAYS.toMillis(7))
        );
    }

    public void testModified30Days()
    {
        final String days30 = "30days";
        given(res.getString(R.string.previous_30_days)).willReturn(days30);
        assertCategory(
                days30,
                mockStat(millis(midnight) - DAYS.toMillis(7) - 1),
                mockStat(millis(midnight) - DAYS.toMillis(16)),
                mockStat(millis(midnight) - DAYS.toMillis(30))
        );
    }

    public void testModifiedThisYear()
    {
        final Calendar month0 = calendar(millis(midnight) - monthMillis() - 1);
        final Calendar month1 = calendar(millis(midnight) - monthMillis());
        final Calendar month2 = calendar(millis(midnight) - monthMillis() * 2);
        assertShowMonthOnly(month0);
        assertShowMonthOnly(month1);
        assertShowMonthOnly(month2);
    }

    private long monthMillis()
    {
        return DAYS.toMillis(31);
    }

    @SuppressLint("SimpleDateFormat")
    private String formatMonth(final Calendar calendar)
    {
        return new SimpleDateFormat("MMMM").format(calendar.getTime());
    }

    private void assertShowMonthOnly(final Calendar month)
    {
        final String expected = formatMonth(month);
        final String actual = categorizer.get(res, mockStat(month));
        assertEquals(expected, actual);
    }

    public void testModifiedYearsAgo()
    {
        final Calendar year2 = calendar(millis(midnight) - yearMillis() * 2);
        final Calendar year1 = calendar(millis(midnight) - yearMillis());
        final Calendar year0 = calendar(millis(midnight));
        year0.set(Calendar.MONTH, Calendar.JANUARY);
        year0.set(Calendar.DAY_OF_MONTH, 1);
        year0.add(Calendar.MILLISECOND, -1);
        assertShowYearOnly(year0);
        assertShowYearOnly(year1);
        assertShowYearOnly(year2);
    }

    private long yearMillis()
    {
        return DAYS.toMillis(365);
    }

    private void assertShowYearOnly(final Calendar year)
    {
        final String expected = String.valueOf(year.get(YEAR));
        final String actual = categorizer.get(res, mockStat(year));
        assertEquals(expected, actual);
    }

    public void testModifiedUnknownFuture()
    {
        final String unknown = "unknown";
        given(res.getString(R.string.unknown)).willReturn(unknown);
        assertCategory(unknown, mockStat(millis(midnight) + DAYS.toMillis(1)));
    }

    public void testModifiedUnknownPast()
    {
        final String unknown = "unknown";
        given(res.getString(R.string.__)).willReturn(unknown);
        assertCategory(unknown, mockStat(-1L), mockStat(0L));
    }

    private File mockStat(final Calendar time)
    {
        return mockStat(time.getTimeInMillis());
    }

    private File mockStat(final long time)
    {
        final Stat stat = mock(Stat.class);
        long seconds = MILLISECONDS.toSeconds(time);
        int nanos = (int) MILLISECONDS.toNanos(time - SECONDS.toMillis(seconds));
        if (nanos < 0)
        {
            final int delta = (int) (SECONDS.toNanos(1) + nanos);
            nanos = delta;
            seconds -= (delta / (double) SECONDS.toNanos(1) + 0.5);
        }
        given(stat.modificationTime()).willReturn(Instant.of(seconds, nanos));
        return File.create(mock(Resource.class), stat, stat);
    }

    private void assertCategory(final String expected, final File... stats)
    {
        for (final File file : stats)
        {
            assertEquals(file.toString(), expected, categorizer.get(res, file));
        }
    }

    private Calendar calendar(final long time)
    {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return calendar;
    }
}
