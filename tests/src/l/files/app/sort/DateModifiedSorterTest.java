package l.files.app.sort;

import android.content.res.Resources;
import junit.framework.TestCase;
import l.files.R;
import l.files.app.setting.Sort;

import java.io.File;

import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.joda.time.DateTimeConstants.MILLIS_PER_DAY;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public final class DateModifiedSorterTest extends TestCase {

  private Resources res;
  private DateModifiedSorter sorter;

  @Override protected void setUp() throws Exception {
    super.setUp();
    res = mock(Resources.class);
    sorter = new DateModifiedSorter(res);
  }

  public void testSortsFilesByDateModifiedDescendinglyWithinTheSameSection() {
    File a = fileModifiedAt("1", 1);
    File b = fileModifiedAt("2", 2);
    File c = fileModifiedAt("3", 3);

    String header = setString(R.string.earlier, "earlier");
    assertThat(sorter.apply(asList(c, a, b))).containsExactly(
        header,
        c,
        b,
        a);
  }

  public void testSortsFilesByDateModifiedDescendinglyForAllSections() {

    File tomorrow = fileModifiedAt("tomorrow", now() + MILLIS_PER_DAY);
    File today = fileModifiedAt("today", now());
    File yesterday = fileModifiedAt("yesterday", now() - MILLIS_PER_DAY);
    File last7Days = fileModifiedAt("7 days", now() - MILLIS_PER_DAY * 6L);
    File last30Days = fileModifiedAt("30 days", now() - MILLIS_PER_DAY * 20L);
    File earlier = fileModifiedAt("earlier", now() - MILLIS_PER_DAY * 100L);

    String headerUnknown = setString(R.string.unknown, "unknown");
    String headerToday = setString(R.string.today, "today");
    String headerYesterday = setString(R.string.yesterday, "yesterday");
    String header7Days = setString(R.string.previous_7_days, "7 days");
    String header30Days = setString(R.string.previous_30_days, "30 days");
    String headerEarlier = setString(R.string.earlier, "earlier");

    assertThat(

        sorter.apply(asList(
            earlier,
            last30Days,
            yesterday,
            last7Days,
            today,
            tomorrow)))

        .containsExactly(

            headerUnknown, tomorrow,
            headerToday, today,
            headerYesterday, yesterday,
            header7Days, last7Days,
            header30Days, last30Days,
            headerEarlier, earlier);
  }

  public void testId() {
    assertThat(sorter.id()).isEqualTo(Sort.DATE_MODIFIED);
  }

  public void testName() {
    given(res.getString(R.string.date_modified)).willReturn("a");
    assertThat(sorter.name(res)).isEqualTo("a");
  }

  private String setString(int id, String value) {
    given(res.getString(id)).willReturn(value);
    return value;
  }

  private long now() {
    return currentTimeMillis();
  }

  private File fileModifiedAt(String name, long time) {
    File file = mock(File.class);
    given(file.lastModified()).willReturn(time);
    given(file.toString()).willReturn(name);
    return file;
  }
}
