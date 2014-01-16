package l.files.provider.event;

import android.net.Uri;

import l.files.common.base.ValueObject;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Event to be fired when a query finishes loading the children of a directory.
 */
public final class LoadFinished extends ValueObject {

  private final Uri uri;

  public LoadFinished(Uri uri) {
    this.uri = checkNotNull(uri, "uri");
  }

  public Uri getUri() {
    return uri;
  }
}
