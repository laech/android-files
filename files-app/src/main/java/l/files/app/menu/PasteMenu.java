package l.files.app.menu;

import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.os.AsyncTask;
import android.view.Menu;
import android.view.MenuItem;

import l.files.app.Clipboards;
import l.files.common.app.OptionsMenuAction;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.app.Clipboards.clear;
import static l.files.app.Clipboards.getFileIds;
import static l.files.app.Clipboards.isCopy;
import static l.files.app.Clipboards.isCut;
import static l.files.provider.FilesContract.copy;
import static l.files.provider.FilesContract.cut;

public final class PasteMenu extends OptionsMenuAction {

  private final String directoryId;
  private final ClipboardManager manager;
  private final ContentResolver resolver;

  public PasteMenu(
      ClipboardManager manager, String directoryId, ContentResolver resolver) {
    super(android.R.id.paste);
    this.resolver = checkNotNull(resolver, "resolver");
    this.manager = checkNotNull(manager, "manager");
    this.directoryId = checkNotNull(directoryId, "directoryId");
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
          copy(resolver, getFileIds(manager), directoryId);
        } else if (isCut(manager)) {
          cut(resolver, getFileIds(manager), directoryId);
          clear(manager);
        }
      }
    });
  }
}
