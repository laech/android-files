package l.files.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.List;

import l.files.R;
import l.files.fs.FileId;

import static android.content.UriMatcher.NO_MATCH;
import static android.os.Looper.getMainLooper;
import static android.os.Looper.myLooper;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.common.database.Cursors.getInt;
import static l.files.common.database.Cursors.getLong;
import static l.files.common.database.Cursors.getString;
import static l.files.common.database.DataTypes.intToBoolean;

public final class FilesContract {

  static final String PATH_FILES = "files";
  static final String PATH_CHILDREN = "children";
  static final String PATH_HIERARCHY = "hierarchy";
  static final String PATH_SELECTION = "selection";

  static final String PARAM_SHOW_HIDDEN = "show-hidden";
  static final String PARAM_SELECTION = "selection";

  static final int MATCH_FILES_ID = 100;
  static final int MATCH_FILES_ID_CHILDREN = 101;
  static final int MATCH_HIERARCHY = 400;
  static final int MATCH_SELECTION = 500;

  static final String METHOD_CUT = "cut";
  static final String METHOD_COPY = "copy";
  static final String METHOD_DELETE = "delete";
  static final String METHOD_RENAME = "rename";
  static final String METHOD_SUGGESTION = "suggestion";
  static final String EXTRA_FILE_ID = "file";
  static final String EXTRA_FILE_IDS = "files";
  static final String EXTRA_NEW_NAME = "new_name";
  static final String EXTRA_DESTINATION_ID = "destination";
  static final String EXTRA_ERROR = "error";

  private static volatile Uri authority;

  private FilesContract() {
  }

  static UriMatcher newMatcher(Context context) {
    String authority = getAuthorityString(context);
    UriMatcher matcher = new UriMatcher(NO_MATCH);
    matcher.addURI(authority, PATH_FILES + "/*", MATCH_FILES_ID);
    matcher.addURI(authority, PATH_FILES + "/*/" + PATH_CHILDREN, MATCH_FILES_ID_CHILDREN);
    matcher.addURI(authority, PATH_HIERARCHY + "/*", MATCH_HIERARCHY);
    matcher.addURI(authority, PATH_SELECTION, MATCH_SELECTION);
    return matcher;
  }

  private static Uri getAuthority(Context context) {
    if (authority == null) {
      authority = Uri.parse("content://" + getAuthorityString(context));
    }
    return authority;
  }

  private static String getAuthorityString(Context context) {
    return context.getString(R.string.files_provider_authority);
  }

  /**
   * Gets a URI for querying the hierarchy of a file. The result cursor will
   * contain the file itself and all the ancestor files of the hierarchy. The
   * first item in the cursor will be the root ancestor, the last item will be
   * the file given there.
   */
  public static Uri getHierarchyUri(Context context, String id) {
    checkNotNull(id, "id");
    return getAuthority(context)
        .buildUpon()
        .appendPath(PATH_HIERARCHY)
        .appendPath(id)
        .build();
  }

  /**
   * Gets the ID from the given content URI built with
   * {@link #getHierarchyUri(Context, String)}
   */
  public static String getHierarchyFileId(Uri hierarchyUri) {
    List<String> segments = hierarchyUri.getPathSegments();
    checkArgument(segments.size() == 2, segments.size());
    checkArgument(segments.get(0).equals(PATH_HIERARCHY), segments.get(0));
    return segments.get(1);
  }

  static Uri getFilesUri(Context context) {
    return getFilesUriBuilder(context).build();
  }

  /**
   * Gets a single file content URI to be queried, if the file exists, the
   * resulting cursor will contain exactly one row, otherwise the cursor will be
   * empty.
   */
  public static Uri getFileUri(Context context, File f) {
    return getFileUri(context, getFileId(f));
  }

  /**
   * Gets a single file content URI to be queried, if the file exists, the
   * resulting cursor will contain exactly one row, otherwise the cursor will be
   * empty.
   */
  public static Uri getFileUri(Context context, String id) {
    checkNotNull(id, "id");
    return getFilesUriBuilder(context).appendPath(id).build();
  }

