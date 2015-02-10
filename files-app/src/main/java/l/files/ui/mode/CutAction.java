package l.files.ui.mode;

import android.content.ClipboardManager;
import android.content.Context;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import l.files.common.widget.MultiChoiceModeAction;
import l.files.fs.Path;
import l.files.ui.Clipboards;
import l.files.ui.ListSelection;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.common.content.res.Styles.getDrawable;

public final class CutAction extends MultiChoiceModeAction {

  private final Context context;
  private final ClipboardManager manager;
  private final ListSelection<Path> provider;

  @SuppressWarnings("unchecked")
  public CutAction(Context context, ClipboardManager manager,
                   ListSelection<? extends Path> provider) {
    super(android.R.id.cut);
    this.context = checkNotNull(context);
    this.manager = checkNotNull(manager);
    this.provider = (ListSelection<Path>) checkNotNull(provider);
  }

  @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    menu.add(NONE, id(), NONE, android.R.string.cut)
        .setIcon(getDrawable(android.R.attr.actionModeCutDrawable, context))
        .setShowAsAction(SHOW_AS_ACTION_IF_ROOM);
    return true;
  }

  @Override protected void onItemSelected(ActionMode mode, MenuItem item) {
    Clipboards.setCut(manager, provider.getCheckedItems());
    mode.finish();
  }
}
