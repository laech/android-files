package l.files.ui.browser.text;

import android.content.Context;
import android.os.Looper;

import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

import static android.os.Looper.getMainLooper;
import static android.os.Looper.myLooper;
import static android.text.format.DateFormat.getDateFormat;
import static android.text.format.DateFormat.getTimeFormat;
import static android.text.format.DateUtils.*;
import static java.lang.System.currentTimeMillis;
import static java.util.Calendar.DAY_OF_YEAR;
import static java.util.Calendar.YEAR;

final class DateFormatter {

    private final DateFormat dateFormat;
    private final DateFormat timeFormat;

    private static final Date tempDate = new Date();
    private static final StringBuffer tempBuffer = new StringBuffer();
    private static final FieldPosition tempField = new FieldPosition(0);

    private final Formatter tempFormatter =
        new Formatter(tempBuffer, Locale.getDefault());

    private static final Calendar currentTime = Calendar.getInstance();
    private static final Calendar thatTime = Calendar.getInstance();

    private static final int flags
        = FORMAT_SHOW_DATE
        | FORMAT_ABBREV_MONTH
        | FORMAT_NO_YEAR;

    private final Looper mainLooper;

    DateFormatter(Context context) {
        this.dateFormat = getDateFormat(context);
        this.timeFormat = getTimeFormat(context);
        this.mainLooper = getMainLooper();
    }

    String apply(BasicFileAttributes attrs, Context context) {

        if (myLooper() != mainLooper) {
            throw new IllegalStateException(
                "Can only be called on the UI thread.");
        }

        long millis = attrs.lastModifiedTime().toMillis();

        tempDate.setTime(millis);
        tempField.setBeginIndex(0);
        tempField.setEndIndex(0);
        tempBuffer.setLength(0);

        thatTime.setTimeInMillis(millis);
        currentTime.setTimeInMillis(currentTimeMillis());

        if (currentTime.get(YEAR) == thatTime.get(YEAR)) {
            if (currentTime.get(DAY_OF_YEAR) == thatTime.get(DAY_OF_YEAR)) {
                return timeFormat.format(tempDate, tempBuffer, tempField)
                    .toString();
            } else {
                return formatDateRange(
                    context,
                    tempFormatter,
                    millis,
                    millis,
                    flags
                ).toString();
            }
        }

        return dateFormat.format(tempDate, tempBuffer, tempField).toString();
    }

}
