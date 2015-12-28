package l.files.ui.browser;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.SparseArray;

import java.util.LinkedHashMap;

import l.files.fs.Name;
import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.ui.base.fs.FileInfo;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static android.os.Looper.getMainLooper;
import static android.os.Looper.myLooper;
import static android.text.Layout.Alignment.ALIGN_CENTER;
import static android.text.format.Formatter.formatShortFileSize;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

final class FileTextLayoutCache {

    private static final FileTextLayoutCache instance = new FileTextLayoutCache();

    static FileTextLayoutCache get() {
        return instance;
    }

    private FileTextLayoutCache() {
    }

    private final SparseArray<Cache<Name, Layout>> names = new SparseArray<>();
    private final SparseArray<Cache<Path, Layout>> links = new SparseArray<>();
    private final SparseArray<Cache<Path, Snapshot<Layout>>> summaries = new SparseArray<>();

    // Like LruCache but simpler and without the synchronization overhead
    private static final class Cache<K, V> extends LinkedHashMap<K, V> {

        @Override
        protected boolean removeEldestEntry(Entry<K, V> eldest) {
            return size() > 5_000;
        }

    }

    private static final class Snapshot<V> {

        final V value;
        final long timestamp;

        Snapshot(V value, long timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }
    }

    private static DateFormatter formatter;

    private static DateFormatter createFormatter(final Context context) {
        return new DateFormatter(context.getApplicationContext());
    }

    private <K, V> Cache<K, V> getCache(
            Context context,
            SparseArray<Cache<K, V>> caches) {

        int orientation = context.getResources().getConfiguration().orientation;
        Cache<K, V> cache = caches.get(orientation);
        if (cache == null) {
            cache = new Cache<>();
            caches.put(orientation, cache);
        }
        return cache;
    }

    Layout getName(Context context, FileInfo item, int width) {

        if (getMainLooper() != myLooper()) {
            throw new IllegalStateException();
        }

        Cache<Name, Layout> cache = getCache(context, names);
        Name name = item.selfPath().name();
        Layout layout = cache.get(name);
        if (layout == null) {
            layout = new StaticLayout(
                    name.toString(),
                    getNamePaint(context),
                    width,
                    ALIGN_CENTER,
                    1.1F,
                    0,
                    false);
            cache.put(name, layout);
        }

        return layout;

    }

    private static TextPaint namePaint;

    private TextPaint getNamePaint(Context context) {
        if (namePaint == null) {
            Resources res = context.getResources();
            namePaint = new TextPaint(ANTI_ALIAS_FLAG);
            namePaint.density = res.getDisplayMetrics().density;
            namePaint.setTextSize(res.getDimension(R.dimen.files_item_name_size));
        }
        return namePaint;
    }

    @Nullable
    Layout getLink(Context context, FileInfo item, int width) {

        Path target = item.linkTargetPath();
        if (target == null) {
            return null;
        }

        if (getMainLooper() != myLooper()) {
            throw new IllegalStateException();
        }

        Cache<Path, Layout> cache = getCache(context, links);
        Layout layout = cache.get(target);
        if (layout == null) {
            layout = new StaticLayout(
                    context.getString(R.string.link_x, target.toString()),
                    getSummaryPaint(context),
                    width,
                    ALIGN_CENTER,
                    1.1F,
                    0,
                    false);
            cache.put(target, layout);
        }

        return layout;

    }

    @Nullable
    Layout getSummary(Context context, FileInfo item, int width) {

        Stat stat = item.selfStat();
        if (stat == null) {
            return null;
        }

        if (getMainLooper() != myLooper()) {
            throw new IllegalStateException();
        }

        Cache<Path, Snapshot<Layout>> cache = getCache(context, summaries);
        Path file = item.selfPath();
        Snapshot<Layout> cached = cache.get(file);
        long timestamp = stat.lastModifiedTime().to(MILLISECONDS);

        if (cached == null || cached.timestamp != timestamp) {

            String summary = getSummary(context, item);
            if (summary == null || summary.isEmpty()) {
                return null;
            }

            cached = new Snapshot<Layout>(
                    new StaticLayout(
                            summary,
                            getSummaryPaint(context),
                            width,
                            ALIGN_CENTER,
                            1.1F,
                            0,
                            false),
                    timestamp);

            cache.put(file, cached);
        }

        return cached.value;

    }

    private static TextPaint summaryPaint;

    private TextPaint getSummaryPaint(Context context) {
        if (summaryPaint == null) {
            Resources res = context.getResources();
            summaryPaint = new TextPaint(ANTI_ALIAS_FLAG);
            summaryPaint.density = res.getDisplayMetrics().density;
            summaryPaint.setTextSize(res.getDimension(R.dimen.files_item_summary_size));
        }
        return summaryPaint;
    }

    private static String getSummary(Context context, FileInfo file) {
        Stat stat = file.selfStat();
        if (stat == null) {
            return null;
        }
        if (formatter == null) {
            formatter = createFormatter(context.getApplicationContext());
        }
        String date = formatter.apply(stat);
        String size = formatShortFileSize(context, stat.size());
        boolean hasDate = stat.lastModifiedTime().to(MINUTES) > 0;
        boolean isFile = stat.isRegularFile();
        if (hasDate && isFile) {
            return context.getString(R.string.x_dot_y, date, size);
        } else if (hasDate) {
            return date;
        } else if (isFile) {
            return size;
        }
        return null;
    }

}
