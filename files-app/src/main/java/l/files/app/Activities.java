package l.files.app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.view.Window;

import com.google.common.base.Optional;

import static android.graphics.Bitmap.Config.RGB_565;
import static android.graphics.Bitmap.createScaledBitmap;
import static java.lang.Math.round;

public final class Activities {
  private Activities() {}

  public static Optional<Bitmap> takeScreenshotForFeedback(Activity activity) {
    Window window = activity.getWindow();
    if (window == null) {
      return Optional.absent();
    }

    View view = window.getDecorView().getRootView();
    boolean cacheEnabled = view.isDrawingCacheEnabled();
    try {
      view.setDrawingCacheEnabled(true);
      Bitmap cache = view.getDrawingCache();
      int width = cache.getWidth();
      int height = cache.getHeight();
      double scale = 0.65;
      int scaledWidth = (int) round(width * scale);
      int scaledHeight = (int) round(height * scale);
      Bitmap tmp = createScaledBitmap(cache, scaledWidth, scaledHeight, true);
      Bitmap result = tmp.copy(RGB_565, false);
      tmp.recycle();
      return Optional.of(result);
    } finally {
      view.setDrawingCacheEnabled(cacheEnabled);
    }
  }
}
