package l.files.app.util;

import android.graphics.Bitmap;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static android.graphics.Bitmap.createScaledBitmap;
import static android.graphics.BitmapFactory.Options;
import static android.graphics.BitmapFactory.decodeStream;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.min;
import static java.lang.Math.round;

public final class Bitmaps {
  private Bitmaps() {}

  public static ScaledSize scaleSize(
      int width, int height, int maxWidth, int maxHeight) {
    float scale = scale(width, height, maxWidth, maxHeight);
    int scaledWith = round(width * scale);
    int scaledHeight = round(height * scale);
    return new ScaledSize(scaledWith, scaledHeight, scale);
  }

  private static float scale(
      int width, int height, int maxWidth, int maxHeight) {
    checkGreaterThanZero(width);
    checkGreaterThanZero(height);
    checkGreaterThanZero(maxWidth);
    checkGreaterThanZero(maxHeight);
    float widthScale = maxWidth / (float) width;
    float heightScale = maxHeight / (float) height;
    float scale = min(widthScale, heightScale);
    if (scale > 1) {
      scale = 1;
    }
    return scale;
  }

  private static void checkGreaterThanZero(int value) {
    if (value <= 0) {
      throw new IllegalArgumentException(
          "Must be greater than zero, got " + value);
    }
  }

  /**
   * Decodes an image that can fit within the specified {@link ScaledSize}.
   * Returns null if failed to decode the image.
   */
  public static Bitmap decodeScaledBitmap(
      URL url, ScaledSize size) throws IOException {
    checkNotNull(url);
    checkNotNull(size);
    Bitmap bitmap = decodeBitmap(url, size);
    if (bitmap == null) {
      return null;
    }
    Bitmap scaled = getScaledBitmap(bitmap, size);
    if (scaled != bitmap) {
      bitmap.recycle();
    }
    return scaled;
  }

  private static Bitmap getScaledBitmap(Bitmap bitmap, ScaledSize size) {
    int width = size.scaledWidth;
    int height = size.scaledHeight;
    if (bitmap.getWidth() > width || bitmap.getHeight() > height) {
      return createScaledBitmap(bitmap, width, height, true);
    }
    return bitmap;
  }

  private static Bitmap decodeBitmap(
      URL url, ScaledSize size) throws IOException {
    InputStream stream = url.openStream();
    //noinspection TryFinallyCanBeTryWithResources
    try {
      Options options = new Options();
      options.inSampleSize = (int) (1 / size.scale);
      return decodeStream(stream, null, options);
    } finally {
      stream.close();
    }
  }
}
