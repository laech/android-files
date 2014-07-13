package l.files.provider.event;

import android.net.Uri;

import l.files.common.base.ValueObject;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Event to be fired when a query has made some progress loading the children of
 * a directory.
 */
public final class LoadProgress extends ValueObject {

  private final Uri uri;
  private final int numChildrenLoaded;
  private final int totalChildrenCount;

  public LoadProgress(
      Uri uri,
      int numChildrenLoaded,
      int totalChildrenCount) {

    checkNotNull(uri, "uri");
    checkArgument(numChildrenLoaded >= 0);
    checkArgument(totalChildrenCount >= 0);
    checkArgument(numChildrenLoaded <= totalChildrenCount);

    this.uri = uri;
    this.numChildrenLoaded = numChildrenLoaded;
    this.totalChildrenCount = totalChildrenCount;
  }

  public Uri getUri() {
    return uri;
  }

  public int getNumChildrenLoaded() {
    return numChildrenLoaded;
  }

  public int getTotalChildrenCount() {
    return totalChildrenCount;
  }
}
