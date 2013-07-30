package l.files.ui.format;

import android.content.Context;
import com.google.common.base.Function;

public final class Formats {

  /**
   * Returns a function to format date in milliseconds to readable string.
   */
  public static Function<Long, String> date(Context context) {
    return new DateFormat(context);
  }

  /**
   * Returns a function to format size in bytes to readable string.
   */
  public static Function<Long, String> size(Context context) {
    return new SizeFormat(context);
  }

  private Formats() {}
}
