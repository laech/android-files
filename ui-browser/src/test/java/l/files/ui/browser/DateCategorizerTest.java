package l.files.ui.browser;

import android.annotation.SuppressLint;
import android.content.res.Resources;

import com.ibm.icu.text.Collator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;

import l.files.fs.File;
import l.files.fs.Instant;
import l.files.fs.Name;
import l.files.fs.Stat;
import l.files.ui.browser.BrowserItem.FileItem;
import l.files.ui.browser.BrowserItem.HeaderItem;

import static java.util.Arrays.asList;
import static java.util.Calendar.JUNE;
import static java.util.Calendar.YEAR;
import static java.util.Collections.unmodifiableList;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;

public final class DateCategorizerTest {

    private Resources res;
    private Calendar midnight;
    private Calendar now;
    private DateCategorizer categorizer;
    private Collator collator;

    @Before
    public void setUp() throws Exception {
        midnight = new GregorianCalendar(2014, JUNE, 12);
        now = Calendar.getInstance();
        now.setTimeInMillis(millis(midnight));
        now.add(Calendar.HOUR_OF_DAY, 8);
        categorizer = new DateCategorizer(now.getTimeInMillis());
        collator = Collator.getInstance();
        res = mock(Resources.class);
        given(res.getString(anyInt())).willAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArguments()[0].toString();
            }
        });
    }

    @Test
    public void categorize_with_header() throws Exception {
        FileItem future1 = file(addDaysToMidnight(2));
        FileItem future2 = file(addDaysToMidnight(1));
        FileItem today1 = file(addDaysToMidnight(0) + 1);
        FileItem today2 = file(addDaysToMidnight(0));
        FileItem yesterday1 = file(addDaysToMidnight(-1) + 10);
        FileItem yesterday2 = file(addDaysToMidnight(-1));
        FileItem prev7Days1 = file(addDaysToMidnight(-2));
        FileItem prev7Days2 = file(addDaysToMidnight(-3));
        FileItem prev30Days1 = file(addDaysToMidnight(-10));
        FileItem prev30Days2 = file(addDaysToMidnight(-12));
        FileItem prevMonth1 = file(addDaysToMidnight(-31));
        FileItem prevMonth2 = file(addDaysToMidnight(-31 * 2));
        FileItem prevMonth3 = file(addDaysToMidnight(-31 * 3));
        FileItem prevYear1 = file(addDaysToMidnight(-365));
        FileItem prevYear2 = file(addDaysToMidnight(-365 * 2));

        List<BrowserItem> expected = asList(
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

        List<BrowserItem> actual = categorizer.categorize(res, asList(
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

    private static List<String> names(Collection<BrowserItem> items) {
        List<String> names = new ArrayList<>(items.size());
        for (BrowserItem item : items) {
            names.add(item.isHeaderItem()
                    ? ((HeaderItem) item).header()
                    : ((FileItem) item).selfFile().name().toString());
        }
        return unmodifiableList(names);
    }

    private long addDaysToMidnight(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(midnight.getTimeInMillis());
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendar.getTimeInMillis();
    }

    private HeaderItem header(int stringId) {
        return header(res.getString(stringId));
    }

    private HeaderItem header(String string) {
        return HeaderItem.of(string);
    }

    private long millis(Calendar calendar) {
        return calendar.getTimeInMillis();
    }

    @Test
    public void modifiedToday() throws Exception {
        assertCategory(
                res.getString(R.string.today),
                file(midnight),
                file(now));
    }

    @Test
    public void modifiedYesterday() throws Exception {
        assertCategory(
                res.getString(R.string.yesterday),
                file(millis(midnight) - 1),
                file(millis(midnight) - HOURS.toMillis(6)),
                file(millis(midnight) - DAYS.toMillis(1))
        );
    }

    @Test
    public void modified7Days() {
        assertCategory(
                res.getString(R.string.previous_7_days),
                file(millis(midnight) - DAYS.toMillis(1) - 1),
                file(millis(midnight) - DAYS.toMillis(2)),
                file(millis(midnight) - DAYS.toMillis(7))
        );
    }

    @Test
    public void modified30Days() {
        assertCategory(
                res.getString(R.string.previous_30_days),
                file(millis(midnight) - DAYS.toMillis(7) - 1),
                file(millis(midnight) - DAYS.toMillis(16)),
                file(millis(midnight) - DAYS.toMillis(30))
        );
    }

    @Test
    public void modifiedThisYear() {
        Calendar month0 = calendar(millis(midnight) - monthMillis() - 1);
        Calendar month1 = calendar(millis(midnight) - monthMillis());
        Calendar month2 = calendar(millis(midnight) - monthMillis() * 2);
        assertShowMonthOnly(month0);
        assertShowMonthOnly(month1);
        assertShowMonthOnly(month2);
    }

    private long monthMillis() {
        return DAYS.toMillis(31);
    }

    private String formatMonth(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return formatMonth(calendar);
    }

    @SuppressLint("SimpleDateFormat")
    private String formatMonth(Calendar calendar) {
        return new SimpleDateFormat("MMMM").format(calendar.getTime());
    }

    private void assertShowMonthOnly(Calendar month) {
        String actual = label(file(month));
        String expected = formatMonth(month);
        assertEquals(expected, actual);
    }

    private String label(FileItem file) {
        Object id = categorizer.id(file);
        return categorizer.label(file, res, id);
    }

    @Test
    public void modifiedYearsAgo() {
        Calendar year2 = calendar(millis(midnight) - yearMillis() * 2);
        Calendar year1 = calendar(millis(midnight) - yearMillis());
        Calendar year0 = calendar(millis(midnight));
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

    private void assertShowYearOnly(Calendar year) {
        String expected = String.valueOf(year.get(YEAR));
        String actual = label(file(year));
        assertEquals(expected, actual);
    }

    @Test
    public void modifiedUnknownFuture() {
        assertCategory(
                res.getString(R.string.future),
                file(millis(midnight) + DAYS.toMillis(1)));
    }

    @Test
    public void modifiedUnknownPast() {
        assertCategory(res.getString(R.string.__), file(-1L), file(0L));
    }

    private FileItem file(Calendar time) {
        return file(time.getTimeInMillis());
    }

    private FileItem file(long time) {
        Stat stat = mock(Stat.class);
        File file = mock(File.class);
        Name name = mock(Name.class);
        given(name.toString()).willReturn(String.valueOf(time));
        given(file.name()).willReturn(name);
        given(stat.lastModifiedTime()).willReturn(Instant.ofMillis(time));
        return FileItem.create(file, stat, null, null, new Provider<Collator>() {
            @Override
            public Collator get() {
                return collator;
            }
        });
    }

    private void assertCategory(String expected, FileItem... stats) {
        for (FileItem file : stats) {
            assertEquals(file.toString(), expected, label(file));
        }
    }

    private Calendar calendar(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return calendar;
    }
}
