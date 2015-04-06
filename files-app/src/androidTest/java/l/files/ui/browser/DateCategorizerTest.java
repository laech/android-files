package l.files.ui.browser;

import android.content.res.Resources;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.ReadableInstant;

import l.files.R;
import l.files.common.testing.BaseTest;
import l.files.fs.ResourceStatus;
import l.files.fs.Path;

import static org.joda.time.Period.hours;
import static org.joda.time.Period.millis;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public final class DateCategorizerTest extends BaseTest {

  private Resources res;
  private DateTime midnight;
  private DateTime now;
  private DateCategorizer categorizer;

  @Override protected void setUp() throws Exception {
    super.setUp();
    midnight = new DateMidnight(2014, 6, 12).toDateTime();
    now = midnight.plusHours(8);
    res = mock(Resources.class);
    categorizer = new DateCategorizer(now.getMillis());
  }

  public void testModifiedToday() throws Exception {
    String today = "today";
    given(res.getString(R.string.today)).willReturn(today);
    assertCategory(today, mockStat(midnight), mockStat(now));
  }

  public void testModifiedYesterday() throws Exception {
    String yesterday = "yesterday";
    given(res.getString(R.string.yesterday)).willReturn(yesterday);
    assertCategory(yesterday,
        mockStat(midnight.minus(millis(1))),
        mockStat(midnight.minus(hours(6))),
        mockStat(midnight.minusDays(1)));
  }

  public void testModified7Days() {
    String days7 = "7days";
    given(res.getString(R.string.previous_7_days)).willReturn(days7);
    assertCategory(days7,
        mockStat(midnight.minusDays(1).minus(millis(1))),
        mockStat(midnight.minusDays(2)),
        mockStat(midnight.minusDays(7)));
  }

  public void testModified30Days() {
    String days30 = "30days";
    given(res.getString(R.string.previous_30_days)).willReturn(days30);
    assertCategory(days30,
        mockStat(midnight.minusDays(7).minus(millis(1))),
        mockStat(midnight.minusDays(16)),
        mockStat(midnight.minusDays(30)));
  }

  public void testModifiedThisYear() {
    DateTime month0 = midnight.minusDays(30).minus(millis(1));
    DateTime month1 = midnight.minusMonths(1);
    DateTime month2 = midnight.minusMonths(2);
    DateTime month3 = midnight.withMonthOfYear(1).withDayOfMonth(1);
    assertEquals(month0.monthOfYear().getAsText(), categorizer.get(res, mockStat(month0)));
    assertEquals(month1.monthOfYear().getAsText(), categorizer.get(res, mockStat(month1)));
    assertEquals(month2.monthOfYear().getAsText(), categorizer.get(res, mockStat(month2)));
    assertEquals(month3.monthOfYear().getAsText(), categorizer.get(res, mockStat(month3)));
  }

  public void testModifiedYearsAgo() {
    DateTime year0 = midnight.withMonthOfYear(1).withDayOfMonth(1).minus(1);
    DateTime year1 = midnight.minusYears(1);
    DateTime year2 = midnight.minusYears(2);
    assertEquals(String.valueOf(year0.getYear()), categorizer.get(res, mockStat(year0)));
    assertEquals(String.valueOf(year1.getYear()), categorizer.get(res, mockStat(year1)));
    assertEquals(String.valueOf(year2.getYear()), categorizer.get(res, mockStat(year2)));
  }

  public void testModifiedUnknownFuture() {
    String unknown = "unknown";
    given(res.getString(R.string.unknown)).willReturn(unknown);
    assertCategory(unknown, mockStat(midnight.plusDays(1)));
  }

  public void testModifiedUnknownPast() {
    String unknown = "unknown";
    given(res.getString(R.string.__)).willReturn(unknown);
    assertCategory(unknown, mockStat(-1L), mockStat(0L));
  }

  private FileListItem.File mockStat(ReadableInstant time) {
    return mockStat(time.getMillis());
  }

  private FileListItem.File mockStat(long time) {
    ResourceStatus stat = mock(ResourceStatus.class);
    given(stat.getLastModifiedTime()).willReturn(time);
    return FileListItem.File.create(mock(Path.class), stat, stat);
  }

  private void assertCategory(String expected, FileListItem.File... stats) {
    for (FileListItem.File file : stats) {
      assertEquals(file.toString(), expected, categorizer.get(res, file));
    }
  }
}
