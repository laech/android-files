package l.files.ui.mode;

import android.content.ClipboardManager;
import android.content.Context;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.google.common.base.Supplier;

import java.util.Set;

import l.files.common.widget.MultiChoiceModeAction;
import l.files.fs.Path;
import l.files.ui.Clipboards;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.common.content.res.Styles.getDrawable;

public final class CopyAction extends MultiChoiceModeAction {

  private final Context context;
  private final ClipboardManager manager;
  private final Supplier<Set<Path>> supplier;

  public CopyAction(Context context, ClipboardManager manager,
                    Supplier<Set<Path>> supplier) {
    super(android.R.id.copy);
    this.context = checkNotNull(context);
    this.supplier = checkNotNull(supplier);
    this.manager = checkNotNull(manager);
  }

  @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    menu.add(NONE, id(), NONE, android.R.string.copy)
        .setIcon(getDrawable(android.R.attr.actionModeCopyDrawable, context))
        .setShowAsAction(SHOW_AS_ACTION_IF_ROOM);
    return true;
  }

  @Override protected void onItemSelected(ActionMode mode, MenuItem item) {
    Clipboards.setCopy(manager, supplier.get());
    mode.finish();
  }
}
