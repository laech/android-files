package l.files.app.menu;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;

import l.files.R;
import l.files.common.app.OptionsMenuAdapter;

import static android.support.v4.app.LoaderManager.LoaderCallbacks;
import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.System.identityHashCode;
import static l.files.provider.FilesContract.buildBookmarkUri;
import static l.files.provider.FilesContract.deleteBookmark;
import static l.files.provider.FilesContract.insertBookmark;

final class BookmarkMenu extends OptionsMenuAdapter
    implements OnMenuItemClickListener, LoaderCallbacks<Cursor> {

  private boolean bookmarked;
  private final Context context;
  private final LoaderManager loaders;
  private final ContentResolver resolver;
  private final String fileId;

  BookmarkMenu(Context context, LoaderManager loaders,
               ContentResolver resolver, String fileId) {
    this.context = checkNotNull(context, "context");
    this.loaders = checkNotNull(loaders, "loaders");
    this.resolver = checkNotNull(resolver, "resolver");
    this.fileId = checkNotNull(fileId, "fileId");
  }

  @Override public void onCreate(Menu menu) {
    super.onCreate(menu);
    loaders.initLoader(identityHashCode(this), null, this);
    menu.add(NONE, R.id.bookmark, NONE, R.string.bookmark)
        .setOnMenuItemClickListener(this)
        .setCheckable(true)
        .setShowAsAction(SHOW_AS_ACTION_NEVER);
  }

  @Override public void onPrepare(Menu menu) {
    super.onPrepare(menu);
    MenuItem item = menu.findItem(R.id.bookmark);
    if (item != null) item.setChecked(bookmarked);
  }

  @Override public boolean onMenuItemClick(MenuItem item) {
    final boolean checked = item.isChecked();
    AsyncTask.execute(new Runnable() {
      @Override public void run() {
        if (checked) deleteBookmark(resolver, fileId);
        else insertBookmark(resolver, fileId);
      }
    });
    return true;
  }

  @Override public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
    Uri uri = buildBookmarkUri(fileId);
    return new CursorLoader(context, uri, null, null, null, null);
  }

  @Override public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    bookmarked = cursor.getCount() > 0;
  }

  @Override public void onLoaderReset(Loader<Cursor> loader) {}
}
