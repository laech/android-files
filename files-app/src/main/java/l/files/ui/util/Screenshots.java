package l.files.ui.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.view.Window;

import com.google.common.base.Optional;

import static android.graphics.Bitmap.Config.RGB_565;
import static android.graphics.Bitmap.createScaledBitmap;
import static java.lang.Math.round;

public final class Screenshots {

  private Screenshots() {}

  /**
   * Takes a screenshot of the given activity. The returned screenshot is scaled
   * and the quality is lowered appropriately to save memory.
   */
  public static Optional<Bitmap> take(Activity activity) {
    return take(activity, RGB_565, 0.65);
  }

  private static Optional<Bitmap> take(
      Activity activity, Bitmap.Config config, double scale) {
    Window window = activity.getWindow();
    if (window == null) {
      return Optional.absent();
    }

    View view = window.getDecorView().getRootView();
    boolean cacheEnabled = view.isDrawingCacheEnabled();
    try {
      return take(view, config, scale);
    } finally {
      view.setDrawingCacheEnabled(cacheEnabled);
    }
  }

  private static Optional<Bitmap> take(
      View view, Bitmap.Config config, double scale) {
    view.setDrawingCacheEnabled(true);
    Bitmap cache = view.getDrawingCache();
    int scaledWidth = (int) round(cache.getWidth() * scale);
    int scaledHeight = (int) round(cache.getHeight() * scale);
    Bitmap scaled = createScaledBitmap(cache, scaledWidth, scaledHeight, true);
    try {
      return Optional.fromNullable(scaled.copy(config, false));
    } finally {
      scaled.recycle();
    }
  }
}
