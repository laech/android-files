package l.files.ui.category;

import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.ReadableInstant;

import l.files.R;
import l.files.common.testing.BaseTest;

import static l.files.provider.FilesContract.Files.MODIFIED;
import static l.files.provider.FilesContract.Files.NAME;
import static l.files.provider.FilesContract.Files.modified;
import static org.joda.time.Period.hours;
import static org.joda.time.Period.millis;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public final class FileDateCategorizerTest extends BaseTest {

  private Resources res;
  private DateTime midnight;
  private DateTime now;
  private MatrixCursor cursor;
  private FileDateCategorizer sorter;

  @Override protected void setUp() throws Exception {
    super.setUp();
    midnight = new DateMidnight(2014, 6, 12).toDateTime();
    now = midnight.plusHours(8);
    res = mock(Resources.class);
    sorter = new FileDateCategorizer(now.getMillis());
    cursor = new MatrixCursor(new String[]{NAME, MODIFIED});
  }

  public void testModifiedToday() throws Exception {
    String today = "today";
    given(res.getString(R.string.today)).willReturn(today);
    addRowLastModified(cursor, midnight);
    addRowLastModified(cursor, now);
    assertCategory(today, cursor);
  }

  public void testModifiedYesterday() throws Exception {
    String yesterday = "yesterday";
    given(res.getString(R.string.yesterday)).willReturn(yesterday);
    addRowLastModified(cursor, midnight.minus(millis(1)));
    addRowLastModified(cursor, midnight.minus(hours(6)));
    addRowLastModified(cursor, midnight.minusDays(1));
    assertCategory(yesterday, cursor);
  }

  public void testModified7Days() {
    String days7 = "7days";
    given(res.getString(R.string.previous_7_days)).willReturn(days7);
    addRowLastModified(cursor, midnight.minusDays(1).minus(millis(1)));
    addRowLastModified(cursor, midnight.minusDays(2));
    addRowLastModified(cursor, midnight.minusDays(7));
    assertCategory(days7, cursor);
  }

  public void testModified30Days() {
    String days30 = "30days";
    given(res.getString(R.string.previous_30_days)).willReturn(days30);
    addRowLastModified(cursor, midnight.minusDays(7).minus(millis(1)));
    addRowLastModified(cursor, midnight.minusDays(16));
    addRowLastModified(cursor, midnight.minusDays(30));
    assertCategory(days30, cursor);
  }

  public void testModifiedThisYear() {
    DateTime month0 = midnight.minusDays(30).minus(millis(1));
    DateTime month1 = midnight.minusMonths(1);
    DateTime month2 = midnight.minusMonths(2);
    DateTime month3 = midnight.withMonthOfYear(1).withDayOfMonth(1);
    addRowLastModified(cursor, month0);
    addRowLastModified(cursor, month1);
    addRowLastModified(cursor, month2);
    addRowLastModified(cursor, month3);
    assertEquals(month0.monthOfYear().getAsText(), getCategory(0, cursor));
    assertEquals(month1.monthOfYear().getAsText(), getCategory(1, cursor));
    assertEquals(month2.monthOfYear().getAsText(), getCategory(2, cursor));
    assertEquals(month3.monthOfYear().getAsText(), getCategory(3, cursor));
  }

  public void testModifiedYearsAgo() {
    DateTime year0 = midnight.withMonthOfYear(1).withDayOfMonth(1).minus(1);
    DateTime year1 = midnight.minusYears(1);
    DateTime year2 = midnight.minusYears(2);
    addRowLastModified(cursor, year0);
    addRowLastModified(cursor, year1);
    addRowLastModified(cursor, year2);
    assertEquals(String.valueOf(year0.getYear()), getCategory(0, cursor));
    assertEquals(String.valueOf(year1.getYear()), getCategory(1, cursor));
    assertEquals(String.valueOf(year2.getYear()), getCategory(2, cursor));
  }

  public void testModifiedUnknownFuture() {
    String unknown = "unknown";
    given(res.getString(R.string.unknown)).willReturn(unknown);
    addRowLastModified(cursor, midnight.plusDays(1));
    assertCategory(unknown, cursor);
  }

  public void testModifiedUnknownPast() {
    String unknown = "unknown";
    given(res.getString(R.string.__)).willReturn(unknown);
    addRowLastModified(cursor, -1L);
    addRowLastModified(cursor, 0L);
    assertCategory(unknown, cursor);
  }

  private String getCategory(int position, Cursor cursor) {
    assertTrue(cursor.moveToPosition(position));
    return sorter.getCategory(res, cursor);
  }

  private void assertCategory(String expected, Cursor cursor) {
    assertTrue(cursor.getCount() > 0);
    assertTrue(cursor.moveToFirst());
    do {
      String msg = cursor.getPosition() + ":" + modified(cursor);
      assertEquals(msg, expected, sorter.getCategory(res, cursor));
    } while (cursor.moveToNext());
  }

  private void addRowLastModified(MatrixCursor cursor, long millis) {
    cursor.newRow().add(MODIFIED, millis);
  }

  private void addRowLastModified(MatrixCursor cursor, ReadableInstant i) {
    cursor.newRow().add(MODIFIED, i.getMillis());
  }
}
