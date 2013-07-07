package l.files.ui.format;

import android.content.Context;
import com.google.common.base.Function;

public final class Formatters {

  public static Function<Long, String> newDateFormatter(Context context) {
    return new DateFormatter(context);
  }

  public static Function<Long, String> newSizeFormatter(Context context) {
    return new SizeFormatter(context);
  }

  private Formatters() {}
}
