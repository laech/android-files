package l.files.ui.menu;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.AsyncTask;
import android.view.Menu;
import android.view.MenuItem;

import l.files.ui.analytics.AnalyticsMenu;
import l.files.ui.Clipboards;
import l.files.common.app.OptionsMenu;
import l.files.common.app.OptionsMenuAction;
import l.files.provider.FilesContract;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.ui.Clipboards.clear;
import static l.files.ui.Clipboards.getFileIds;
import static l.files.ui.Clipboards.isCopy;
import static l.files.ui.Clipboards.isCut;
import static l.files.common.app.SystemServices.getClipboardManager;
import static l.files.provider.FilesContract.copy;
import static l.files.provider.FilesContract.move;

/**
 * Menu to paste files to a directory identified by the given {@link
 * FilesContract.Files#ID}.
 */
public final class PasteMenu extends OptionsMenuAction {

  private final String dirId;
  private final ClipboardManager manager;
  private final Context context;

  private PasteMenu(
      Context context, ClipboardManager manager, String dirId) {
    super(android.R.id.paste);
    this.context = checkNotNull(context, "context");
    this.manager = checkNotNull(manager, "manager");
    this.dirId = checkNotNull(dirId, "dirId");
  }

  public static OptionsMenu create(Activity activity, String dirId) {
    ClipboardManager manager = getClipboardManager(activity);
    PasteMenu menu = new PasteMenu(activity, manager, dirId);
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
      // TODO check existence of the files in the clipboard?
      item.setEnabled(Clipboards.hasClip(manager));
    }
  }

  @Override protected void onItemSelected(MenuItem item) {
    AsyncTask.execute(new Runnable() {
      @Override public void run() {
        if (isCopy(manager)) {
          copy(context, getFileIds(manager), dirId);
        } else if (isCut(manager)) {
          move(context, getFileIds(manager), dirId);
          clear(manager);
        }
      }
    });
  }
}