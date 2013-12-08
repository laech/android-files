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

import org.apache.tika.Tika;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import l.files.service.CopyService;
import l.files.service.DeleteService;
import l.files.service.MoveService;

import static android.os.ParcelFileDescriptor.MODE_READ_ONLY;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSetWithExpectedSize;
import static java.util.Arrays.sort;
import static java.util.Collections.reverse;
import static l.files.common.io.Files.listFiles;
import static l.files.common.io.Files.normalize;
import static l.files.provider.Bookmarks.getBookmark;
import static l.files.provider.Bookmarks.getBookmarks;
import static l.files.provider.FilesContract.EXTRA_DESTINATION_ID;
import static l.files.provider.FilesContract.EXTRA_FILE_ID;
import static l.files.provider.FilesContract.EXTRA_FILE_IDS;
import static l.files.provider.FilesContract.EXTRA_NEW_NAME;
import static l.files.provider.FilesContract.EXTRA_RESULT;
import static l.files.provider.FilesContract.FileInfo;
import static l.files.provider.FilesContract.FileInfo.MEDIA_TYPE_DIR;
import static l.files.provider.FilesContract.FileInfo.SORT_BY_LAST_MODIFIED;
import static l.files.provider.FilesContract.FileInfo.SORT_BY_NAME;
import static l.files.provider.FilesContract.MATCH_BOOKMARKS;
import static l.files.provider.FilesContract.MATCH_BOOKMARKS_ID;
import static l.files.provider.FilesContract.MATCH_FILES_ID;
import static l.files.provider.FilesContract.MATCH_FILES_ID_CHILDREN;
import static l.files.provider.FilesContract.MATCH_HIERARCHY;
import static l.files.provider.FilesContract.MATCH_SUGGESTION;
import static l.files.provider.FilesContract.METHOD_COPY;
import static l.files.provider.FilesContract.METHOD_CUT;
import static l.files.provider.FilesContract.METHOD_DELETE;
import static l.files.provider.FilesContract.METHOD_RENAME;
import static l.files.provider.FilesContract.PARAM_SHOW_HIDDEN;
import static l.files.provider.FilesContract.VALUE_SHOW_HIDDEN_YES;
import static l.files.provider.FilesContract.buildBookmarksUri;
import static l.files.provider.FilesContract.getBookmarkFileId;
import static l.files.provider.FilesContract.getFileId;
import static l.files.provider.FilesContract.getHierarchyFileId;
import static l.files.provider.FilesContract.getSuggestionBasename;
import static l.files.provider.FilesContract.getSuggestionParentId;
import static l.files.provider.FilesContract.newMatcher;
import static l.files.provider.FilesContract.toURI;
import static org.apache.commons.io.comparator.LastModifiedFileComparator.LASTMODIFIED_REVERSE;
import static org.apache.commons.io.comparator.NameFileComparator.NAME_INSENSITIVE_COMPARATOR;

