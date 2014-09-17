package l.files.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
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
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import l.files.io.file.FileInfo;
import l.files.io.file.WatchService;
import l.files.logging.Logger;
import l.files.operations.OperationService;

import static android.os.ParcelFileDescriptor.MODE_READ_ONLY;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.collect.Sets.newHashSetWithExpectedSize;
import static java.util.Arrays.asList;
import static java.util.Collections.reverse;
import static l.files.io.file.Files.normalize;
import static l.files.io.file.Files.rename;
import static l.files.provider.BuildConfig.DEBUG;
import static l.files.provider.FilesContract.EXTRA_DESTINATION_ID;
import static l.files.provider.FilesContract.EXTRA_ERROR;
import static l.files.provider.FilesContract.EXTRA_FILE_ID;
import static l.files.provider.FilesContract.EXTRA_FILE_IDS;
import static l.files.provider.FilesContract.EXTRA_NEW_NAME;
import static l.files.provider.FilesContract.FileInfo.MIME_DIR;
import static l.files.provider.FilesContract.MATCH_FILES_ID;
import static l.files.provider.FilesContract.MATCH_FILES_ID_CHILDREN;
import static l.files.provider.FilesContract.MATCH_HIERARCHY;
import static l.files.provider.FilesContract.MATCH_SELECTION;
import static l.files.provider.FilesContract.METHOD_COPY;
import static l.files.provider.FilesContract.METHOD_CUT;
import static l.files.provider.FilesContract.METHOD_DELETE;
import static l.files.provider.FilesContract.METHOD_RENAME;
import static l.files.provider.FilesContract.METHOD_SUGGESTION;
import static l.files.provider.FilesContract.PARAM_SELECTION;
import static l.files.provider.FilesContract.getFileId;
import static l.files.provider.FilesContract.getHierarchyFileId;
import static l.files.provider.FilesContract.newMatcher;

public final class FilesProvider extends ContentProvider {

  private static final Logger logger = Logger.get(FilesProvider.class);

  private static final String[] DEFAULT_COLUMNS = {
      FilesContract.FileInfo.ID,
      FilesContract.FileInfo.NAME,
      FilesContract.FileInfo.SIZE,
      FilesContract.FileInfo.READABLE,
      FilesContract.FileInfo.WRITABLE,
      FilesContract.FileInfo.MIME,
      FilesContract.FileInfo.MODIFIED,
      FilesContract.FileInfo.HIDDEN,
      FilesContract.FileInfo.TYPE,
  };

  private UriMatcher matcher;
  private FilesCache helper;

  @Override public boolean onCreate() {
    matcher = newMatcher(getContext());
    helper = new FilesCache(getContext(), WatchService.get());
    return true;
  }

  @Override public String getType(Uri uri) {
    switch (matcher.match(uri)) {
      case MATCH_FILES_ID:
        String location = getFileId(uri);
        File file = new File(URI.create(location));
        // TODO don't do anything special for directories, detect and return "application/x-directory"
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

  @Override
  public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
    switch (matcher.match(uri)) {
      case MATCH_FILES_ID:
        File file = new File(URI.create(getFileId(uri)));
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
      case MATCH_FILES_ID:
        return queryFile(uri, projection, sortOrder);
      case MATCH_FILES_ID_CHILDREN:
        return queryFiles(uri, projection, sortOrder);
      case MATCH_HIERARCHY:
        return queryHierarchy(uri, projection, sortOrder);
      case MATCH_SELECTION:
        return querySelection(uri, projection, sortOrder);
      default:
        throw new UnsupportedOperationException("Unsupported Uri: " + uri);
    }
  }

  private Cursor queryHierarchy(Uri uri, String[] projection, String sortOrder) {
    File file = normalize(new File(URI.create(getHierarchyFileId(uri))));
    List<File> files = newArrayList();
    while (file != null) {
      files.add(file);
      file = file.getParentFile();
    }
    reverse(files);
    File[] fileArray = files.toArray(new File[files.size()]);
    return newFileCursor(uri, projection, sortOrder, fileArray);
  }

  private Cursor queryFile(Uri uri, String[] projection, String sortOrder) {
    File file = new File(URI.create(getFileId(uri)));
    return newFileCursor(uri, projection, sortOrder, file);
  }

  private Cursor queryFiles(Uri uri, String[] projection, String sortOrder) {
    FileInfo[] data = helper.get(uri, null);
    return newFileCursor(uri, projection, sortOrder, data);
  }

  private Cursor querySelection(Uri uri, String[] projection, String sortOrder) {
    List<String> selection = uri.getQueryParameters(PARAM_SELECTION);
    File[] files = new File[selection.size()];
    for (int i = 0; i < files.length; i++) {
      files[i] = new File(URI.create(selection.get(i)));
    }

    return newFileCursor(uri, projection, sortOrder, files);
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
      case METHOD_SUGGESTION:
        return callSuggestion(extras);
    }
    return super.call(method, arg, extras);
  }

