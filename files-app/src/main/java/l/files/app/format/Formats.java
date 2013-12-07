package l.files.app.format;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Typeface;

import com.google.common.base.Function;

import java.io.File;

public final class Formats {
  private Formats() {}

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

  /**
   * Function to return a short summary about the file.
   *
   * @param dateFormat function to format {@link File#lastModified()}
   * @param sizeFormat function to format {@link File#length()}
   */
  public static Function<File, String> summary(
      Resources res,
      Function<? super Long, ? extends CharSequence> dateFormat,
      Function<? super Long, ? extends CharSequence> sizeFormat) {
    return new FileSummaryFunction(res, dateFormat, sizeFormat);
  }

  /**
   * Function to return icon fonts for files.
   */
  public static Function<File, Typeface> iconFont(AssetManager assets) {
    return new IconFontFunction(assets);
  }
}