  /**
   * Gets a URI based on a parent file's ID and the
   * name of a file under that parent.
   */
  public static Uri getFileUri(Context context, String parentId, String name) {
    Uri uri = Uri.parse(parentId).buildUpon().appendPath(name).build();
    return getFileUri(context, uri.toString());
  }

  /**
   * Gets a URI for querying the children of the given directory.
   */
  public static Uri getFilesUri(Context context, File dir, boolean showHidden) {
    return getFilesUri(context, getFileId(dir), showHidden);
  }

  /**
   * Gets a URI for querying the children of the given directory.
   */
  public static Uri getFilesUri(Context context, String dirId, boolean showHidden) {
    checkNotNull(dirId, "dirId");
    return getFilesUriBuilder(context)
        .appendPath(dirId)
        .appendPath(PATH_CHILDREN)
        .appendQueryParameter(PARAM_SHOW_HIDDEN, Boolean.toString(showHidden))
        .build();
  }

  private static Uri.Builder getFilesUriBuilder(Context context) {
    return getAuthority(context).buildUpon().appendPath(PATH_FILES);
  }

  /**
   * Gets a URI for querying the given files.
   */
  public static Uri getSelectionUri(Context context, String... ids) {
    Uri.Builder builder = getSelectionUriBuilder(context);
    for (String id : ids) {
      builder.appendQueryParameter(PARAM_SELECTION, id);
    }
    return builder.build();
  }

  private static Uri.Builder getSelectionUriBuilder(Context context) {
    return getAuthority(context).buildUpon().appendPath(PATH_SELECTION);
  }

  static File getFile(String fileId) {
    return new File(URI.create(fileId));
  }

  public static String getFileId(File file) {
    return FileId.of(file).toString();
  }

  /**
   * Gets the file ID from the given URI built with
   * {@link #getFileUri(Context, String)}.
   */
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
   * Creates a new directory.
   *
   * @param parentId the ID of the parent
   * @param name     the name of the new directory
   * @return true if directory created successfully, false otherwise
   * @throws IllegalStateException if called from main thread
   */
  public static boolean createDirectory(Context context, String parentId, String name) {
    ensureNonMainThread();
    Uri uri = getFileUri(context, parentId, name);
    return context.getContentResolver().insert(uri, null) != null;
  }

  /**
   * Gets a name that does not exist at the given directory ID.
   *
   * @param parentId the parent directory's ID
   * @param baseName the basename for the return directory's name, if there is
   *                 already a file with the same name, a number will be
   *                 appended
   * @throws IllegalStateException if called from main thread
   */
  public static String getNameSuggestion(
      Context context, String parentId, String baseName) {
    ensureNonMainThread();
    Bundle args = new Bundle(2);
    args.putString(EXTRA_FILE_ID, parentId);
    args.putString(EXTRA_NEW_NAME, baseName);
    Uri uri = getFilesUri(context);
    ContentResolver resolver = context.getContentResolver();
    Bundle result = resolver.call(uri, METHOD_SUGGESTION, null, args);
    return result.getString(EXTRA_NEW_NAME);
  }

  /**
   * Renames a file with a new name in the same directory.
   *
   * @throws IOException           if any error occurs
   * @throws IllegalStateException if called from main thread
   */
  public static void rename(
      Context context, String id, String newName) throws IOException {
    ensureNonMainThread();
    Bundle args = new Bundle(2);
    args.putString(EXTRA_FILE_ID, id);
    args.putString(EXTRA_NEW_NAME, newName);
    Uri uri = getFileUri(context, id);
    ContentResolver resolver = context.getContentResolver();
    Bundle result = resolver.call(uri, METHOD_RENAME, null, args);
    IOException e = (IOException) result.getSerializable(EXTRA_ERROR);
    if (e != null) {
      throw e;
    }
  }

