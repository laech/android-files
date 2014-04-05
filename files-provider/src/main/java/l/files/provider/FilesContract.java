package l.files.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.content.UriMatcher;
import android.net.Uri;
import android.os.Bundle;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.List;

import l.files.common.event.Events;
import l.files.provider.event.LoadFinished;
import l.files.provider.event.LoadProgress;
import l.files.provider.event.LoadStarted;

import static android.content.UriMatcher.NO_MATCH;
import static android.os.Looper.getMainLooper;
import static android.os.Looper.myLooper;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class FilesContract {

  // TODO separate bookmarks to another lib

  static final String PATH_FILES = "files";
  static final String PATH_CHILDREN = "children";
  static final String PATH_BOOKMARKS = "bookmarks";
  static final String PATH_SUGGESTION = "suggestion";
  static final String PATH_HIERARCHY = "hierarchy";

  static final String PARAM_SHOW_HIDDEN = "show-hidden";

  static final int MATCH_FILES_LOCATION = 100;
  static final int MATCH_FILES_LOCATION_CHILDREN = 101;
  static final int MATCH_BOOKMARKS = 200;
  static final int MATCH_BOOKMARKS_LOCATION = 201;
  static final int MATCH_SUGGESTION = 300;
  static final int MATCH_HIERARCHY = 400;

  static final String METHOD_CUT = "cut";
  static final String METHOD_COPY = "copy";
  static final String METHOD_DELETE = "delete";
  static final String METHOD_RENAME = "rename";
  static final String EXTRA_FILE_LOCATION = "file_uri";
  static final String EXTRA_FILE_LOCATIONS = "file_uris";
  static final String EXTRA_NEW_NAME = "new_name";
  static final String EXTRA_DESTINATION_LOCATION = "destination";
  static final String EXTRA_RESULT = "result";

  private static volatile Uri authority;

  private FilesContract() {}

  static UriMatcher newMatcher(Context context) {
    String authority = getAuthorityString(context);
    UriMatcher matcher = new UriMatcher(NO_MATCH);
    matcher.addURI(authority, PATH_BOOKMARKS, MATCH_BOOKMARKS);
    matcher.addURI(authority, PATH_BOOKMARKS + "/*", MATCH_BOOKMARKS_LOCATION);
    matcher.addURI(authority, PATH_FILES + "/*", MATCH_FILES_LOCATION);
    matcher.addURI(authority, PATH_FILES + "/*/" + PATH_CHILDREN, MATCH_FILES_LOCATION_CHILDREN);
    matcher.addURI(authority, PATH_SUGGESTION + "/*/*", MATCH_SUGGESTION);
    matcher.addURI(authority, PATH_HIERARCHY + "/*", MATCH_HIERARCHY);
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
   * Creates a URI for querying the hierarchy of a file. The result cursor will
   * contain the file itself and all the ancestor files of the hierarchy. The
   * first item in the cursor will be the root ancestor, the last item will be
   * the file given there.
   *
   * @param fileLocation the {@link FileInfo#LOCATION} of the file
   */
  public static Uri buildHierarchyUri(Context context, String fileLocation) {
    checkNotNull(fileLocation, "fileLocation");
    return getAuthority(context)
        .buildUpon()
        .appendPath(PATH_HIERARCHY)
        .appendPath(fileLocation)
        .build();
  }

  /**
   * Gets the {@link FileInfo#LOCATION} from the given content URI built with
   * {@link #buildHierarchyUri(Context, String)}
   */
  public static String getHierarchyFileLocation(Uri hierarchyUri) {
    List<String> segments = hierarchyUri.getPathSegments();
    checkArgument(segments.size() == 2, segments.size());
    checkArgument(segments.get(0).equals(PATH_HIERARCHY), segments.get(0));
    return segments.get(1);
  }

  /**
   * A suggestion content URI can be queried, the return cursor will contain a
   * single directory that does not exist, suitable for creating a new directory
   * under that name.
   *
   * @param parentLocation the parent directory's {@link FileInfo#LOCATION}
   * @param basename the basename for the return directory's name, if there is
   * already a file with the same name, a number will be appended
   */
  public static Uri buildSuggestionUri(
      Context context, String parentLocation, String basename) {
    checkNotNull(parentLocation, "parentLocation");
    checkNotNull(basename, "basename");
    return getAuthority(context)
        .buildUpon()
        .appendPath(PATH_SUGGESTION)
        .appendPath(parentLocation)
        .appendPath(basename)
        .build();
  }

  /**
   * Gets the {@link FileInfo#LOCATION} from the given content URI built with
   * {@link #buildSuggestionUri(Context, String, String)}
   */
  public static String getSuggestionParentUri(Uri suggestionUri) {
    return checkSuggestionUri(suggestionUri).get(1);
  }

  /**
   * Gets the basename from the given content URI built with {@link
   * #buildSuggestionUri(Context, String, String)}
   */
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
   * {@link #buildBookmarkUri(Context, String)}.
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

  static Uri buildFilesUri(Context context) {
    return filesUriBuilder(context).build();
  }

  /**
   * Creates a single file content URI to be queried, if the file exists, the
   * resulting cursor will contain exactly one row, otherwise the cursor will be
   * empty.
   *
   * @param fileLocation the {@link FileInfo#LOCATION} of the file
   */
  public static Uri buildFileUri(Context context, String fileLocation) {
    checkNotNull(fileLocation, "fileLocation");
    return filesUriBuilder(context).appendPath(fileLocation).build();
  }

  /**
   * Creates a URI based on a parent file's {@link FileInfo#LOCATION} and the
   * name of a file under that parent.
   */
  public static Uri buildFileUri(
      Context context, String parentLocation, String filename) {
    Uri uri = Uri.parse(parentLocation).buildUpon().appendPath(filename).build();
    return buildFileUri(context, uri.toString());
  }

  /**
   * Creates a URI for querying the children of the given directory.
   * <p/>
   * The progress can also be monitor by registering to {@link Events#bus()} for
   * the following events: {@link LoadStarted}, {@link LoadProgress}, {@link
   * LoadFinished}.
   */
  public static Uri buildFileChildrenUri(
      Context context, String directoryLocation, boolean showHidden) {
    checkNotNull(directoryLocation, "directoryLocation");
    return filesUriBuilder(context)
        .appendPath(directoryLocation)
        .appendPath(PATH_CHILDREN)
        .appendQueryParameter(PARAM_SHOW_HIDDEN, Boolean.toString(showHidden))
        .build();
  }

  private static Uri.Builder filesUriBuilder(Context context) {
    return getAuthority(context).buildUpon().appendPath(PATH_FILES);
  }

  /**
   * Gets the {@link FileInfo#LOCATION} of the given file.
   */
  public static String getFileLocation(File file) {
    /* TODO test this
     * Don't use File.toURI as it will append a "/" to the end of the URI
     * depending on whether or not the file is a directory, that means two calls
     * to the method before and after the directory is deleted will create two
     * URIs that are not equal. This will cause problems else where such as when
     * you try to remove a record from the database after a directory is
     * deleted. File.getAbsolutePath doesn't not have this problem and is safe.
     */
    return URI.create(Uri.fromFile(file).toString()).normalize().toString();
  }

  /**
   * Gets the {@link FileInfo#LOCATION} from the given URI built with {@link
   * #buildFileUri(Context, String)}.
   */
  public static String getFileLocation(Uri uri) {
    return checkFilesUri(uri).get(1);
  }

  private static List<String> checkFilesUri(Uri uri) {
    List<String> segments = uri.getPathSegments();
    checkArgument(segments.size() >= 2, segments.size());
    checkArgument(segments.get(0).equals(PATH_FILES), segments.get(0));
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

  /**
   * Creates a new directory. Do not call this on the UI thread.
   *
   * @param parentLocation the {@link FileInfo#LOCATION} of the parent
   * @param name the name of the new directory
   * @return true if directory created successfully, false otherwise
   */
  public static boolean createDirectory(
      Context context, String parentLocation, String name) {
    ensureNonMainThread();
    Uri uri = buildFileUri(context, parentLocation, name);
    return context.getContentResolver().insert(uri, null) != null;
  }

  /**
   * Renames a file.
   *
   * @return true if the file is successfully renamed, false otherwise
   */
  public static boolean rename(
      Context context, String fileLocation, String newName) {

    ensureNonMainThread();
    Bundle args = new Bundle(2);
    args.putString(EXTRA_FILE_LOCATION, fileLocation);
    args.putString(EXTRA_NEW_NAME, newName);
    Uri uri = buildFileUri(context, fileLocation);
    ContentResolver resolver = context.getContentResolver();
    Bundle result = resolver.call(uri, METHOD_RENAME, null, args);
    return result.getBoolean(EXTRA_RESULT);
  }

  /**
   * Deletes the files identified by the given {@link FileInfo#LOCATION}s. Do
   * not call this on the UI thread.
   */
  public static void delete(Context context, Collection<String> fileLocations) {
    ensureNonMainThread();
    String[] array = fileLocations.toArray(new String[fileLocations.size()]);
    Bundle args = new Bundle(1);
    args.putStringArray(EXTRA_FILE_LOCATIONS, array);
    Uri uri = buildFilesUri(context);
    context.getContentResolver().call(uri, METHOD_DELETE, null, args);
  }

  /**
   * Copies the files identified by the given {@link FileInfo#LOCATION}s to the
   * destination {@link FileInfo#LOCATION}. Do not call this on the UI thread.
   */
  public static void copy(
      Context context, Collection<String> locations, String dstLocation) {
    paste(context, locations, dstLocation, METHOD_COPY);
  }

  /**
   * Moves the files identified by the given {@link FileInfo#LOCATION}s to the
   * destination {@link FileInfo#LOCATION}. Do not call this on the UI thread.
   */
  public static void cut(
      Context context, Collection<String> fileLocations, String dstLocation) {
    paste(context, fileLocations, dstLocation, METHOD_CUT);
  }

  private static void paste(
      Context context,
      Collection<String> fileLocations,
      String dstLocation,
      String method) {

    ensureNonMainThread();
    String[] array = fileLocations.toArray(new String[fileLocations.size()]);
    Bundle args = new Bundle(2);
    args.putStringArray(EXTRA_FILE_LOCATIONS, array);
    args.putString(EXTRA_DESTINATION_LOCATION, dstLocation);
    Uri uri = buildFileUri(context, dstLocation);
    context.getContentResolver().call(uri, method, null, args);
  }

  private static void ensureNonMainThread() {
    if (myLooper() == getMainLooper()) {
      throw new IllegalStateException("Don't call this from the main thread");
    }
  }

  /**
   * Represents a file.
   */
  public final static class FileInfo {

    /**
     * The location of a file, this is actually the file system URI of the file,
     * but named location instead of URI to avoid confusion with Android's
     * content URI.
     * <p/>
     * Type: STRING
     */
    public static final String LOCATION = "location";

    /**
     * Concrete MIME type of a file. For example, "image/png" or
     * "application/pdf", or directory - {@link #MIME_DIR}.
     * <p/>
     * Type: STRING
     *
     * @see #MIME_DIR
     */
    public static final String MIME = "mime";

    /**
     * Name of a file.
     * <p/>
     * Type: STRING
     */
    public static final String NAME = "name";

    /**
     * Timestamp when a file was last modified, in milliseconds since January 1,
     * 1970 00:00:00.0 UTC.
     * <p/>
     * Type: INTEGER (long)
     */
    public static final String MODIFIED = "modified";

    /**
     * Size of a file, in bytes.
     * <p/>
     * Type: INTEGER (long)
     */
    public static final String SIZE = "size";

    /**
     * Flag indicating that a file is writable.
     * <p/>
     * Type: BOOLEAN
     */
    public static final String WRITABLE = "writable";

    /**
     * Flag indicating that a file is readable.
     * <p/>
     * Type: BOOLEAN
     */
    public static final String READABLE = "readable";

    /**
     * Flag indicating that a file is hidden.
     * <p/>
     * Type: BOOLEAN
     */
    public static final String HIDDEN = "hidden";

    /**
     * Media type of a directory.
     *
     * @see #MIME
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

    FileInfo() {}
  }
}
