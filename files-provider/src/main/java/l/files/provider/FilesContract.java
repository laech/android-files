package l.files.provider;

import android.content.ContentResolver;
import android.content.UriMatcher;
import android.net.Uri;
import android.os.Bundle;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Set;

import static android.content.UriMatcher.NO_MATCH;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class FilesContract {

  public static final String AUTHORITY = "l.files";
  public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

  static final String PATH_FILES = "files";
  static final String PATH_CHILDREN = "children";
  static final String PATH_BOOKMARKS = "bookmarks";
  static final String PATH_SUGGESTION = "suggestion";

  static final String PARAM_SHOW_HIDDEN = "showHidden";
  static final String VALUE_SHOW_HIDDEN_YES = "1";
  static final String VALUE_SHOW_HIDDEN_FILES_NO = "0";

  static final int MATCH_FILES_ID = 100;
  static final int MATCH_FILES_ID_CHILDREN = 101;
  static final int MATCH_BOOKMARKS = 200;
  static final int MATCH_BOOKMARKS_ID = 201;
  static final int MATCH_SUGGESTION = 300;

  static final String METHOD_RENAME = "rename";
  static final String EXTRA_NEW_NAME = "new_name";
  static final String EXTRA_RESULT = "result";

  private FilesContract() {}

  static UriMatcher newMatcher() {
    UriMatcher matcher = new UriMatcher(NO_MATCH);
    matcher.addURI(AUTHORITY, PATH_BOOKMARKS, MATCH_BOOKMARKS);
    matcher.addURI(AUTHORITY, PATH_BOOKMARKS + "/*", MATCH_BOOKMARKS_ID);
    matcher.addURI(AUTHORITY, PATH_FILES + "/*", MATCH_FILES_ID);
    matcher.addURI(AUTHORITY, PATH_FILES + "/*/" + PATH_CHILDREN, MATCH_FILES_ID_CHILDREN);
    matcher.addURI(AUTHORITY, PATH_SUGGESTION + "/*/*", MATCH_SUGGESTION);
    return matcher;
  }

  /**
   * A suggestion Uri can be queried, the return cursor will contain a single
   * directory that does not exist, suitable for creating a new directory under
   * that name.
   *
   * @param parentId the parent directory's ID
   * @param basename the basename for the return directory's name, if there is
   * already a file with the same name, a number will be appended
   */
  public static Uri buildSuggestionUri(String parentId, String basename) {
    checkNotNull(parentId, "parentId");
    checkNotNull(basename, "basename");
    return AUTHORITY_URI
        .buildUpon()
        .appendPath(PATH_SUGGESTION)
        .appendPath(parentId)
        .appendPath(basename)
        .build();
  }

  public static String getSuggestionParentId(Uri uri) {
    return checkSuggestionUri(uri).get(1);
  }

  public static String getSuggestionBasename(Uri uri) {
    return checkSuggestionUri(uri).get(2);
  }

  private static List<String> checkSuggestionUri(Uri uri) {
    List<String> segments = uri.getPathSegments();
    checkArgument(segments.size() == 3, segments.size());
    checkArgument(segments.get(0).equals(PATH_SUGGESTION), segments.get(0));
    return segments;
  }

  /**
   * Creates a Uri for querying the list of all bookmarks.
   */
  public static Uri buildBookmarksUri() {
    return bookmarksUriBuilder().build();
  }

  /**
   * Creates a single bookmark Uri to be queried, if the file is currently
   * bookmarked, the resulting cursor will contain exactly one row, otherwise
   * the cursor will be empty.
   */
  public static Uri buildBookmarkUri(String fileId) {
    checkNotNull(fileId, "fileId");
    return bookmarksUriBuilder().appendPath(fileId).build();
  }

  private static Uri.Builder bookmarksUriBuilder() {
    return AUTHORITY_URI.buildUpon().appendPath(PATH_BOOKMARKS);
  }

  public static String getBookmarkFileId(Uri uri) {
    return checkBookmarkUri(uri).get(1);
  }

  private static List<String> checkBookmarkUri(Uri uri) {
    List<String> segments = uri.getPathSegments();
    checkArgument(segments.size() == 2, segments.size());
    checkArgument(segments.get(0).equals(PATH_BOOKMARKS), segments.get(0));
    return segments;
  }

  /**
   * Creates a single file Uri to be queried, if the file exists, the resulting
   * cursor will contain exactly one row, otherwise the cursor will be empty.
   */
  public static Uri buildFileUri(String fileId) {
    checkNotNull(fileId, "fileId");
    return filesUriBuilder().appendPath(fileId).build();
  }

  /**
   * Creates a Uri based on a parent file ID and the name of a file under that
   * parent.
   */
  public static Uri buildFileUri(String parentId, String filename) {
    Uri parentUri = toUri(parentId);
    Uri childUri = parentUri.buildUpon().appendPath(filename).build();
    String childId = toFileId(childUri);
    return buildFileUri(childId);
  }

  /**
   * Creates a Uri for querying the children of the given file.
   */
  public static Uri buildFileChildrenUri(String fileId, boolean showHidden) {
    checkNotNull(fileId, "fileId");
    return filesUriBuilder()
        .appendPath(fileId)
        .appendPath(PATH_CHILDREN)
        .appendQueryParameter(PARAM_SHOW_HIDDEN, showHidden
            ? VALUE_SHOW_HIDDEN_YES
            : VALUE_SHOW_HIDDEN_FILES_NO)
        .build();
  }

  private static Uri.Builder filesUriBuilder() {
    return AUTHORITY_URI.buildUpon().appendPath(PATH_FILES);
  }

  public static String getFileId(File file) {
    return toFileId(file.toURI());
  }

  public static String getFileId(Uri uri) {
    return checkFilesUri(uri).get(1);
  }

  private static List<String> checkFilesUri(Uri uri) {
    List<String> segments = uri.getPathSegments();
    checkArgument(segments.size() >= 2, segments.size());
    checkArgument(segments.get(0).equals(PATH_FILES), segments.get(0));
    return segments;
  }

  /**
   * Gets the path of the file represented by the given content Uri.
   */
  public static Uri getFileSystemUri(Uri contentUri) {
    return Uri.parse(getFileId(contentUri));
  }

  static String toFileId(URI uri) {
    return uri.toString();
  }

  static String toFileId(Uri uri) {
    return uri.toString();
  }

  static String toFileId(File file) {
    return toFileId(file.toURI());
  }

  static URI toURI(String fileId) {
    return URI.create(fileId);
  }

  static Uri toUri(String fileId) {
    return Uri.parse(fileId);
  }

  public static void insertBookmark(ContentResolver resolver, String fileId) {
    resolver.insert(buildBookmarkUri(fileId), null);
  }

  public static void deleteBookmark(ContentResolver resolver, String fileId) {
    resolver.delete(buildBookmarkUri(fileId), null, null);
  }

  /**
   * Creates a new directory.
   *
   * @param parentId the ID of the parent file
   * @param name the name of the new directory
   * @return true if directory created successfully, false otherwise
   */
  public static boolean createDirectory(
      ContentResolver resolver, String parentId, String name) {
    return resolver.insert(buildFileUri(parentId, name), null) != null;
  }

  /**
   * Renames a file.
   *
   * @return true if the file is successfully renamed, false otherwise
   */
  public static boolean rename(
      ContentResolver resolver, String fileId, String newName) {
    Bundle args = new Bundle(1);
    args.putString(EXTRA_NEW_NAME, newName);
    Uri uri = buildFileUri(fileId);
    Bundle result = resolver.call(uri, METHOD_RENAME, fileId, args);
    return result.getBoolean(EXTRA_RESULT);
  }

  public static void deleteFiles(ContentResolver resolver, Set<String> fileIds) {

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

    public static final String SORT_BY_NAME = COLUMN_NAME;
    public static final String SORT_BY_LAST_MODIFIED = COLUMN_LAST_MODIFIED;

    private FileInfo() {}
  }
}
