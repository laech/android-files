package l.files.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;

import java.io.File;

import static android.content.UriMatcher.NO_MATCH;

public final class FilesProvider extends ContentProvider {

  public static final int MATCH_CHILDREN = 2;

  private UriMatcher matcher;

  @Override public void attachInfo(Context context, ProviderInfo info) {
    String authority = info.authority;

    matcher = new UriMatcher(NO_MATCH);
    matcher.addURI(authority, "files/*/children", MATCH_CHILDREN);

    super.attachInfo(context, info);
  }

  @Override public boolean onCreate() {
    return true;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection,
                      String[] selectionArgs, String sortOrder) {
    switch (matcher.match(uri)) {
      case MATCH_CHILDREN: {
        @SuppressWarnings("ConstantConditions")
        String path = uri.getPathSegments().get(1);
        File[] children = new File(path).listFiles();
        if (children == null) {
          children = new File[0];
        }
        return new FileCursor(children, projection);
      }
      default:
        throw new UnsupportedOperationException("Unsupported Uri: " + uri);
    }
  }

  @Override public String getType(Uri uri) {
    return null;
  }

  @Override public Uri insert(Uri uri, ContentValues values) {
    return null;
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    return 0;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection,
                    String[] selectionArgs) {
    return 0;
  }
}
