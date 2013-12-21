package l.files.app.util;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import java.net.URI;
import java.net.URL;

import static com.google.common.base.Preconditions.checkArgument;
import static l.files.app.util.Bitmaps.decodeScaledBitmap;
import static l.files.app.util.Bitmaps.decodeScaledSize;

/**
 * Task to decode an image.
 * <p/>
 * {@link #onProgressUpdate(Object[])} will be called only once with the size
 * decoded. Then {@link #onPostExecute(Object)} with the decoded bitmap, or null
 * if failed to decode.
 * <p/>
 * This task accepts a single string URI argument for {@link
 * #execute(Object[])}.
 */
public class DecodeImageTask extends AsyncTask<String, ScaledSize, Bitmap> {

  private static final String TAG = DecodeImageTask.class.getSimpleName();

  private final int maxWidth;
  private final int maxHeight;

  public DecodeImageTask(int maxWidth, int maxHeight) {
    checkArgument(maxWidth > 0);
    checkArgument(maxHeight > 0);
    this.maxWidth = maxWidth;
    this.maxHeight = maxHeight;
  }

  @Override protected Bitmap doInBackground(String... params) {
    if (isCancelled()) {
      return null;
    }
    try {
      URL url = new URI(params[0]).toURL();
      ScaledSize size = decodeScaledSize(url, maxWidth, maxHeight);
      if (size != null) {
        publishProgress(size);
        if (!isCancelled()) {
          return decodeScaledBitmap(url, size);
        }
      }
    } catch (Exception e) {
      Log.w(TAG, e);
    }
    return null;
  }
}
