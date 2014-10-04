package l.files.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;

public final class MediaContract {
  private MediaContract() {}

  static final String METHOD_DECODE_BOUNDS = "decode-bounds";
  static final String EXTRA_WIDTH = "width";
  static final String EXTRA_HEIGHT = "height";

  private static Uri authority;

  private static Uri getAuthority(Context context) {
    if (authority == null) {
      authority = Uri.parse("content://" + getAuthorityString(context));
    }
    return authority;
  }

  private static String getAuthorityString(Context context) {
    return context.getString(R.string.files_provider_media_authority);
  }

  public static Optional<Bounds> decodeBounds(Context context, String fileId) {
    Uri uri = getAuthority(context).buildUpon().appendPath(fileId).build();
    ContentResolver resolver = context.getContentResolver();
    Bundle result = resolver.call(uri, METHOD_DECODE_BOUNDS, fileId, null);
    if (result.isEmpty()) {
      return Optional.absent();
    } else {
      return Optional.of(Bounds.create(
          result.getInt(EXTRA_WIDTH),
          result.getInt(EXTRA_HEIGHT)
      ));
    }
  }

  @AutoValue
  public abstract static class Bounds {
    Bounds() {}

    public abstract int width();
    public abstract int height();

    public static Bounds create(int width, int height) {
      return new AutoValue_MediaContract_Bounds(width, height);
    }
  }
}