public final class FilesProvider extends ContentProvider
    implements SharedPreferences.OnSharedPreferenceChangeListener {

  private static final String PREF_BOOKMARKS = "bookmarks";

  private static final String[] DEFAULT_COLUMNS = {
      FileInfo.COLUMN_ID,
      FileInfo.COLUMN_NAME,
      FileInfo.COLUMN_SIZE,
      FileInfo.COLUMN_READABLE,
      FileInfo.COLUMN_WRITABLE,
      FileInfo.COLUMN_MEDIA_TYPE,
      FileInfo.COLUMN_LAST_MODIFIED,
  };

  private static final UriMatcher matcher = newMatcher();

  @Override public boolean onCreate() {
    getPreference().registerOnSharedPreferenceChangeListener(this);
    return true;
  }

  @Override public String getType(Uri uri) {
    switch (matcher.match(uri)) {
      case MATCH_FILES_ID:
        String fileId = getFileId(uri);
        File file = new File(toURI(fileId));
        if (file.isDirectory()) {
          return MEDIA_TYPE_DIR;
        }
        try {
          return TikaHolder.TIKA.detect(file);
        } catch (IOException e) {
          return null;
        }
    }
    throw new UnsupportedOperationException("Unsupported Uri: " + uri);
  }

  @Override public ParcelFileDescriptor openFile(Uri uri, String mode)
      throws FileNotFoundException {
    switch (matcher.match(uri)) {
      case MATCH_FILES_ID:
        File file = new File(toURI(getFileId(uri)));
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

    if (projection == null) projection = DEFAULT_COLUMNS;

    switch (matcher.match(uri)) {
      case MATCH_SUGGESTION:
        return querySuggestion(uri, projection);
      case MATCH_BOOKMARKS:
        return queryBookmarks(uri, projection);
      case MATCH_BOOKMARKS_ID:
        return queryBookmark(uri, projection);
      case MATCH_FILES_ID:
        return queryFile(uri, projection);
      case MATCH_FILES_ID_CHILDREN:
        return queryFiles(uri, projection, sortOrder);
      case MATCH_HIERARCHY:
        return queryHierarchy(uri, projection);
      default:
        throw new UnsupportedOperationException("Unsupported Uri: " + uri);
    }
  }

  private Cursor queryHierarchy(Uri uri, String[] projection) {
    File file = normalize(new File(toURI(getHierarchyFileId(uri))));
    List<File> files = newArrayList();
    while (file != null) {
      files.add(file);
      file = file.getParentFile();
    }
    reverse(files);
    return newFileCursor(uri, projection, files.toArray(new File[files.size()]));
  }

  private Cursor queryFile(Uri uri, String[] projection) {
    File file = new File(toURI(getFileId(uri)));
    File[] files = file.exists() ? new File[]{file} : new File[0];
    return new FileCursor(files, projection);
  }

  private Cursor querySuggestion(Uri uri, String[] projection) {
    File parent = new File(toURI(getSuggestionParentId(uri)));
    String name = getSuggestionBasename(uri);
    File file = new File(parent, name);
    for (int i = 2; file.exists(); i++) {
      file = new File(parent, name + " " + i);
    }
    return newFileCursor(uri, projection, new File[]{file});
  }

  private Cursor queryBookmark(Uri uri, String[] projection) {
    File[] bookmark = getBookmark(getPreference(), getBookmarkFileId(uri));
    return newFileCursor(uri, projection, bookmark);
  }

  private Cursor queryBookmarks(Uri uri, String[] projection) {
    File[] bookmarks = getBookmarks(getPreference());
    return newFileCursor(uri, projection, bookmarks);
  }

  private Cursor queryFiles(Uri uri, String[] projection, String sortOrder) {
    boolean showHidden = VALUE_SHOW_HIDDEN_YES
        .equals(uri.getQueryParameter(PARAM_SHOW_HIDDEN));

    File dir = new File(toURI(getFileId(uri)));
    File[] files = listFiles(dir, showHidden);

    if (files == null) files = new File[0];
    switch (sortOrder) {
      case SORT_BY_LAST_MODIFIED:
        sort(files, LASTMODIFIED_REVERSE);
        break;
      case SORT_BY_NAME:
      default:
        sort(files, NAME_INSENSITIVE_COMPARATOR);
        break;
    }
    return newMonitoringCursor(uri, dir, projection, files);
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
    Set<File> files = toFilesSet(extras.getStringArray(EXTRA_FILE_IDS));
    File destination = new File(toURI(extras.getString(EXTRA_DESTINATION_ID)));
    MoveService.start(getContext(), files, destination);
    return Bundle.EMPTY;
  }

  private Bundle callCopy(Bundle extras) {
    Set<File> files = toFilesSet(extras.getStringArray(EXTRA_FILE_IDS));
    File destination = new File(toURI(extras.getString(EXTRA_DESTINATION_ID)));
    CopyService.start(getContext(), files, destination);
    return Bundle.EMPTY;
  }

  private Bundle callRename(Bundle extras) {
    File from = new File(toURI(extras.getString(EXTRA_FILE_ID)));
    File to = new File(from.getParent(), extras.getString(EXTRA_NEW_NAME));
    Bundle out = new Bundle(1);
    out.putBoolean(EXTRA_RESULT, from.renameTo(to));
    return out;
  }

  private Bundle callDelete(Bundle extras) {
    Set<File> files = toFilesSet(extras.getStringArray(EXTRA_FILE_IDS));
    DeleteService.delete(getContext(), files);
    return Bundle.EMPTY;
  }

  private Set<File> toFilesSet(String[] fileIds) {
    Set<File> files = newHashSetWithExpectedSize(fileIds.length);
    for (String fileId : fileIds) {
      files.add(new File(toURI(fileId)));
    }
    return files;
  }

  @Override public Uri insert(Uri uri, ContentValues values) {
    switch (matcher.match(uri)) {
      case MATCH_BOOKMARKS_ID:
        return insertBookmark(uri);
      case MATCH_FILES_ID:
        return insertDirectory(uri);
    }
    throw new UnsupportedOperationException("Unsupported Uri: " + uri);
  }

  private Uri insertBookmark(Uri uri) {
    Bookmarks.add(getPreference(), getBookmarkFileId(uri));
    return uri;
  }

  private Uri insertDirectory(Uri uri) {
    File file = new File(toURI(getFileId(uri)));
    return (file.mkdirs() || file.isDirectory()) ? uri : null;
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    switch (matcher.match(uri)) {
      case MATCH_BOOKMARKS_ID:
        return deleteBookmark(uri);
    }
    throw new UnsupportedOperationException("Unsupported Uri: " + uri);
  }

  private int deleteBookmark(Uri uri) {
    Bookmarks.remove(getPreference(), getBookmarkFileId(uri));
    return 1; // TODO check bookmarks contain Uri
  }

  @Override public int update(
      Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    throw new UnsupportedOperationException("Update not supported");
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
    if (PREF_BOOKMARKS.equals(key))
      getContentResolver().notifyChange(buildBookmarksUri(), null);
  }

  private Cursor newFileCursor(Uri uri, String[] projection, File[] files) {
    Cursor c = new FileCursor(files, projection);
    c.setNotificationUri(getContentResolver(), uri);
    return c;
  }

  private Cursor newMonitoringCursor(
      Uri uri, File dir, String[] projection, File[] files) {
    Cursor cursor = newFileCursor(uri, projection, files);
    return MonitoringCursor.create(getContentResolver(), uri, dir, cursor);
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
