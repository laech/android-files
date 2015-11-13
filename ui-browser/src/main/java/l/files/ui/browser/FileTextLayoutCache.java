package l.files.ui.browser;

import android.content.Context;
import android.support.v4.util.CircularArray;
import android.support.v4.util.CircularIntArray;
import android.text.Layout;
import android.text.SpannableString;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.util.SparseArray;

import java.util.LinkedHashMap;

import l.files.base.Objects;
import l.files.fs.File;
import l.files.fs.Stat;
import l.files.ui.browser.BrowserItem.FileItem;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static android.os.Looper.getMainLooper;
import static android.os.Looper.myLooper;
import static android.text.Layout.Alignment.ALIGN_CENTER;
import static android.text.Spanned.SPAN_INCLUSIVE_EXCLUSIVE;
import static android.text.format.Formatter.formatShortFileSize;
import static java.util.concurrent.TimeUnit.MINUTES;

final class FileTextLayoutCache {

    private FileTextLayoutCache() {
    }

    private static final SparseArray<Cache> caches = new SparseArray<>(2);

    // Like LruCache but simpler and without the synchronization overhead
    private static final class Cache extends LinkedHashMap<File, CacheEntry> {

        private int hits;
        private int misses;

        @Override
        public CacheEntry get(Object key) {
            CacheEntry entry = super.get(key);

            if (entry == null) {
                misses++;
            } else {
                hits++;
            }

            return entry;
        }

        @Override
        protected boolean removeEldestEntry(Entry<File, CacheEntry> eldest) {
            return size() > 5_000;
        }

        String stats() {
            int total = hits + misses;
            int hitPercent = total != 0 ? (100 * hits / total) : 0;
            return "hits=" + hits +
                    ", misses=" + misses +
                    ", hitRate=" + hitPercent + "%";
        }
    }

    static void printStat() {
        if (!BuildConfig.DEBUG) {
            return;
        }
        for (int i = 0; i < caches.size(); i++) {
            Cache cache = caches.get(caches.keyAt(i));
            Log.d("FileLayoutCache", "Cache #" + i + ": " + cache.stats());
        }
    }

    private static final class CacheEntry {

        final Stat stat;
        final Layout layout;

        CacheEntry(Stat stat, Layout layout) {
            this.stat = stat;
            this.layout = layout;
        }
    }

    private static final Object[] spansForLink = {
            new MaxAlphaSpan(150),
            new AbsoluteSizeSpan(12, true),
            new VerticalSpaceSpan(3),
    };

    private static final Object[] spansForSummary = {
            new MaxAlphaSpan(150),
            new AbsoluteSizeSpan(12, true),
            new VerticalSpaceSpan(3),
    };

    private static final CircularIntArray spanStarts = new CircularIntArray(3);
    private static final CircularIntArray spanEnds = new CircularIntArray(3);
    private static final CircularArray<Object[]> spanObjects = new CircularArray<>(16);
    private static final StringBuilder spanBuilder = new StringBuilder();

    private static DateFormatter formatter;

    private static DateFormatter createFormatter(final Context context) {
        return new DateFormatter(context.getApplicationContext());
    }

    static Layout get(Context context, FileItem item, int width) {

        if (getMainLooper() != myLooper()) {
            throw new IllegalStateException();
        }

        int orientation = context.getResources().getConfiguration().orientation;
        Cache cache = caches.get(orientation);
        if (cache == null) {
            cache = new Cache();
            caches.put(orientation, cache);
        }

        File file = item.selfFile();
        Stat stat = item.selfStat();

        CacheEntry entry = cache.get(file);
        if (entry != null && Objects.equal(entry.stat, stat)) {
            return entry.layout;
        }

        if (formatter == null) {
            formatter = createFormatter(context.getApplicationContext());
        }

        spanStarts.clear();
        spanEnds.clear();
        spanObjects.clear();
        spanBuilder.setLength(0);

        CharSequence name = file.name().toString();
        CharSequence summary = getSummary(context, item);
        CharSequence link = null;
        boolean isLink = stat != null && stat.isSymbolicLink();
        if (isLink) {
            File target = item.linkTargetFile();
            if (target != null) {
                link = target.path().toString();
            }
        }

        spanBuilder.append(name);

        if (link != null && link.length() > 0) {
            spanStarts.addLast(spanBuilder.length());
            spanBuilder.append('\n').append(context.getString(R.string.link_x, link));
            spanEnds.addLast(spanBuilder.length());
            spanObjects.addLast(spansForLink);
        }

        if (summary != null && summary.length() > 0) {
            spanStarts.addLast(spanBuilder.length());
            spanBuilder.append('\n').append(summary);
            spanEnds.addLast(spanBuilder.length());
            spanObjects.addLast(spansForSummary);
        }

        SpannableString span = new SpannableString(spanBuilder.toString());
        while (!spanStarts.isEmpty()) {
            int start = spanStarts.popFirst();
            int end = spanEnds.popFirst();
            for (Object sp : spanObjects.popFirst()) {
                if (sp instanceof VerticalSpaceSpan) {
                    span.setSpan(sp, start, start == 0 ? end : start + 1, SPAN_INCLUSIVE_EXCLUSIVE);
                } else {
                    span.setSpan(sp, start, end, SPAN_INCLUSIVE_EXCLUSIVE);
                }
            }
        }

        TextPaint paint = new TextPaint(ANTI_ALIAS_FLAG);
        paint.density = context.getResources().getDisplayMetrics().density;
        paint.setTextSize(context.getResources().getDimension(R.dimen.files_item_text_size));

        entry = new CacheEntry(stat, new StaticLayout(span, paint, width, ALIGN_CENTER, 1.1F, 0, false));
        cache.put(file, entry);

        return entry.layout;
    }

    private static CharSequence getSummary(Context context, FileItem file) {
        Stat stat = file.selfStat();
        if (stat != null) {
            CharSequence date = formatter.apply(stat);
            CharSequence size = formatShortFileSize(context, stat.size());
            boolean hasDate = stat.lastModifiedTime().to(MINUTES) > 0;
            boolean isFile = stat.isRegularFile();
            if (hasDate && isFile) {
                return context.getString(R.string.x_dot_y, date, size);
            } else if (hasDate) {
                return date;
            } else if (isFile) {
                return size;
            }
        }
        return null;
    }

}
