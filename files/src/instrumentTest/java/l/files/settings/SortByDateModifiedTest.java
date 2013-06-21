package l.files.settings;

import android.test.AndroidTestCase;
import l.files.R;

import java.io.File;

import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static org.joda.time.DateTimeConstants.MILLIS_PER_DAY;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public final class SortByDateModifiedTest extends AndroidTestCase {

  private SortByDateModified sort;

  @Override protected void setUp() throws Exception {
    super.setUp();
    sort = new SortByDateModified();
  }

  public void testFilesWithinTheSameSectionAreSortedDesc() {
    File a = fileModifiedAt("1", 1);
    File b = fileModifiedAt("2", 2);
    File c = fileModifiedAt("3", 3);
    assertEquals(
        asList(string(R.string.earlier), c, b, a),
        sort.transform(getContext(), c, a, b));
  }

  public void testOrderIs_unknown_today_yesterday_7Days_30Days_earlier() {

    File tomorrow = fileModifiedAt("tomorrow", now() + MILLIS_PER_DAY);
    File today = fileModifiedAt("today", now());
    File yesterday = fileModifiedAt("yesterday", now() - MILLIS_PER_DAY);
    File last7Days = fileModifiedAt("7 days", now() - MILLIS_PER_DAY * 6L);
    File last30Days = fileModifiedAt("30 days", now() - MILLIS_PER_DAY * 20L);
    File earlier = fileModifiedAt("earlier", now() - MILLIS_PER_DAY * 100L);

    assertEquals(
        asList(
            string(R.string.unknown), tomorrow,
            string(R.string.today), today,
            string(R.string.yesterday), yesterday,
            string(R.string.previous_7_days), last7Days,
            string(R.string.previous_30_days), last30Days,
            string(R.string.earlier), earlier
        ),
        sort.transform(getContext(),
            earlier,
            last30Days,
            yesterday,
            last7Days,
            today,
            tomorrow
        )
    );
  }

  private String string(int resId) {
    return getContext().getString(resId);
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
