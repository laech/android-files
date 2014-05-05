package l.files.provider.bookmarks;

import android.content.Context;
import android.content.UriMatcher;
import android.net.Uri;

import java.util.List;

import static android.content.UriMatcher.NO_MATCH;
import static android.os.Looper.getMainLooper;
import static android.os.Looper.myLooper;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.provider.FilesContract.FileInfo;

/**
 * Enables bookmarking of {@link FileInfo}.
 */
public final class BookmarksContract {

  static final String PATH_BOOKMARKS = "bookmarks";

  static final int MATCH_BOOKMARKS = 100;
  static final int MATCH_BOOKMARKS_LOCATION = 101;

  private static volatile Uri authority;

  private BookmarksContract() {}

  static UriMatcher newMatcher(Context context) {
    String authority = getAuthorityString(context);
    UriMatcher matcher = new UriMatcher(NO_MATCH);
    matcher.addURI(authority, PATH_BOOKMARKS, MATCH_BOOKMARKS);
    matcher.addURI(authority, PATH_BOOKMARKS + "/*", MATCH_BOOKMARKS_LOCATION);
    return matcher;
  }

  private static Uri getAuthority(Context context) {
    if (authority == null) {
      authority = Uri.parse("content://" + getAuthorityString(context));
    }
    return authority;
  }

  private static String getAuthorityString(Context context) {
    return context.getString(R.string.files_provider_bookmarks_authority);
  }

  /**
   * Creates a Uri for querying the list of all bookmarks.
   */
  public static Uri buildBookmarksUri(Context context) {
    return bookmarksUriBuilder(context).build();
  }

  /**
   * Creates a single bookmark URI to be queried, if the file is currently
   * bookmarked, the resulting cursor will contain exactly one row, otherwise
   * the cursor will be empty.
   *
   * @param fileLocation the {@link FileInfo#LOCATION} of the file
   */
  public static Uri buildBookmarkUri(Context context, String fileLocation) {
    checkNotNull(fileLocation, "fileLocation");
    return bookmarksUriBuilder(context).appendPath(fileLocation).build();
  }

  private static Uri.Builder bookmarksUriBuilder(Context context) {
    return getAuthority(context).buildUpon().appendPath(PATH_BOOKMARKS);
  }

  /**
   * Gets the {@link FileInfo#LOCATION} from the given content URI built with
   * {@link #buildBookmarkUri(android.content.Context, String)}.
   */
  public static String getBookmarkLocation(Uri bookmarkUri) {
    return checkBookmarkUri(bookmarkUri).get(1);
  }

  private static List<String> checkBookmarkUri(Uri uri) {
    List<String> segments = uri.getPathSegments();
    checkArgument(segments.size() == 2, segments.size());
    checkArgument(segments.get(0).equals(PATH_BOOKMARKS), segments.get(0));
    return segments;
  }

  /**
   * Bookmarks the given {@link FileInfo#LOCATION}. Do not call this on the UI
   * thread.
   */
  public static void bookmark(Context context, String fileLocation) {
    ensureNonMainThread();
    Uri uri = buildBookmarkUri(context, fileLocation);
    context.getContentResolver().insert(uri, null);
  }

  /**
   * Unbookmarks the given {@link FileInfo#LOCATION}.Do not call this on the UI
   * thread.
   */
  public static void unbookmark(Context context, String fileLocation) {
    ensureNonMainThread();
    Uri uri = buildBookmarkUri(context, fileLocation);
    context.getContentResolver().delete(uri, null, null);
  }

  private static void ensureNonMainThread() {
    if (myLooper() == getMainLooper()) {
      throw new IllegalStateException("Don't call this from the main thread");
    }
  }
}
