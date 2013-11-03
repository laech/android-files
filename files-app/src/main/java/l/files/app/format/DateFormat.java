package l.files.app.format;

import android.content.Context;
import com.google.common.base.Function;

import java.util.Date;

import static android.text.format.DateFormat.getDateFormat;
import static android.text.format.DateFormat.getTimeFormat;
import static android.text.format.DateUtils.isToday;
import static com.google.common.base.Preconditions.checkNotNull;

final class DateFormat implements Function<Long, String> {

  private final Context context;

  DateFormat(Context context) {
    this.context = checkNotNull(context, "context");
  }

  @Override public String apply(Long millis) {
    return (isToday(millis)
        ? getTimeFormat(context)
        : getDateFormat(context)).format(new Date(millis));
  }
}
