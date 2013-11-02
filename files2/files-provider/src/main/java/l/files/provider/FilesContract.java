package l.files.provider;

import android.net.Uri;

import java.io.File;
import java.util.List;

import static android.content.ContentResolver.SCHEME_CONTENT;

public final class FilesContract {

  public static final String AUTHORITY = "l.files";

  public static final String PATH_FILE = "file";
  public static final String PATH_CHILDREN = "children";

  private FilesContract() {}

  public static Uri buildChildFilesUri(File file) {
    return new Uri.Builder()
        .scheme(SCHEME_CONTENT)
        .authority(AUTHORITY)
        .appendPath(PATH_FILE)
        .appendPath(Uri.fromFile(file).toString())
        .appendPath(PATH_CHILDREN)
        .build();
  }

  public static String getFileId(Uri uri) {
    List<String> segments = uri.getPathSegments();
    if (segments == null || segments.size() < 2) {
      throw new IllegalArgumentException();
    }
    if (!PATH_FILE.equals(segments.get(0))) {
      throw new IllegalArgumentException();
    }
    return segments.get(1);
  }

  public final static class FileInfo {

    /**
     * Unique ID of a file. This value is always available.
     * <p/>
     * Type: STRING
     */
    public static final String COLUMN_FILE_ID = "file_id";

    /**
     * Concrete MIME type of a file. For example, "image/png" or
     * "application/pdf", or directory - {@link #MEDIA_TYPE_DIR}.
     * <p/>
     * Type: STRING
     *
     * @see #MEDIA_TYPE_DIR
     */
    public static final String COLUMN_MEDIA_TYPE = "mime_type";

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
