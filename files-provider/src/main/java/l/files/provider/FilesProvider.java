package l.files.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;

import org.apache.tika.Tika;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Set;

import l.files.analytics.Analytics;
import l.files.common.logging.Logger;
import l.files.service.CopyService;
import l.files.service.DeleteService;
import l.files.service.MoveService;

import static android.os.ParcelFileDescriptor.MODE_READ_ONLY;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSetWithExpectedSize;
import static java.util.Collections.reverse;
import static l.files.common.io.Files.normalize;
import static l.files.provider.Bookmarks.getBookmark;
import static l.files.provider.Bookmarks.getBookmarks;
import static l.files.provider.Bookmarks.getBookmarksCount;
import static l.files.provider.Bookmarks.isBookmarksKey;
import static l.files.provider.BuildConfig.DEBUG;
import static l.files.provider.FilesContract.EXTRA_DESTINATION_LOCATION;
import static l.files.provider.FilesContract.EXTRA_FILE_LOCATION;
import static l.files.provider.FilesContract.EXTRA_FILE_LOCATIONS;
import static l.files.provider.FilesContract.EXTRA_NEW_NAME;
import static l.files.provider.FilesContract.EXTRA_RESULT;
import static l.files.provider.FilesContract.FileInfo;
import static l.files.provider.FilesContract.FileInfo.MIME_DIR;
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
import static l.files.provider.FilesContract.buildBookmarksUri;
import static l.files.provider.FilesContract.getBookmarkLocation;
import static l.files.provider.FilesContract.getFileLocation;
import static l.files.provider.FilesContract.getHierarchyFileLocation;
import static l.files.provider.FilesContract.getSuggestionBasename;
import static l.files.provider.FilesContract.getSuggestionParentUri;
import static l.files.provider.FilesContract.newMatcher;

public final class FilesProvider extends ContentProvider
    implements SharedPreferences.OnSharedPreferenceChangeListener {

  private static final Logger logger = Logger.get(FilesProvider.class);

  private static final String[] DEFAULT_COLUMNS = {
      FileInfo.LOCATION,
      FileInfo.NAME,
      FileInfo.SIZE,
      FileInfo.READABLE,
      FileInfo.WRITABLE,
      FileInfo.MIME,
      FileInfo.MODIFIED,
      FileInfo.HIDDEN,
  };

  private UriMatcher matcher;
  private FilesDb helper;

  @Override public boolean onCreate() {
    matcher = newMatcher(getContext());
    helper = new FilesDb(getContext());
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
          logger.warn(e);
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
      logger.debug("Detected %s in %s ms: %s", mime, (end - start), file);
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

    if (projection == null) {
      projection = DEFAULT_COLUMNS;
    }

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
        return queryFiles(uri, projection, selection, selectionArgs, sortOrder);
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
    return newFileCursor(uri, projection, FileData.from(file));
  }

  private Cursor queryBookmark(Uri uri, String[] projection) {
    File[] bookmarks = getBookmark(getPreference(), getBookmarkLocation(uri));
    return newFileCursor(uri, projection, FileData.from(bookmarks));
  }

  private Cursor queryBookmarks(Uri uri, String[] projection) {
    File[] bookmarks = getBookmarks(getPreference());
    return newFileCursor(uri, projection, FileData.from(bookmarks));
  }

  private Cursor queryFiles(
      Uri uri,
      String[] projection,
      String selection,
      String[] selectArgs,
      String sortOrder) {
    return helper.query(uri, projection, selection, selectArgs, sortOrder);
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
      getContentResolver().notifyChange(buildBookmarksUri(getContext()), null);
      String count = Integer.toString(getBookmarksCount(pref));
      Analytics.onPreferenceChanged(getContext(), key, count);
    }
  }

  private Cursor newFileCursor(Uri uri, String[] projection, FileData... files) {
    Cursor c = new FileCursor(files, projection);
    c.setNotificationUri(getContentResolver(), uri);
    return c;
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
