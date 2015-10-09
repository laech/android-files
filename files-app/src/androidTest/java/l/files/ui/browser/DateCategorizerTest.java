package l.files.ui.browser;

import android.annotation.SuppressLint;
import android.content.res.Resources;

import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;

import l.files.R;
import l.files.testing.BaseTest;
import l.files.fs.File;
import l.files.fs.FileName;
import l.files.fs.Instant;
import l.files.fs.Stat;
import l.files.ui.browser.FileListItem.Header;

import static java.util.Arrays.asList;
import static java.util.Calendar.JUNE;
import static java.util.Calendar.YEAR;
import static java.util.Collections.unmodifiableList;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public final class DateCategorizerTest extends BaseTest {

    private Resources res;
    private Calendar midnight;
    private Calendar now;
    private DateCategorizer categorizer;
    private Collator collator;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        midnight = new GregorianCalendar(2014, JUNE, 12);
        now = Calendar.getInstance();
        now.setTimeInMillis(millis(midnight));
        now.add(Calendar.HOUR_OF_DAY, 8);
        res = getContext().getResources();
        categorizer = new DateCategorizer(now.getTimeInMillis());
        collator = Collator.getInstance();
    }

    public void test_categorize_with_header() throws Exception {
        final FileListItem.File future1 = file(addDaysToMidnight(2));
        final FileListItem.File future2 = file(addDaysToMidnight(1));
        final FileListItem.File today1 = file(addDaysToMidnight(0) + 1);
        final FileListItem.File today2 = file(addDaysToMidnight(0));
        final FileListItem.File yesterday1 = file(addDaysToMidnight(-1) + 10);
        final FileListItem.File yesterday2 = file(addDaysToMidnight(-1));
        final FileListItem.File prev7Days1 = file(addDaysToMidnight(-2));
        final FileListItem.File prev7Days2 = file(addDaysToMidnight(-3));
        final FileListItem.File prev30Days1 = file(addDaysToMidnight(-10));
        final FileListItem.File prev30Days2 = file(addDaysToMidnight(-12));
        final FileListItem.File prevMonth1 = file(addDaysToMidnight(-31));
        final FileListItem.File prevMonth2 = file(addDaysToMidnight(-31 * 2));
        final FileListItem.File prevMonth3 = file(addDaysToMidnight(-31 * 3));
        final FileListItem.File prevYear1 = file(addDaysToMidnight(-365));
        final FileListItem.File prevYear2 = file(addDaysToMidnight(-365 * 2));

        final List<FileListItem> expected = asList(
                header(R.string.future),
                future1,
                future2,
                header(R.string.today),
                today1,
                today2,
                header(R.string.yesterday),
                yesterday1,
                yesterday2,
                header(R.string.previous_7_days),
                prev7Days1,
                prev7Days2,
                header(R.string.previous_30_days),
                prev30Days1,
                prev30Days2,
                header(formatMonth(addDaysToMidnight(-31))),
                prevMonth1,
                header(formatMonth(addDaysToMidnight(-31 * 2))),
                prevMonth2,
                header(formatMonth(addDaysToMidnight(-31 * 3))),
                prevMonth3,
                header("2013"),
                prevYear1,
                header("2012"),
                prevYear2
        );

        final List<FileListItem> actual = categorizer.categorize(res, asList(
                future1,
                future2,
                today1,
                today2,
                yesterday1,
                yesterday2,
                prev7Days1,
                prev7Days2,
                prev30Days1,
                prev30Days2,
                prevMonth1,
                prevMonth2,
                prevMonth3,
                prevYear1,
                prevYear2));

        assertEquals(names(expected), names(actual));
    }

    private static List<String> names(Collection<FileListItem> items) {
        List<String> names = new ArrayList<>(items.size());
        for (FileListItem item : items) {
            names.add(item.isHeader()
                    ? ((Header) item).header()
                    : ((FileListItem.File) item).file().name().toString());
        }
        return unmodifiableList(names);
    }

    private long addDaysToMidnight(final int days) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(midnight.getTimeInMillis());
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendar.getTimeInMillis();
    }

    private Header header(final int stringId) {
        return header(res.getString(stringId));
    }

    private Header header(final String string) {
        return Header.of(string);
    }

    private long millis(final Calendar calendar) {
        return calendar.getTimeInMillis();
    }

    public void testModifiedToday() throws Exception {
        assertCategory(
                res.getString(R.string.today),
                file(midnight),
                file(now));
    }

    public void testModifiedYesterday() throws Exception {
        assertCategory(
                res.getString(R.string.yesterday),
                file(millis(midnight) - 1),
                file(millis(midnight) - HOURS.toMillis(6)),
                file(millis(midnight) - DAYS.toMillis(1))
        );
    }

    public void testModified7Days() {
        assertCategory(
                res.getString(R.string.previous_7_days),
                file(millis(midnight) - DAYS.toMillis(1) - 1),
                file(millis(midnight) - DAYS.toMillis(2)),
                file(millis(midnight) - DAYS.toMillis(7))
        );
    }

    public void testModified30Days() {
        assertCategory(
                res.getString(R.string.previous_30_days),
                file(millis(midnight) - DAYS.toMillis(7) - 1),
                file(millis(midnight) - DAYS.toMillis(16)),
                file(millis(midnight) - DAYS.toMillis(30))
        );
    }

    public void testModifiedThisYear() {
        final Calendar month0 = calendar(millis(midnight) - monthMillis() - 1);
        final Calendar month1 = calendar(millis(midnight) - monthMillis());
        final Calendar month2 = calendar(millis(midnight) - monthMillis() * 2);
        assertShowMonthOnly(month0);
        assertShowMonthOnly(month1);
        assertShowMonthOnly(month2);
    }

    private long monthMillis() {
        return DAYS.toMillis(31);
    }

    private String formatMonth(final long time) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return formatMonth(calendar);
    }

    @SuppressLint("SimpleDateFormat")
    private String formatMonth(final Calendar calendar) {
        return new SimpleDateFormat("MMMM").format(calendar.getTime());
    }

    private void assertShowMonthOnly(final Calendar month) {
        final String actual = label(file(month));
        final String expected = formatMonth(month);
        assertEquals(expected, actual);
    }

    private String label(final FileListItem.File file) {
        final Object id = categorizer.id(file);
        return categorizer.label(file, res, id);
    }

    public void testModifiedYearsAgo() {
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

    private long yearMillis() {
        return DAYS.toMillis(365);
    }

    private void assertShowYearOnly(final Calendar year) {
        final String expected = String.valueOf(year.get(YEAR));
        final String actual = label(file(year));
        assertEquals(expected, actual);
    }

    public void testModifiedUnknownFuture() {
        assertCategory(
                res.getString(R.string.future),
                file(millis(midnight) + DAYS.toMillis(1)));
    }

    public void testModifiedUnknownPast() {
        assertCategory(res.getString(R.string.__), file(-1L), file(0L));
    }

    private FileListItem.File file(final Calendar time) {
        return file(time.getTimeInMillis());
    }

    private FileListItem.File file(final long time) {
        final Stat stat = mock(Stat.class);
        final File res = mock(File.class);
        given(res.name()).willReturn(FileName.of(String.valueOf(time)));
        given(stat.lastModifiedTime()).willReturn(Instant.ofMillis(time));
        return FileListItem.File.create(res, stat, stat, collator);
    }

    private void assertCategory(final String expected, final FileListItem.File... stats) {
        for (final FileListItem.File file : stats) {
            assertEquals(file.toString(), expected, label(file));
        }
    }

    private Calendar calendar(final long time) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return calendar;
    }
}
