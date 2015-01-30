package l.files.ui.menu;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.AsyncTask;
import android.view.Menu;
import android.view.MenuItem;

import l.files.common.app.OptionsMenuAction;
import l.files.fs.Path;
import l.files.operations.OperationService;
import l.files.ui.Clipboards;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.ui.Clipboards.clear;
import static l.files.ui.Clipboards.getPaths;
import static l.files.ui.Clipboards.isCopy;
import static l.files.ui.Clipboards.isCut;

public final class PasteMenu extends OptionsMenuAction {

  private final Path path;
  private final ClipboardManager manager;
  private final Context context;

  public PasteMenu(Context context, ClipboardManager manager, Path path) {
    super(android.R.id.paste);
    this.context = checkNotNull(context);
    this.manager = checkNotNull(manager);
    this.path = checkNotNull(path);
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
          OperationService.copy(context, getPaths(manager), path);
        } else if (isCut(manager)) {
          OperationService.move(context, getPaths(manager), path);
          clear(manager);
        }
      }
    });
  }
}
