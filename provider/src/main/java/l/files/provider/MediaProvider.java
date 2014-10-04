package l.files.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import l.files.logging.Logger;

import static android.graphics.BitmapFactory.Options;
import static android.graphics.BitmapFactory.decodeStream;
import static l.files.provider.FilesContract.getFile;
import static l.files.provider.MediaContract.EXTRA_HEIGHT;
import static l.files.provider.MediaContract.EXTRA_WIDTH;
import static l.files.provider.MediaContract.METHOD_DECODE_BOUNDS;

public final class MediaProvider extends ContentProvider {

  private static final Logger logger = Logger.get(MediaProvider.class);

  @Override public boolean onCreate() {
    return true;
  }

  @Override public Bundle call(String method, String arg, Bundle extras) {
    if (METHOD_DECODE_BOUNDS.equals(method)) {
      try {
        return decodeBounds(arg);
      } catch (IOException e) {
        return Bundle.EMPTY;
      }
    }
    return super.call(method, arg, extras);
  }

  private Bundle decodeBounds(String fileId) throws IOException {
    logger.debug("Decode bounds %s", fileId);

    File file = getFile(fileId);
    if (!file.canRead() && !file.isFile()) {
      return Bundle.EMPTY;
    }

    /* Some files are protected by more than just permission attributes, a file
     * with readable bits set doesn't mean it will be readable, they are also
     * being protected by other means, e.g. /proc/1/maps. Attempt to read those
     * files with BitmapFactory will cause the native code to catch and ignore
     * the exception and loop forever, see
     * https://github.com/android/platform_frameworks_base/blob/master/core/jni/android/graphics/CreateJavaOutputStreamAdaptor.cpp
     * This code here is a workaround for that, attempt to read one byte off the
     * file, if an exception occurs, meaning it can't be read, let the exception
     * propagate, and return no result.
     */
    try (InputStream in = new FileInputStream(file)) {
      //noinspection ResultOfMethodCallIgnored
      in.read();
    }

    Options options = new Options();
    options.inJustDecodeBounds = true;
    try (InputStream in = new FileInputStream(file)) {
      decodeStream(in, null, options);
    }
    if (options.outWidth > 0 && options.outHeight > 0) {
      Bundle bundle = new Bundle(2);
      bundle.putInt(EXTRA_WIDTH, options.outWidth);
      bundle.putInt(EXTRA_HEIGHT, options.outHeight);
      return bundle;
    }
    return Bundle.EMPTY;
  }

  @Override public Cursor query(Uri uri, String[] projection, String selection,
                                String[] selectionArgs, String sortOrder) {
    throw new UnsupportedOperationException();
  }

  @Override public String getType(Uri uri) {
    throw new UnsupportedOperationException();
  }

  @Override public Uri insert(Uri uri, ContentValues values) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    throw new UnsupportedOperationException();
  }

  @Override public int update(
      Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    throw new UnsupportedOperationException();
  }
}
