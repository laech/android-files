package l.files.ui;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import com.google.common.base.Function;

import java.io.File;

public final class Labels {

  public static Function<File, String> newFileLabelProvider(Resources res) {
    return new FileLabelProvider(res);
  }

  public static Function<File, Drawable> newFileDrawableProvider(Resources res) {
    return new FileDrawableProvider(res);
  }

  private Labels() {}
}
