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
import l.files.analytics.AnalyticsMenu;
import l.files.common.app.OptionsMenu;
import l.files.common.app.OptionsMenuAction;
import l.files.provider.FilesContract;

import static android.app.LoaderManager.LoaderCallbacks;
import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.System.identityHashCode;
import static l.files.provider.bookmarks.BookmarksContract.bookmark;
import static l.files.provider.bookmarks.BookmarksContract.buildBookmarkUri;
import static l.files.provider.bookmarks.BookmarksContract.unbookmark;

/**
 * Menu to bookmark/unbookmark a directory at the given {@link
 * FilesContract.Files#LOCATION}.
 */
public final class BookmarkMenu
    extends OptionsMenuAction implements LoaderCallbacks<Cursor> {

  private final Activity context;
  private final String directoryLocation;

  private Menu menu;
  private boolean bookmarked;

  private BookmarkMenu(Activity context, String directoryLocation) {
    super(R.id.bookmark);
    this.context = checkNotNull(context, "context");
    this.directoryLocation = checkNotNull(directoryLocation, "directoryLocation");
  }

  public static OptionsMenu create(Activity activity, String directoryLocation) {
    OptionsMenu menu = new BookmarkMenu(activity, directoryLocation);
    return new AnalyticsMenu(activity, menu, "bookmark");
  }

  @Override public void onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    LoaderManager loaders = context.getLoaderManager();
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

  @Override protected void onItemSelected(MenuItem item) {
    final boolean checked = item.isChecked();
    AsyncTask.execute(new Runnable() {
      @Override public void run() {
        if (checked) unbookmark(context, directoryLocation);
        else bookmark(context, directoryLocation);
      }
    });
  }

  @Override public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
    Uri uri = buildBookmarkUri(context, directoryLocation);
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
