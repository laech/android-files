package l.files.app.menu;

import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.os.AsyncTask;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;

import l.files.app.Clipboards;
import l.files.common.app.OptionsMenuAdapter;
import l.files.provider.FilesContract;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.app.Clipboards.getFileIds;
import static l.files.app.Clipboards.isCopy;

final class PasteMenu
    extends OptionsMenuAdapter implements OnMenuItemClickListener {

  private final String directoryId;
  private final ClipboardManager manager;
  private final ContentResolver resolver;

  PasteMenu(ClipboardManager manager, String directoryId, ContentResolver resolver) {
    this.resolver = checkNotNull(resolver, "resolver");
    this.manager = checkNotNull(manager, "manager");
    this.directoryId = checkNotNull(directoryId, "directoryId");
  }

  @Override public void onCreate(Menu menu) {
    super.onCreate(menu);
    menu.add(NONE, android.R.id.paste, NONE, android.R.string.paste)
        .setOnMenuItemClickListener(this)
        .setShowAsAction(SHOW_AS_ACTION_NEVER);
  }

  @Override public void onPrepare(Menu menu) {
    super.onPrepare(menu);
    MenuItem item = menu.findItem(android.R.id.paste);
    if (null != item) {
      item.setEnabled(Clipboards.hasClip(manager)); // TODO
    }
  }

  @Override public boolean onMenuItemClick(MenuItem item) {
    AsyncTask.execute(new Runnable() {
      @Override public void run() {
        if (isCopy(manager)) {
          FilesContract.copy(resolver, getFileIds(manager), directoryId);
        }
      }
    });
    return true;
  }
}
