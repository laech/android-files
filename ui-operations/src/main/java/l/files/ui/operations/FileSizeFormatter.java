package l.files.ui.operations;

import android.content.Context;

import static android.text.format.Formatter.formatFileSize;

abstract class FileSizeFormatter {

    static final FileSizeFormatter INSTANCE = new FileSizeFormatter() {

        @Override
        String format(Context context, long sizeBytes) {
            return formatFileSize(context, sizeBytes);
        }

    };

    abstract String format(Context context, long sizeBytes);

}
