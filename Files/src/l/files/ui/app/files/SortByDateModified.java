package l.files.ui.app.files;

import android.content.Context;
import com.google.common.base.Function;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import l.files.R;
import org.joda.time.DateMidnight;

import java.io.File;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static java.util.Arrays.asList;
import static java.util.Arrays.sort;
import static org.joda.time.DateTimeConstants.MILLIS_PER_DAY;

public final class SortByDateModified implements Function<File[], List<Object>> {

  private static enum When {
    UNKNOWN(R.string.unknown),
    TODAY(R.string.today),
    YESTERDAY(R.string.yesterday),
    PREVIOUS_7_DAYS(R.string.previous_7_days),
    PREVIOUS_30_DAYS(R.string.previous_30_days),
    EARLIER(R.string.earlier);

    final int stringResId;

    When(int stringResId) {
      this.stringResId = stringResId;
    }
  }

  private static final Comparator<File> BY_DATE_MODIFIED_DESC = new Comparator<File>() {
    @Override public int compare(File x, File y) {
      long a = x.lastModified();
      long b = y.lastModified();
      if (a < b) return 1;
      if (a > b) return -1;
      return 0;
    }
  };

  private final Context context;

  public SortByDateModified(Context context) {
    this.context = checkNotNull(context, "context");
  }

  public List<Object> apply(File... fs) {
    File[] files = fs.clone();
    sort(files, BY_DATE_MODIFIED_DESC);
    return toList(groupByDateModified(files), files);
  }

  private Multimap<When, File> groupByDateModified(File[] files) {

    final long startOfToday = DateMidnight.now().getMillis();
    final long startOfTomorrow = startOfToday + MILLIS_PER_DAY;
    final long startOfYesterday = startOfToday - MILLIS_PER_DAY;
    final long startOf7Days = startOfToday - MILLIS_PER_DAY * 7L;
    final long startOf30Days = startOfToday - MILLIS_PER_DAY * 30L;

    return Multimaps.index(asList(files), new Function<File, When>() {
      @Override public When apply(File file) {
        long time = file.lastModified();
        if (time >= startOfTomorrow) return When.UNKNOWN;
        if (time >= startOfToday) return When.TODAY;
        if (time >= startOfYesterday) return When.YESTERDAY;
        if (time >= startOf7Days) return When.PREVIOUS_7_DAYS;
        if (time >= startOf30Days) return When.PREVIOUS_30_DAYS;
        return When.EARLIER;
      }
    });
  }

  private List<Object> toList(Multimap<When, File> groups, File[] files) {
    When[] whens = When.values();
    List<Object> result = newArrayListWithCapacity(files.length + whens.length);
    for (When when : whens) {
      Collection<File> col = groups.get(when);
      if (!col.isEmpty()) {
        result.add(context.getString(when.stringResId));
        result.addAll(col);
      }
    }
    return result;
  }
}
