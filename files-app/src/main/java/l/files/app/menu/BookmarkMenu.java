package l.files.app.menu;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuItem;

import l.files.R;
import l.files.analytics.AnalyticsMenu;
import l.files.common.app.OptionsMenu;
import l.files.common.app.OptionsMenuAction;
import l.files.provider.FilesContract;

import static android.support.v4.app.LoaderManager.LoaderCallbacks;
import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.System.identityHashCode;
import static l.files.provider.FilesContract.bookmark;
import static l.files.provider.FilesContract.buildBookmarkUri;
import static l.files.provider.FilesContract.unbookmark;

/**
 * Menu to bookmark/unbookmark a directory with the given ID.
 *
 * @see FilesContract.FileInfo#COLUMN_ID
 */
public final class BookmarkMenu
    extends OptionsMenuAction implements LoaderCallbacks<Cursor> {

  private final FragmentActivity context;
  private final String fileId;

  private Menu menu;
  private boolean bookmarked;

  private BookmarkMenu(FragmentActivity context, String fileId) {
    super(R.id.bookmark);
    this.context = checkNotNull(context, "context");
    this.fileId = checkNotNull(fileId, "fileId");
  }

  public static OptionsMenu create(FragmentActivity activity, String fileId) {
    OptionsMenu menu = new BookmarkMenu(activity, fileId);
    return new AnalyticsMenu(activity, menu, "bookmark");
  }

  @Override public void onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    LoaderManager loaders = context.getSupportLoaderManager();
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
    final ContentResolver resolver = context.getContentResolver();
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
