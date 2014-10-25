package l.files.app.menu;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import l.files.R;
import l.files.ui.analytics.AnalyticsMenu;
import l.files.common.app.OptionsMenu;
import l.files.common.app.OptionsMenuAction;

import static android.app.LoaderManager.LoaderCallbacks;
import static android.view.Menu.NONE;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.System.identityHashCode;
import static l.files.provider.bookmarks.BookmarksContract.bookmark;
import static l.files.provider.bookmarks.BookmarksContract.getBookmarkUri;
import static l.files.provider.bookmarks.BookmarksContract.unbookmark;

/**
 * Menu to bookmark/unbookmark a directory.
 */
public final class BookmarkMenu
    extends OptionsMenuAction implements LoaderCallbacks<Cursor> {

  private final Activity context;
  private final String dirId;

  private Menu menu;
  private boolean bookmarked;

  private BookmarkMenu(Activity context, String dirId) {
    super(R.id.bookmark);
    this.context = checkNotNull(context, "context");
    this.dirId = checkNotNull(dirId, "dirId");
  }

  public static OptionsMenu create(Activity activity, String dirId) {
    OptionsMenu menu = new BookmarkMenu(activity, dirId);
    return new AnalyticsMenu(activity, menu, "bookmark");
  }

  @Override public void onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    LoaderManager loaders = context.getLoaderManager();
    loaders.initLoader(identityHashCode(this), null, this);
    menu.add(NONE, id(), NONE, R.string.bookmark).setCheckable(true);
  }

  @Override public void onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    this.menu = menu;
    MenuItem item = menu.findItem(id());
    if (item != null) {
      item.setChecked(bookmarked);
    }
  }

  @Override protected void onItemSelected(MenuItem item) {
    final boolean checked = item.isChecked();
    item.setChecked(!checked);
    AsyncTask.execute(new Runnable() {
      @Override public void run() {
        if (checked) unbookmark(context, dirId);
        else bookmark(context, dirId);
      }
    });
  }

  @Override public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
    Uri uri = getBookmarkUri(context, dirId);
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
