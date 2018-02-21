package l.files.ui.browser.sort;

import android.annotation.SuppressLint;
import android.content.res.Resources;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;

import l.files.fs.Instant;
import l.files.fs.Path;
import l.files.ui.base.fs.FileInfo;
import l.files.ui.browser.Header;
import l.files.ui.browser.R;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static java.util.Arrays.asList;
import static java.util.Calendar.JUNE;
import static java.util.Calendar.YEAR;
import static java.util.Collections.unmodifiableList;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static l.files.fs.LinkOption.NOFOLLOW;
import static org.junit.Assert.assertEquals;

public final class DateCategorizerTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

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
        res = getInstrumentation().getContext().getResources();
    }

    @Test
    public void categorize_with_header() throws Exception {
        FileInfo future1 = file(addDaysToMidnight(2));
        FileInfo future2 = file(addDaysToMidnight(1));
        FileInfo today1 = file(addDaysToMidnight(0) + 1);
        FileInfo today2 = file(addDaysToMidnight(0));
        FileInfo yesterday1 = file(addDaysToMidnight(-1) + 10);
        FileInfo yesterday2 = file(addDaysToMidnight(-1));
        FileInfo prev7Days1 = file(addDaysToMidnight(-2));
        FileInfo prev7Days2 = file(addDaysToMidnight(-3));
        FileInfo prev30Days1 = file(addDaysToMidnight(-10));
        FileInfo prev30Days2 = file(addDaysToMidnight(-12));
        FileInfo prevMonth1_1 = file(addDaysToMidnight(-31));
        FileInfo prevMonth1_2 = file(addDaysToMidnight(-31));
        FileInfo prevMonth2 = file(addDaysToMidnight(-31 * 2));
        FileInfo prevMonth3 = file(addDaysToMidnight(-31 * 3));
        FileInfo prevYear1 = file(addDaysToMidnight(-365));
        FileInfo prevYear2_1 = file(addDaysToMidnight(-365 * 2));
        FileInfo prevYear2_2 = file(addDaysToMidnight(-365 * 2));
        FileInfo prevYear2_3 = file(addDaysToMidnight(-365 * 2 - 1));

        List<Object> expected = asList(
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
                prevMonth1_1,
                prevMonth1_2,
                header(formatMonth(addDaysToMidnight(-31 * 2))),
                prevMonth2,
                header(formatMonth(addDaysToMidnight(-31 * 3))),
                prevMonth3,
                header("2013"),
                prevYear1,
                header("2012"),
                prevYear2_1,
                prevYear2_2,
                prevYear2_3
        );

        List<Object> actual = categorizer.categorize(res, asList(
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
                prevMonth1_1,
                prevMonth1_2,
                prevMonth2,
                prevMonth3,
                prevYear1,
                prevYear2_1,
                prevYear2_2,
                prevYear2_3));

        assertEquals(names(expected), names(actual));
    }

    private static List<String> names(Collection<Object> items) {
        List<String> names = new ArrayList<>(items.size());
        for (Object item : items) {
            names.add(item instanceof Header
                    ? item.toString()
                    : ((FileInfo) item).selfPath().name().toString());
        }
        return unmodifiableList(names);
    }

    private long addDaysToMidnight(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(midnight.getTimeInMillis());
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendar.getTimeInMillis();
    }

    private Header header(int stringId) {
        return header(res.getString(stringId));
    }

    private Header header(String string) {
        return new Header(string);
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
    public void modified7Days() throws Exception {
        assertCategory(
                res.getString(R.string.previous_7_days),
                file(millis(midnight) - DAYS.toMillis(1) - 1),
                file(millis(midnight) - DAYS.toMillis(2)),
                file(millis(midnight) - DAYS.toMillis(7))
        );
    }

    @Test
    public void modified30Days() throws Exception {
        assertCategory(
                res.getString(R.string.previous_30_days),
                file(millis(midnight) - DAYS.toMillis(7) - 1),
                file(millis(midnight) - DAYS.toMillis(16)),
                file(millis(midnight) - DAYS.toMillis(30))
        );
    }

    @Test
    public void modifiedThisYear() throws Exception {
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

    private void assertShowMonthOnly(Calendar month) throws IOException {
        String actual = label(file(month));
        String expected = formatMonth(month);
        assertEquals(expected, actual);
    }

    private String label(FileInfo file) {
        int id = categorizer.id(file);
        return categorizer.label(res, id);
    }

    @Test
    public void modifiedYearsAgo() throws Exception {
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

    private void assertShowYearOnly(Calendar year) throws IOException {
        String expected = String.valueOf(year.get(YEAR));
        String actual = label(file(year));
        assertEquals(expected, actual);
    }

    @Test
    public void modifiedUnknownFuture() throws Exception {
        assertCategory(
                res.getString(R.string.future),
                file(millis(midnight) + DAYS.toMillis(1)));
    }

    private FileInfo file(Calendar time) throws IOException {
        return file(time.getTimeInMillis());
    }

    private FileInfo file(long time) throws IOException {
        Path path = Path.of(temporaryFolder.newFile());
        path.setLastModifiedTime(NOFOLLOW, Instant.ofMillis(time));
        return FileInfo.create(path, path.stat(NOFOLLOW), null, null, collator);
    }

    private void assertCategory(String expected, FileInfo... stats) {
        for (FileInfo file : stats) {
            assertEquals(file.toString(), expected, label(file));
        }
    }

    private Calendar calendar(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return calendar;
    }
}
