package l.files.ui.browser.text;

import android.content.Context;
import android.content.res.Resources;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import javax.annotation.Nullable;

import l.files.fs.Name;
import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.ui.base.fs.FileInfo;
import l.files.ui.browser.R;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static android.os.Looper.getMainLooper;
import static android.os.Looper.myLooper;
import static android.text.Layout.Alignment.ALIGN_CENTER;
import static android.text.format.Formatter.formatShortFileSize;
import static java.util.concurrent.TimeUnit.MINUTES;

public final class FileTextLayouts {

    private static final FileTextLayouts instance = new FileTextLayouts();

    public static FileTextLayouts get() {
        return instance;
    }

    private FileTextLayouts() {
    }

    @Nullable
    private static DateFormatter formatter;

    private static DateFormatter createFormatter(final Context context) {
        return new DateFormatter(context.getApplicationContext());
    }

    public Layout getName(Context context, FileInfo item, int width) {

        if (getMainLooper() != myLooper()) {
            throw new IllegalStateException();
        }

        Name name = item.selfPath().name();
        return new StaticLayout(
                name.toString(),
                getNamePaint(context),
                width,
                ALIGN_CENTER,
                1.1F,
                0,
                false);
    }

    @Nullable
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
    public Layout getLink(Context context, FileInfo item, int width) {

        Path target = item.linkTargetPath();
        if (target == null) {
            return null;
        }

        if (getMainLooper() != myLooper()) {
            throw new IllegalStateException();
        }

        return new StaticLayout(
                context.getString(R.string.link_x, target.toString()),
                getSummaryPaint(context),
                width,
                ALIGN_CENTER,
                1.1F,
                0,
                false);
    }

    @Nullable
    public Layout getSummary(Context context, FileInfo item, int width) {

        Stat stat = item.selfStat();
        if (stat == null) {
            return null;
        }

        if (getMainLooper() != myLooper()) {
            throw new IllegalStateException();
        }

        String summary = getSummary(context, item);
        if (summary == null || summary.isEmpty()) {
            return null;
        }

        return new StaticLayout(
                summary,
                getSummaryPaint(context),
                width,
                ALIGN_CENTER,
                1.1F,
                0,
                false);
    }

    @Nullable
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
