package l.files.ui.browser.text;

import android.content.Context;
import androidx.annotation.Nullable;
import l.files.ui.base.fs.FileInfo;
import l.files.ui.browser.R;

import java.nio.file.attribute.BasicFileAttributes;

import static android.text.format.Formatter.formatShortFileSize;

public final class FileTextLayouts {

    private FileTextLayouts() {
    }

    @Nullable
    private static DateFormatter formatter;

    private static DateFormatter createFormatter(Context context) {
        return new DateFormatter(context.getApplicationContext());
    }

    @Nullable
    public static String getSummary(Context context, FileInfo file) {
        BasicFileAttributes attrs = file.selfAttrs();
        if (attrs == null) {
            return null;
        }
        if (formatter == null) {
            formatter = createFormatter(context.getApplicationContext());
        }
        String date = formatter.apply(attrs, context);
        String size = formatShortFileSize(context, attrs.size());
        boolean hasDate =
            attrs.lastModifiedTime().toInstant().getEpochSecond() > 0;
        boolean isFile = attrs.isRegularFile();
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
