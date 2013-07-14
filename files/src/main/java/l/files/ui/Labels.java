package l.files.ui;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import com.google.common.base.Function;

import java.io.File;

public final class Labels {

  // TODO rename this class and methods and implementations

  public static Function<File, String> newFileNameProvider() {
    return new FileNameProvider();
  }

  public static Function<File, String> newFileLabelProvider(Resources res) {
    return new FileLabelProvider(res);
  }

  public static Function<File, String> newFileSummaryProvider(
      Resources res,
      Function<? super Long, ? extends CharSequence> dateFormatter,
      Function<? super Long, ? extends CharSequence> sizeFormatter) {
    return new FileSummaryProvider(res, dateFormatter, sizeFormatter);
  }

  public static Function<File, Drawable> newFileDrawableProvider(Resources res) {
    return new FileDrawableProvider(res);
  }

  private Labels() {}
}
