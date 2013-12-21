package l.files.app.util;

import android.graphics.Bitmap;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static android.graphics.Bitmap.createScaledBitmap;
import static android.graphics.BitmapFactory.Options;
import static android.graphics.BitmapFactory.decodeStream;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.min;
import static java.lang.Math.round;

public final class Bitmaps {
  private Bitmaps() {}

  /**
   * Decodes a {@link ScaledSize} for the given URL. The maximum width and
   * height values define the scale of the result, if the width or height of the
   * bitmap is greater than the maximum width or height specified, the returned
   * size will be scaled down (maintaining the aspect ratio) to fit. Returns
   * null if failed to decode the size.
   */
  public static ScaledSize decodeScaledSize(
      URL url, int maxWidth, int maxHeight) throws IOException {
    checkNotNull(url);
    checkArgument(maxWidth > 0);
    checkArgument(maxHeight > 0);
    Options options = decodeBounds(url);
    if (options.outWidth >= 0 && options.outHeight >= 0) {
      return newScaledSize(options, maxWidth, maxHeight);
    }
    return null;
  }

  private static Options decodeBounds(URL url) throws IOException {
    final InputStream stream = url.openStream();
    //noinspection TryFinallyCanBeTryWithResources
    try {
      final Options options = new Options();
      options.inJustDecodeBounds = true;
      decodeStream(stream, null, options);
      return options;
    } finally {
      stream.close();
    }
  }

  private static ScaledSize newScaledSize(
      Options options, int maxWidth, int maxHeight) {
    float scale = getScale(options, maxWidth, maxHeight);
    int scaledWith = round(options.outWidth * scale);
    int scaledHeight = round(options.outHeight * scale);
    return new ScaledSize(scaledWith, scaledHeight, scale);
  }

  private static float getScale(Options options, int maxWidth, int maxHeight) {
    float widthScale = maxWidth / (float) options.outWidth;
    float heightScale = maxHeight / (float) options.outHeight;
    float scale = min(widthScale, heightScale);
    if (scale > 1) {
      return 1;
    } else {
      return scale;
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
