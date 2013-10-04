package l.files.sort;

import android.content.res.Resources;
import com.google.common.primitives.Longs;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import org.joda.time.DateMidnight;

import java.io.File;
import java.util.Comparator;
import java.util.List;

import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static java.util.Arrays.sort;
import static l.files.sort.DateModifiedSorter.When.*;
import static org.joda.time.DateTimeConstants.MILLIS_PER_DAY;

final class DateModifiedSorter implements SortHelper {

    static enum When {

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

    @Override
    public List<Object> apply(Resources res, File... files) {
        final TObjectLongMap<File> timestamps = getLastModifiedTimestamps(files);
        sortByLastModifiedDesc(files, timestamps);

        final long startOfToday = DateMidnight.now().getMillis();
        final long startOfTomorrow = startOfToday + MILLIS_PER_DAY;
        final long startOfYesterday = startOfToday - MILLIS_PER_DAY;
        final long startOf7Days = startOfToday - MILLIS_PER_DAY * 7L;
        final long startOf30Days = startOfToday - MILLIS_PER_DAY * 30L;

        final List<Object> result = newArrayListWithCapacity(files.length + 5);
        When current = null;
        for (File file : files) {
            final long modified = timestamps.get(file);

            final When when;
            if (modified >= startOfTomorrow) when = UNKNOWN;
            else if (modified >= startOfToday) when = TODAY;
            else if (modified >= startOfYesterday) when = YESTERDAY;
            else if (modified >= startOf7Days) when = PREVIOUS_7_DAYS;
            else if (modified >= startOf30Days) when = PREVIOUS_30_DAYS;
            else when = EARLIER;

            if (current != when) {
                current = when;
                result.add(res.getString(when.stringResId));
            }
            result.add(file);
        }
        return result;
    }

    private void sortByLastModifiedDesc(File[] files, final TObjectLongMap<File> timestamps) {
        sort(files, new Comparator<File>() {
            @Override
            public int compare(File a, File b) {
                long x = timestamps.get(a);
                long y = timestamps.get(b);
                return Longs.compare(y, x);
            }
        });
    }

    private TObjectLongMap<File> getLastModifiedTimestamps(File[] files) {
        final float loadFactor = 0.75f;
        final int initialCapacity = (int) ((files.length + 1) / loadFactor);
        final TObjectLongMap<File> timestamps = new TObjectLongHashMap<File>(initialCapacity, loadFactor);
        for (File file : files) {
            timestamps.put(file, file.lastModified());
        }
        return timestamps;
    }

    @Override
    public String name(Resources res) {
        return res.getString(R.string.date_modified);
    }
}
