package l.files.app.menu;

import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

import l.files.analytics.AnalyticsMenu;
import l.files.app.Clipboards;
import l.files.common.app.OptionsMenu;
import l.files.common.app.OptionsMenuAction;
import l.files.provider.FilesContract;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.app.Clipboards.clear;
import static l.files.app.Clipboards.getFileIds;
import static l.files.app.Clipboards.isCopy;
import static l.files.app.Clipboards.isCut;
import static l.files.common.app.SystemServices.getClipboardManager;
import static l.files.provider.FilesContract.copy;
import static l.files.provider.FilesContract.cut;

/**
 * Menu to paste files to a directory identified by the given ID.
 *
 * @see FilesContract.FileInfo#COLUMN_ID
 */
public final class PasteMenu extends OptionsMenuAction {

  private final String dirId;
  private final ClipboardManager manager;
  private final ContentResolver resolver;

  private PasteMenu(
      ClipboardManager manager, ContentResolver resolver, String dirId) {
    super(android.R.id.paste);
    this.resolver = checkNotNull(resolver, "resolver");
    this.manager = checkNotNull(manager, "manager");
    this.dirId = checkNotNull(dirId, "dirId");
  }

  public static OptionsMenu create(FragmentActivity activity, String dirId) {
    ClipboardManager manager = getClipboardManager(activity);
    ContentResolver resolver = activity.getContentResolver();
    PasteMenu menu = new PasteMenu(manager, resolver, dirId);
    return new AnalyticsMenu(activity, menu, "paste");
  }

  @Override public void onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    menu.add(NONE, id(), NONE, android.R.string.paste)
        .setShowAsAction(SHOW_AS_ACTION_NEVER);
  }

  @Override public void onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    MenuItem item = menu.findItem(id());
    if (item != null) {
      item.setEnabled(Clipboards.hasClip(manager)); // TODO
    }
  }

  @Override protected void onItemSelected(MenuItem item) {
    AsyncTask.execute(new Runnable() {
      @Override public void run() {
        if (isCopy(manager)) {
          copy(resolver, getFileIds(manager), dirId);
        } else if (isCut(manager)) {
          cut(resolver, getFileIds(manager), dirId);
          clear(manager);
        }
      }
    });
  }
}
