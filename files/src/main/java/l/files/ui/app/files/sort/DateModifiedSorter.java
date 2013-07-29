package l.files.ui.app.files.sort;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.sort;
import static org.joda.time.DateTimeConstants.MILLIS_PER_DAY;

import java.io.File;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import l.files.R;
import l.files.event.Sort;

import org.joda.time.DateMidnight;

import android.content.res.Resources;

import com.google.common.base.Function;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.primitives.Longs;

final class DateModifiedSorter implements Sorter {

  private static enum When {

    // Ordered as they will be displayed on UI

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
      return Longs.compare(x.lastModified(), y.lastModified()) * -1;
    }
  };

  private final Resources res;

  public DateModifiedSorter(Resources res) {
    this.res = checkNotNull(res, "res");
  }

  @Override public List<Object> apply(Collection<File> files) {
    List<File> result = newArrayList(files);
    sort(result, BY_DATE_MODIFIED_DESC);
    return toList(groupByDateModified(result), res);
  }

  private Multimap<When, File> groupByDateModified(List<File> files) {
    final long startOfToday = DateMidnight.now().getMillis();
    final long startOfTomorrow = startOfToday + MILLIS_PER_DAY;
    final long startOfYesterday = startOfToday - MILLIS_PER_DAY;
    final long startOf7Days = startOfToday - MILLIS_PER_DAY * 7L;
    final long startOf30Days = startOfToday - MILLIS_PER_DAY * 30L;

    return Multimaps.index(files, new Function<File, When>() {
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

  private List<Object> toList(Multimap<When, File> groups, Resources res) {
    List<Object> result = newArrayList();
    for (When when : When.values()) {
      Collection<File> col = groups.get(when);
      if (!col.isEmpty()) {
        result.add(res.getString(when.stringResId));
        result.addAll(col);
      }
    }
    return result;
  }

  @Override public Sort id() {
    return Sort.DATE_MODIFIED;
  }

  @Override public String name(Resources res) {
    return res.getString(R.string.date_modified);
  }

}