  /**
   * Deletes the files identified by the given IDs.
   *
   * @throws IllegalStateException if called from main thread
   */
  public static void delete(Context context, Collection<String> ids) {
    ensureNonMainThread();
    String[] array = ids.toArray(new String[ids.size()]);
    Bundle args = new Bundle(1);
    args.putStringArray(EXTRA_FILE_IDS, array);
    Uri uri = getFilesUri(context);
    context.getContentResolver().call(uri, METHOD_DELETE, null, args);
  }

  /**
   * Copies the files identified by the given IDs to the
   * destination directory. Do not call this on the UI thread.
   *
   * @throws IllegalStateException if called from main thread
   */
  public static void copy(Context context, Collection<String> srcIds, String dstId) {
    paste(context, srcIds, dstId, METHOD_COPY);
  }

  /**
   * Moves the files identified by the given IDs to the
   * destination directory. Do not call this on the UI thread.
   */
  public static void move(Context context, Collection<String> srcIds, String dstId) {
    paste(context, srcIds, dstId, METHOD_CUT);
  }

  private static void paste(
      Context context, Collection<String> srcIds, String dstId, String method) {

    ensureNonMainThread();
    String[] array = srcIds.toArray(new String[srcIds.size()]);
    Bundle args = new Bundle(2);
    args.putStringArray(EXTRA_FILE_IDS, array);
    args.putString(EXTRA_DESTINATION_ID, dstId);
    Uri uri = getFileUri(context, dstId);
    context.getContentResolver().call(uri, method, null, args);
  }

  private static void ensureNonMainThread() {
    if (myLooper() == getMainLooper()) {
      throw new IllegalStateException("Don't call this from the main thread");
    }
  }

  public final static class Files {
    private Files() {}

    public static final String ID = "id";
    public static final String MIME = "mime";
    public static final String NAME = "name";
    public static final String MODIFIED = "modified";
    public static final String LENGTH = "length";
    public static final String WRITABLE = "writable";
    public static final String READABLE = "readable";
    public static final String TYPE = "type";

    public static final String TYPE_DIRECTORY = "directory";
    public static final String TYPE_SYMLINK = "symlink";
    public static final String TYPE_REGULAR_FILE = "regular-file";
    public static final String TYPE_UNKNOWN = "unknown";

    /**
     * Media type of a directory.
     */
    public static final String MIME_DIR = "application/x-directory";

    /**
     * Sorts the files by their names alphabetically.
     */
    public static final String SORT_BY_NAME = SortBy.NAME.name();

    /**
     * Sorts the files by their last modified dates descending).
     */
    public static final String SORT_BY_MODIFIED = SortBy.DATE.name();

    /**
     * Sorts the files by their sizes (descending). Sizes of directories will
     * not be calculated, as such their will appear at the end of the list.
     */
    public static final String SORT_BY_SIZE = SortBy.SIZE.name();

    static final String[] COLUMNS = {
        ID,
        NAME,
        LENGTH,
        READABLE,
        WRITABLE,
        MIME,
        MODIFIED,
        TYPE,
    };

    public static String id(Cursor cursor) {
      return getString(cursor, ID);
    }

    /**
     * Concrete MIME type of a file. For example, "image/png" or
     * "application/pdf", or directory - {@link #MIME_DIR}.
     * <p/>
     * Type: STRING
     */
    public static String mime(Cursor cursor) {
      return getString(cursor, MIME);
    }

    public static String name(Cursor cursor) {
      return getString(cursor, NAME);
    }

    /**
     * Timestamp when a file was last modified, in milliseconds.
     */
    public static long modified(Cursor cursor) {
      return getLong(cursor, MODIFIED);
    }

    /**
     * Length of a file, in bytes.
     */
    public static long length(Cursor cursor) {
      return getLong(cursor, LENGTH);
    }

    public static boolean isReadable(Cursor cursor) {
      return intToBoolean(getInt(cursor, READABLE));
    }

    public static boolean isWritable(Cursor cursor) {
      return intToBoolean(getInt(cursor, WRITABLE));
    }

    public static boolean isDirectory(Cursor cursor) {
      return MIME_DIR.equals(mime(cursor));
    }

    public static String type(Cursor cursor) {
      return getString(cursor, TYPE);
    }
  }
}
