package l.files.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.util.Log;

import org.apache.tika.Tika;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Set;

import l.files.analytics.Analytics;
import l.files.common.io.Files;
import l.files.service.CopyService;
import l.files.service.DeleteService;
import l.files.service.MoveService;

import static android.os.ParcelFileDescriptor.MODE_READ_ONLY;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSetWithExpectedSize;
import static java.util.Arrays.sort;
import static java.util.Collections.reverse;
import static l.files.common.io.Files.normalize;
import static l.files.provider.Bookmarks.getBookmark;
import static l.files.provider.Bookmarks.getBookmarks;
import static l.files.provider.Bookmarks.getBookmarksCount;
import static l.files.provider.Bookmarks.isBookmarksKey;
import static l.files.provider.BuildConfig.DEBUG;
import static l.files.provider.FileData.LAST_MODIFIED_COMPARATOR_REVERSE;
import static l.files.provider.FileData.NAME_COMPARATOR;
import static l.files.provider.FilesContract.EXTRA_DESTINATION_LOCATION;
import static l.files.provider.FilesContract.EXTRA_FILE_LOCATION;
import static l.files.provider.FilesContract.EXTRA_FILE_LOCATIONS;
import static l.files.provider.FilesContract.EXTRA_NEW_NAME;
import static l.files.provider.FilesContract.EXTRA_RESULT;
import static l.files.provider.FilesContract.FileInfo;
import static l.files.provider.FilesContract.FileInfo.MIME_DIR;
import static l.files.provider.FilesContract.FileInfo.SORT_BY_MODIFIED;
import static l.files.provider.FilesContract.FileInfo.SORT_BY_NAME;
import static l.files.provider.FilesContract.MATCH_BOOKMARKS;
import static l.files.provider.FilesContract.MATCH_BOOKMARKS_LOCATION;
import static l.files.provider.FilesContract.MATCH_FILES_LOCATION;
import static l.files.provider.FilesContract.MATCH_FILES_LOCATION_CHILDREN;
import static l.files.provider.FilesContract.MATCH_HIERARCHY;
import static l.files.provider.FilesContract.MATCH_SUGGESTION;
import static l.files.provider.FilesContract.METHOD_COPY;
import static l.files.provider.FilesContract.METHOD_CUT;
import static l.files.provider.FilesContract.METHOD_DELETE;
import static l.files.provider.FilesContract.METHOD_RENAME;
import static l.files.provider.FilesContract.PARAM_SHOW_HIDDEN;
import static l.files.provider.FilesContract.VALUE_SHOW_HIDDEN_YES;
import static l.files.provider.FilesContract.buildBookmarksUri;
import static l.files.provider.FilesContract.getBookmarkLocation;
import static l.files.provider.FilesContract.getFileLocation;
import static l.files.provider.FilesContract.getHierarchyFileLocation;
import static l.files.provider.FilesContract.getSuggestionBasename;
import static l.files.provider.FilesContract.getSuggestionParentUri;
import static l.files.provider.FilesContract.newMatcher;

