package l.files.meta;

import android.content.UriMatcher;
import android.net.Uri;

import java.util.List;

import static android.content.UriMatcher.NO_MATCH;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * Provider for caching file metadata.
 */
public final class MetaContract {

  public static final String AUTHORITY = "l.files.meta";
  public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

  static final String PATH_META = "meta";

  static final int MATCH_META = 100;
  static final int MATCH_META_PARENT = 101;

  private MetaContract() {}

  static UriMatcher newMatcher() {
    UriMatcher matcher = new UriMatcher(NO_MATCH);
    matcher.addURI(AUTHORITY, PATH_META, MATCH_META);
    matcher.addURI(AUTHORITY, PATH_META + "/*", MATCH_META_PARENT);
    return matcher;
  }

  /**
   * Builds a content URI for query file metadata of a directory's child files.
   *
   * @param directoryUri the location URI of the parent directory
   * @see MetaColumns#DIRECTORY_URI
   */
  public static Uri buildMetaUri(String directoryUri) {
    return AUTHORITY_URI.buildUpon().appendPath(directoryUri).build();
  }

  static String getParent(Uri uri) {
    List<String> segments = uri.getPathSegments();
    checkArgument(segments.size() == 1);
    return segments.get(0);
  }

  public static interface MetaColumns {

    /**
     * The ID of this metadata.
     * <p/>
     * Type: TEXT
     */
    String ID = "id";

    /**
     * The location URI of this file (not content URI). The existing metadata
     * with the same value will be overridden.
     * <p/>
     * Type: TEXT
     */
    String FILE_URI = "file_uri";

    /**
     * The location URI (not content URI) of this file's parent.
     * <p/>
     * Type: TEXT
     */
    String DIRECTORY_URI = "directory_uri";

    /**
     * The media type of this meta. This is the actual underlying file content
     * type, not the type of the file extension.
     * <p/>
     * Type: TEXT
     */
    String MIME = "mime";

    /**
     * The width of this file, only valid for files that have sizes, such as
     * images.
     * <p/>
     * Type: INTEGER (int)
     */
    String WIDTH = "width";

    /**
     * The height of this file, only valid for files that have sizes, such as
     * images.
     * <p/>
     * Type: INTEGER (int)
     */
    String HEIGHT = "height";
  }

  public static final class Meta implements MetaColumns {
    public static final Uri CONTENT_URI =
        AUTHORITY_URI.buildUpon().appendPath(PATH_META).build();

    private Meta() {}
  }
}
