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

import l.files.R;
import l.files.common.app.OptionsMenuAction;

import static android.support.v4.app.LoaderManager.LoaderCallbacks;
import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.System.identityHashCode;
import static l.files.provider.FilesContract.bookmark;
import static l.files.provider.FilesContract.buildBookmarkUri;
import static l.files.provider.FilesContract.unbookmark;

public final class BookmarkMenu extends OptionsMenuAction
    implements LoaderCallbacks<Cursor> {

  private final Context context;
  private final LoaderManager loaders;
  private final ContentResolver resolver;
  private final String fileId;

  private Menu menu;
  private boolean bookmarked;

  public BookmarkMenu(Context context, LoaderManager loaders,
                      ContentResolver resolver, String fileId) {
    this.context = checkNotNull(context, "context");
    this.loaders = checkNotNull(loaders, "loaders");
    this.resolver = checkNotNull(resolver, "resolver");
    this.fileId = checkNotNull(fileId, "fileId");
  }

  @Override public void onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    loaders.initLoader(identityHashCode(this), null, this);
    menu.add(NONE, id(), NONE, R.string.bookmark)
        .setCheckable(true)
        .setShowAsAction(SHOW_AS_ACTION_NEVER);
  }

  @Override public void onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    this.menu = menu;
    MenuItem item = menu.findItem(id());
    if (item != null) {
      item.setChecked(bookmarked);
    }
  }

  @Override protected int id() {
    return R.id.bookmark;
  }

  @Override protected void onItemSelected(MenuItem item) {
    final boolean checked = item.isChecked();
    AsyncTask.execute(new Runnable() {
      @Override public void run() {
        if (checked) unbookmark(resolver, fileId);
        else bookmark(resolver, fileId);
      }
    });
  }

  @Override public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
    Uri uri = buildBookmarkUri(fileId);
    return new CursorLoader(context, uri, null, null, null, null);
  }

  @Override public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    bookmarked = cursor.getCount() > 0;
    if (menu != null) {
      onPrepareOptionsMenu(menu);
    }
  }

  @Override public void onLoaderReset(Loader<Cursor> loader) {}
}