public final class FilesProvider extends ContentProvider
    implements SharedPreferences.OnSharedPreferenceChangeListener {

  private static final String TAG = FilesProvider.class.getSimpleName();

  private static final String[] DEFAULT_COLUMNS = {
      FileInfo.LOCATION,
      FileInfo.NAME,
      FileInfo.LENGTH,
      FileInfo.READABLE,
      FileInfo.WRITABLE,
      FileInfo.MIME,
      FileInfo.MODIFIED,
  };

  private static final UriMatcher matcher = newMatcher();

  @Override public boolean onCreate() {
    getPreference().registerOnSharedPreferenceChangeListener(this);
    return true;
  }

  @Override public String getType(Uri uri) {
    switch (matcher.match(uri)) {
      case MATCH_FILES_LOCATION:
        String location = getFileLocation(uri);
        File file = new File(URI.create(location));
        if (file.isDirectory()) {
          return MIME_DIR;
        }
        try {
          return detectMime(file);
        } catch (IOException e) {
          Log.w(TAG, e.getMessage(), e);
          return null;
        }
    }
    throw new UnsupportedOperationException("Unsupported Uri: " + uri);
  }

  private String detectMime(File file) throws IOException {
    long start = 0;
    if (DEBUG) {
      start = SystemClock.elapsedRealtime();
    }

    String mime = TikaHolder.TIKA.detect(file);

    if (DEBUG) {
      long end = SystemClock.elapsedRealtime();
      Log.d(TAG, "Detected " + mime + " in " + (end - start) + " ms: " + file);
    }

    return mime;
  }

  @Override public ParcelFileDescriptor openFile(Uri uri, String mode)
      throws FileNotFoundException {
    switch (matcher.match(uri)) {
      case MATCH_FILES_LOCATION:
        File file = new File(URI.create(getFileLocation(uri)));
        return ParcelFileDescriptor.open(file, MODE_READ_ONLY);
    }
    return super.openFile(uri, mode);
  }

  @Override public Cursor query(
      Uri uri,
      String[] projection,
      String selection,
      String[] selectionArgs,
      String sortOrder) {
    return doQuery(uri, projection, sortOrder, null);
  }

  @Override public Cursor query(
      Uri uri,
      String[] projection,
      String selection,
      String[] selectionArgs,
      String sortOrder,
      CancellationSignal signal) {
    return doQuery(uri, projection, sortOrder, signal);
  }

  private Cursor doQuery(
      Uri uri,
      String[] projection,
      String sortOrder,
      CancellationSignal signal) {
    if (projection == null) projection = DEFAULT_COLUMNS;

    switch (matcher.match(uri)) {
      case MATCH_SUGGESTION:
        return querySuggestion(uri, projection);
      case MATCH_BOOKMARKS:
        return queryBookmarks(uri, projection);
      case MATCH_BOOKMARKS_LOCATION:
        return queryBookmark(uri, projection);
      case MATCH_FILES_LOCATION:
        return queryFile(uri, projection);
      case MATCH_FILES_LOCATION_CHILDREN:
        return queryFiles(uri, projection, sortOrder, signal);
      case MATCH_HIERARCHY:
        return queryHierarchy(uri, projection);
      default:
        throw new UnsupportedOperationException("Unsupported Uri: " + uri);
    }
  }

  private Cursor queryHierarchy(Uri uri, String[] projection) {
    File file = normalize(new File(URI.create(getHierarchyFileLocation(uri))));
    List<File> files = newArrayList();
    while (file != null) {
      files.add(file);
      file = file.getParentFile();
    }
    reverse(files);
    File[] fileArray = files.toArray(new File[files.size()]);
    return newFileCursor(uri, projection, FileData.from(fileArray));
  }

  private Cursor queryFile(Uri uri, String[] projection) {
    File file = new File(URI.create(getFileLocation(uri)));
    File[] files = file.exists() ? new File[]{file} : new File[0];
    return new FileCursor(FileData.from(files), projection);
  }

  private Cursor querySuggestion(Uri uri, String[] projection) {
    File parent = new File(URI.create(getSuggestionParentUri(uri)));
    String name = getSuggestionBasename(uri);
    File file = new File(parent, name);
    for (int i = 2; file.exists(); i++) {
      file = new File(parent, name + " " + i);
    }
    return newFileCursor(uri, projection, new FileData(file));
  }

  private Cursor queryBookmark(Uri uri, String[] projection) {
    File[] bookmarks = getBookmark(getPreference(), getBookmarkLocation(uri));
    return newFileCursor(uri, projection, FileData.from(bookmarks));
  }

  private Cursor queryBookmarks(Uri uri, String[] projection) {
    File[] bookmarks = getBookmarks(getPreference());
    return newFileCursor(uri, projection, FileData.from(bookmarks));
  }

  /*
   * Calling methods on java.io.File (e.g. lastModified, length, etc) is
   * relatively expensive (except getName as it doesn't require a call to the
   * OS), on a large directory, this process can take minutes (Galaxy Nexus,
   * /storage/emulated/0/DCIM/.thumbnails with ~20,000 files took 1 to 4 minutes
   * to load, where File.listFiles took around 2 ~ 3 seconds to return),
   * therefore a CancellationSignal is used to make sure it stops as soon as
   * possible when the result is no longer needed - i.e. the user doesn't want
   * to wait anymore and presses the back button.
   * <p/>
   * It doesn't seem to make a difference in time whether only one property
   * method is called or all properties are called. Ideally such calls to the
   * properties can be avoided until needed, but they are need upfront because
   * of sorting, such as sorting by last modified date.
   * <p/>
   * Note that when using a CursorLoader, don't use the support-v4 version as it
   * doesn't not use a CancellationSignal.
   */
  private Cursor queryFiles(
      Uri uri,
      String[] projection,
      String sortOrder,
      CancellationSignal signal) {

    boolean showHidden = VALUE_SHOW_HIDDEN_YES
        .equals(uri.getQueryParameter(PARAM_SHOW_HIDDEN));

    File parent = new File(URI.create(getFileLocation(uri)));
    String[] children = Files.list(parent, showHidden);
    if (children == null) {
      children = new String[0];
    }

    FileData[] data = new FileData[children.length];
    Set<String> dirs = newHashSetWithExpectedSize(children.length);
    for (int i = 0; i < children.length; i++) {
      File file = new File(parent, children[i]);
      if (signal != null) {
        signal.throwIfCanceled();
      }
      if (file.isDirectory()) {
        dirs.add(file.getAbsolutePath());
      }
      data[i] = new FileData(file);
    }

    switch (sortOrder) {
      case SORT_BY_MODIFIED:
        sort(data, LAST_MODIFIED_COMPARATOR_REVERSE);
        break;
      case SORT_BY_NAME:
      default:
        sort(data, NAME_COMPARATOR);
        break;
    }

    return newMonitoringCursor(uri, parent.getAbsolutePath(), dirs, projection, data);
  }

  @Override public Bundle call(String method, String arg, Bundle extras) {
    switch (method) {
      case METHOD_RENAME:
        return callRename(extras);
      case METHOD_DELETE:
        return callDelete(extras);
      case METHOD_COPY:
        return callCopy(extras);
      case METHOD_CUT:
        return callCut(extras);
    }
    return super.call(method, arg, extras);
  }

  private Bundle callCut(Bundle extras) {
    Set<File> files = toFilesSet(extras.getStringArray(EXTRA_FILE_LOCATIONS));
    File destination = new File(URI.create(extras.getString(EXTRA_DESTINATION_LOCATION)));
    MoveService.start(getContext(), files, destination);
    return Bundle.EMPTY;
  }

  private Bundle callCopy(Bundle extras) {
    Set<File> files = toFilesSet(extras.getStringArray(EXTRA_FILE_LOCATIONS));
    File destination = new File(URI.create(extras.getString(EXTRA_DESTINATION_LOCATION)));
    CopyService.start(getContext(), files, destination);
    return Bundle.EMPTY;
  }

  private Bundle callRename(Bundle extras) {
    File from = new File(URI.create(extras.getString(EXTRA_FILE_LOCATION)));
    File to = new File(from.getParent(), extras.getString(EXTRA_NEW_NAME));
    Bundle out = new Bundle(1);
    out.putBoolean(EXTRA_RESULT, from.renameTo(to));
    return out;
  }

  private Bundle callDelete(Bundle extras) {
    Set<File> files = toFilesSet(extras.getStringArray(EXTRA_FILE_LOCATIONS));
    DeleteService.delete(getContext(), files);
    return Bundle.EMPTY;
  }

  private Set<File> toFilesSet(String[] fileLocations) {
    Set<File> files = newHashSetWithExpectedSize(fileLocations.length);
    for (String location : fileLocations) {
      files.add(new File(URI.create(location)));
    }
    return files;
  }

  @Override public Uri insert(Uri uri, ContentValues values) {
    switch (matcher.match(uri)) {
      case MATCH_BOOKMARKS_LOCATION:
        return insertBookmark(uri);
      case MATCH_FILES_LOCATION:
        return insertDirectory(uri);
    }
    throw new UnsupportedOperationException("Unsupported Uri: " + uri);
  }

  private Uri insertBookmark(Uri uri) {
    Bookmarks.add(getPreference(), getBookmarkLocation(uri));
    return uri;
  }

  private Uri insertDirectory(Uri uri) {
    File file = new File(URI.create(getFileLocation(uri)));
    return (file.mkdirs() || file.isDirectory()) ? uri : null;
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    switch (matcher.match(uri)) {
      case MATCH_BOOKMARKS_LOCATION:
        return deleteBookmark(uri);
    }
    throw new UnsupportedOperationException("Unsupported Uri: " + uri);
  }

  private int deleteBookmark(Uri uri) {
    Bookmarks.remove(getPreference(), getBookmarkLocation(uri));
    return 1;
  }

  @Override public int update(
      Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    throw new UnsupportedOperationException("Update not supported");
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
    if (isBookmarksKey(key)) {
      getContentResolver().notifyChange(buildBookmarksUri(), null);
      String count = Integer.toString(getBookmarksCount(pref));
      Analytics.onPreferenceChanged(getContext(), key, count);
    }
  }

  private Cursor newFileCursor(Uri uri, String[] projection, FileData... files) {
    Cursor c = new FileCursor(files, projection);
    c.setNotificationUri(getContentResolver(), uri);
    return c;
  }

  private Cursor newMonitoringCursor(
      Uri uri, String dir, Set<String> subDirs, String[] projection, FileData[] files) {
    Cursor cursor = newFileCursor(uri, projection, files);
    return MonitoringCursor.create(getContentResolver(), uri, dir, subDirs, cursor);
  }

  private ContentResolver getContentResolver() {
    return getContext().getContentResolver();
  }

  private SharedPreferences getPreference() {
    return getDefaultSharedPreferences(getContext());
  }

  private static class TikaHolder {
    static final Tika TIKA = new Tika();
  }
}
