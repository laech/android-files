package l.files.meta;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import static android.database.DatabaseUtils.appendSelectionArgs;
import static android.database.DatabaseUtils.concatenateWhere;
import static l.files.meta.DatabaseHelper.TABLE_META;
import static l.files.meta.MetaContract.MATCH_META_PARENT;
import static l.files.meta.MetaContract.Meta;
import static l.files.meta.MetaContract.MetaColumns.DIRECTORY_URI;
import static l.files.meta.MetaContract.getParent;
import static l.files.meta.MetaContract.newMatcher;

public final class MetaProvider extends ContentProvider {

  private static final UriMatcher MATCHER = newMatcher();

  private DatabaseHelper helper;

  @Override public boolean onCreate() {
    helper = new DatabaseHelper(getContext());
    return true;
  }

  @Override public Cursor query(
      Uri uri,
      String[] projection,
      String selection,
      String[] selectionArgs,
      String sortOrder) {

    switch (MATCHER.match(uri)) {
      case MATCH_META_PARENT:
        return queryMeta(uri, projection, selection, selectionArgs, sortOrder);
      default:
        throw new UnsupportedOperationException("Unsupported uri: " + uri);
    }
  }

  private Cursor queryMeta(
      Uri uri,
      String[] projection,
      String selection,
      String[] selectionArgs,
      String sortOrder) {

    String parent = getParent(uri);
    selection = concatenateWhere(selection, DIRECTORY_URI + "=?");
    selectionArgs = appendSelectionArgs(selectionArgs, new String[]{parent});
    SQLiteDatabase db = helper.getReadableDatabase();

    return db.query(
        TABLE_META,
        projection,
        selection,
        selectionArgs,
        null,
        null,
        sortOrder);
  }

  @Override public String getType(Uri uri) {
    throw new UnsupportedOperationException("Unsupported uri: " + uri);
  }

  @Override public Uri insert(Uri uri, ContentValues values) {
    if (Meta.CONTENT_URI.equals(uri)) {
      SQLiteDatabase db = helper.getWritableDatabase();
      db.insertOrThrow(TABLE_META, null, values);
      return null;
    }
    throw new UnsupportedOperationException("Unsupported uri: " + uri);
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    throw new UnsupportedOperationException("Unsupported uri: " + uri);
  }

  @Override
  public int update(
      Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    throw new UnsupportedOperationException("Unsupported uri: " + uri);
  }
}
