package l.files.provider.bookmarks;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static l.files.provider.FilesContract.getSelectionUri;
import static l.files.provider.bookmarks.Bookmarks.bookmarks;
import static l.files.provider.bookmarks.Bookmarks.isBookmarksKey;
import static l.files.provider.bookmarks.BookmarksContract.MATCH_BOOKMARKS;
import static l.files.provider.bookmarks.BookmarksContract.MATCH_BOOKMARKS_LOCATION;
import static l.files.provider.bookmarks.BookmarksContract.buildBookmarksUri;
import static l.files.provider.bookmarks.BookmarksContract.getBookmarkId;
import static l.files.provider.bookmarks.BookmarksContract.newMatcher;

public final class BookmarksProvider extends ContentProvider
    implements SharedPreferences.OnSharedPreferenceChangeListener {

  private UriMatcher matcher;

  @Override public boolean onCreate() {
    matcher = newMatcher(getContext());
    preference().registerOnSharedPreferenceChangeListener(this);
    return true;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    switch (matcher.match(uri)) {
      case MATCH_BOOKMARKS:
        return queryBookmarks(uri, projection, sortOrder, bookmarks(preference()));
      case MATCH_BOOKMARKS_LOCATION:
        return queryBookmarks(uri, projection, sortOrder, getBookmarkId(uri));
    }
    throw new UnsupportedOperationException("Unsupported Uri: " + uri);
  }

  private Cursor queryBookmarks(Uri uri, String[] projection, String sortOrder, String... locations) {
    Context context = getContext();
    ContentResolver resolver = context.getContentResolver();
    Uri selectionUri = getSelectionUri(context, locations);
    Cursor cursor = resolver.query(selectionUri, projection, null, null, sortOrder);
    cursor.setNotificationUri(resolver, uri);
    return cursor;
  }

  @Override public String getType(Uri uri) {
    throw new UnsupportedOperationException("Unsupported Uri: " + uri);
  }

  @Override public Uri insert(Uri uri, ContentValues values) {
    switch (matcher.match(uri)) {
      case MATCH_BOOKMARKS_LOCATION:
        return insertBookmark(uri);
    }
    throw new UnsupportedOperationException("Unsupported Uri: " + uri);
  }

  private Uri insertBookmark(Uri uri) {
    Bookmarks.add(preference(), getBookmarkId(uri));
    return uri;
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
    Bookmarks.remove(preference(), getBookmarkId(uri));
    return 1;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    return 0;
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
    if (isBookmarksKey(key)) {
      getContext().getContentResolver().notifyChange(buildBookmarksUri(getContext()), null);
    }
  }

  private SharedPreferences preference() {
    return getDefaultSharedPreferences(getContext());
  }
}
