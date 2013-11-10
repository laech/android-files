package l.files.provider;

import android.content.UriMatcher;
import android.net.Uri;

import java.io.File;
import java.net.URI;
import java.util.List;

import static android.content.UriMatcher.NO_MATCH;

public final class FilesContract {

  public static final String AUTHORITY = "l.files";
  public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

  static final String PATH_FILES = "files";
  static final String PATH_CHILDREN = "children";

  static final int MATCH_FILES_ID = 1;
  static final int MATCH_FILES_CHILDREN = 2;

  private FilesContract() {}

  static UriMatcher newMatcher() {
    UriMatcher matcher = new UriMatcher(NO_MATCH);
    matcher.addURI(AUTHORITY, PATH_FILES + "/*", MATCH_FILES_ID);
    matcher.addURI(AUTHORITY, PATH_FILES + "/*/" + PATH_CHILDREN, MATCH_FILES_CHILDREN);
    return matcher;
  }

  public static Uri buildFileUri(String fileId) {
    return AUTHORITY_URI
        .buildUpon()
        .appendPath(PATH_FILES)
        .appendPath(fileId)
        .build();
  }

  public static Uri buildFileChildrenUri(String fileId) {
    return AUTHORITY_URI
        .buildUpon()
        .appendPath(PATH_FILES)
        .appendPath(fileId)
        .appendPath(PATH_CHILDREN)
        .build();
  }

  public static String getFileId(File file) {
    return toFileId(file.toURI());
  }

  public static String getFileId(Uri contentUri) {
    List<String> segments = contentUri.getPathSegments();
    if (segments == null || segments.size() < 2) {
      throw new IllegalArgumentException();
    }
    if (!PATH_FILES.equals(segments.get(0))) {
      throw new IllegalArgumentException();
    }
    return segments.get(1);
  }

  public static Uri getFileSystemUri(Uri uri) {
    return Uri.parse(getFileId(uri));
  }

  static String toFileId(URI uri) {
    return uri.toString();
  }

  static URI toURI(String fileId) {
    return URI.create(fileId);
  }

  public final static class FileInfo {

    /**
     * Unique ID of a file.
     * <p/>
     * Type: STRING
     */
    public static final String COLUMN_ID = "file_id";

    /**
     * Concrete MIME type of a file. For example, "image/png" or
     * "application/pdf", or directory - {@link #MEDIA_TYPE_DIR}.
     * <p/>
     * Type: STRING
     *
     * @see #MEDIA_TYPE_DIR
     */
    public static final String COLUMN_MEDIA_TYPE = "media_type";

    /**
     * Name of a file.
     * <p/>
     * Type: STRING
     */
    public static final String COLUMN_NAME = "name";

    /**
     * Timestamp when a file was last modified, in milliseconds since January 1,
     * 1970 00:00:00.0 UTC.
     * <p/>
     * Type: INTEGER (long)
     */
    public static final String COLUMN_LAST_MODIFIED = "last_modified";

    /**
     * Size of a file, in bytes.
     * <p/>
     * Type: INTEGER (long)
     */
    public static final String COLUMN_SIZE = "size";

    /**
     * Flag indicating that a file is writable.
     * <p/>
     * Type: BOOLEAN
     */
    public static final String COLUMN_WRITABLE = "writable";

    /**
     * Flag indicating that a file is readable.
     * <p/>
     * Type: BOOLEAN
     */
    public static final String COLUMN_READABLE = "readable";

    /**
     * Media type of a directory.
     *
     * @see #COLUMN_MEDIA_TYPE
     */
    public static final String MEDIA_TYPE_DIR = "application/x-directory";

    private FileInfo() {}
  }
}