  private Bundle callCut(Bundle extras) {
    String[] srcPaths = toFilePaths(extras.getStringArray(EXTRA_FILE_IDS));
    String dstPath = new File(URI.create(extras.getString(EXTRA_DESTINATION_ID))).getPath();
    OperationService.move(getContext(), asList(srcPaths), dstPath);
    return Bundle.EMPTY;
  }

  private Bundle callCopy(Bundle extras) {
    String[] srcPaths = toFilePaths(extras.getStringArray(EXTRA_FILE_IDS));
    String dstPath = new File(URI.create(extras.getString(EXTRA_DESTINATION_ID))).getPath();
    OperationService.copy(getContext(), asList(srcPaths), dstPath);
    return Bundle.EMPTY;
  }

  private Bundle callRename(Bundle extras) {
    File from = new File(URI.create(extras.getString(EXTRA_FILE_ID)));
    File to = new File(from.getParentFile(), extras.getString(EXTRA_NEW_NAME));
    try {
      rename(from.getPath(), to.getPath());
      return Bundle.EMPTY;
    } catch (IOException e) {
      Bundle result = new Bundle(1);
      result.putSerializable(EXTRA_ERROR, e);
      return result;
    }
  }

  private Bundle callDelete(Bundle extras) {
    String[] paths = toFilePaths(extras.getStringArray(EXTRA_FILE_IDS));
    OperationService.delete(getContext(), paths);
    return Bundle.EMPTY;
  }

  private Bundle callSuggestion(Bundle extras) {
    File parent = new File(URI.create(extras.getString(EXTRA_FILE_ID)));
    String name = extras.getString(EXTRA_NEW_NAME);
    File file = new File(parent, name);
    for (int i = 2; file.exists(); i++) {
      file = new File(parent, name + " " + i);
    }
    Bundle result = new Bundle(1);
    result.putString(EXTRA_NEW_NAME, file.getName());
    return result;
  }

  private String[] toFilePaths(String[] fileLocations) {
    Set<String> paths = newHashSetWithExpectedSize(fileLocations.length);
    for (String location : fileLocations) {
      paths.add(toFilePath(location));
    }
    return paths.toArray(new String[paths.size()]);
  }

  private String toFilePath(String location) {
    return new File(URI.create(location)).getPath();
  }

  @Override public Uri insert(Uri uri, ContentValues values) {
    switch (matcher.match(uri)) {
      case MATCH_FILES_ID:
        return insertDirectory(uri);
    }
    throw new UnsupportedOperationException("Unsupported Uri: " + uri);
  }

  private Uri insertDirectory(Uri uri) {
    File file = new File(URI.create(getFileId(uri)));
    return (file.mkdirs() || file.isDirectory()) ? uri : null;
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    throw new UnsupportedOperationException("Unsupported Uri: " + uri);
  }

  @Override public int update(
      Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    throw new UnsupportedOperationException("Update not supported");
  }

  private void sort(FileInfo[] data, String sortOrder) {
    if (sortOrder != null) {
      Arrays.sort(data, SortBy.valueOf(sortOrder));
    }
  }

  private Cursor newFileCursor(Uri uri, String[] projection, String sortOrder, File... files) {
    List<FileInfo> stats = newArrayListWithCapacity(files.length);
    for (File file : files) {
      try {
        stats.add(FileInfo.read(file.getPath()));
      } catch (IOException e) {
        logger.warn(e, "Failed to read info %s", file);
      }
    }
    return newFileCursor(uri, projection, sortOrder, stats.toArray(new FileInfo[stats.size()]));
  }

  private Cursor newFileCursor(Uri uri, String[] projection, String sortOrder, FileInfo... files) {
    sort(files, sortOrder);
    Cursor c = new FileCursor(files, projection);
    c.setNotificationUri(getContentResolver(), uri);
    return c;
  }

  private ContentResolver getContentResolver() {
    return getContext().getContentResolver();
  }

  private static class TikaHolder {
    static final Tika TIKA = new Tika();
  }
}
