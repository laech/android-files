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
import l.files.fs.ResourceStatus;

import static java.util.Calendar.JUNE;
import static java.util.Calendar.YEAR;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public final class DateCategorizerTest extends BaseTest {

    private Resources res;
    private Calendar midnight;
    private Calendar now;
    private DateCategorizer categorizer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        midnight = new GregorianCalendar(2014, JUNE, 12);
        now = Calendar.getInstance();
        now.setTimeInMillis(midnight.getTimeInMillis());
        now.add(Calendar.HOUR_OF_DAY, 8);
        res = mock(Resources.class);
        categorizer = new DateCategorizer(now.getTimeInMillis());
    }

    public void testModifiedToday() throws Exception {
        String today = "today";
        given(res.getString(R.string.today)).willReturn(today);
        assertCategory(today, mockStat(midnight), mockStat(now));
    }

    public void testModifiedYesterday() throws Exception {
        String yesterday = "yesterday";
        given(res.getString(R.string.yesterday)).willReturn(yesterday);
        assertCategory(
                yesterday,
                mockStat(midnight.getTimeInMillis() - 1),
                mockStat(midnight.getTimeInMillis() - HOURS.toMillis(6)),
                mockStat(midnight.getTimeInMillis() - DAYS.toMillis(1))
        );
    }

    public void testModified7Days() {
        String days7 = "7days";
        given(res.getString(R.string.previous_7_days)).willReturn(days7);
        assertCategory(
                days7,
                mockStat(midnight.getTimeInMillis() - DAYS.toMillis(1) - 1),
                mockStat(midnight.getTimeInMillis() - DAYS.toMillis(2)),
                mockStat(midnight.getTimeInMillis() - DAYS.toMillis(7))
        );
    }

    public void testModified30Days() {
        String days30 = "30days";
        given(res.getString(R.string.previous_30_days)).willReturn(days30);
        assertCategory(
                days30,
                mockStat(midnight.getTimeInMillis() - DAYS.toMillis(7) - 1),
                mockStat(midnight.getTimeInMillis() - DAYS.toMillis(16)),
                mockStat(midnight.getTimeInMillis() - DAYS.toMillis(30))
        );
    }

    public void testModifiedThisYear() {
        Calendar month0 = calendar(midnight.getTimeInMillis() - DAYS.toMillis(30) - 1);
        Calendar month1 = calendar(midnight.getTimeInMillis() - DAYS.toMillis(31));
        Calendar month2 = calendar(midnight.getTimeInMillis() - DAYS.toMillis(31 * 2));
        assertEquals(formatMonth(month0), categorizer.get(res, mockStat(month0)));
        assertEquals(formatMonth(month1), categorizer.get(res, mockStat(month1)));
        assertEquals(formatMonth(month2), categorizer.get(res, mockStat(month2)));
    }

    @SuppressLint("SimpleDateFormat")
    private String formatMonth(Calendar calendar) {
        return new SimpleDateFormat("MMMM").format(calendar.getTime());
    }

    public void testModifiedYearsAgo() {
        Calendar year0 = calendar(midnight.getTimeInMillis());
        year0.set(Calendar.MONTH, Calendar.JANUARY);
        year0.set(Calendar.DAY_OF_MONTH, 1);
        year0.add(Calendar.MILLISECOND, -1);
        Calendar year1 = calendar(midnight.getTimeInMillis() - DAYS.toMillis(365));
        Calendar year2 = calendar(midnight.getTimeInMillis() - DAYS.toMillis(365 * 2));
        assertEquals(String.valueOf(year0.get(YEAR)), categorizer.get(res, mockStat(year0)));
        assertEquals(String.valueOf(year1.get(YEAR)), categorizer.get(res, mockStat(year1)));
        assertEquals(String.valueOf(year2.get(YEAR)), categorizer.get(res, mockStat(year2)));
    }

    public void testModifiedUnknownFuture() {
        String unknown = "unknown";
        given(res.getString(R.string.unknown)).willReturn(unknown);
        assertCategory(unknown, mockStat(midnight.getTimeInMillis() + DAYS.toMillis(1)));
    }

    public void testModifiedUnknownPast() {
        String unknown = "unknown";
        given(res.getString(R.string.__)).willReturn(unknown);
        assertCategory(unknown, mockStat(-1L), mockStat(0L));
    }

    private FileListItem.File mockStat(Calendar time) {
        return mockStat(time.getTimeInMillis());
    }

    private FileListItem.File mockStat(long time) {
        ResourceStatus stat = mock(ResourceStatus.class);
        long seconds = MILLISECONDS.toSeconds(time);
        int nanos = (int) MILLISECONDS.toNanos(time - SECONDS.toMillis(seconds));
        if (nanos < 0) {
            int delta = (int) (SECONDS.toNanos(1) + nanos);
            nanos = delta;
            seconds -= (delta / (double) SECONDS.toNanos(1) + 0.5);
        }
        given(stat.getModificationTime()).willReturn(Instant.of(seconds, nanos));
        return FileListItem.File.create(mock(Resource.class), stat, stat);
    }

    private void assertCategory(String expected, FileListItem.File... stats) {
        for (FileListItem.File file : stats) {
            assertEquals(file.toString(), expected, categorizer.get(res, file));
        }
    }

    private Calendar calendar(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return calendar;
    }
}
