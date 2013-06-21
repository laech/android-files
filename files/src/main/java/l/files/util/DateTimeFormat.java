package l.files.util;

import static android.text.format.DateFormat.getDateFormat;
import static android.text.format.DateFormat.getTimeFormat;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.util.Date;

import android.content.Context;
import android.text.format.Time;

public class DateTimeFormat {

  // This class is intended to be called frequently in list adapters, so we
  // cache some variables
  private final Time time = new Time();
  private final Date date = new Date(0);
  private final FieldPosition field = new FieldPosition(0);
  private final StringBuffer buffer = new StringBuffer(0);

  private final DateFormat dayFormat;
  private final DateFormat timeFormat;

  public DateTimeFormat(Context context) {
    this.dayFormat = getDateFormat(context);
    this.timeFormat = getTimeFormat(context);
  }

  public String format(long millis) {
    buffer.delete(0, buffer.length());
    date.setTime(millis);

    time.set(millis);
    int year = time.year;
    int yearDay = time.yearDay;

    time.setToNow();
    boolean today = year == time.year && yearDay == time.yearDay;

    return (today ? timeFormat : dayFormat)
        .format(date, buffer, field).toString();
  }

}
