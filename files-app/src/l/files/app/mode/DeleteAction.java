package l.files.app.mode;

import static android.content.DialogInterface.OnClickListener;
import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
import static android.view.MenuItem.SHOW_AS_ACTION_WITH_TEXT;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.common.widget.ListViews.getCheckedItems;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import com.squareup.otto.Bus;
import java.io.File;
import java.util.List;
import l.files.R;
import l.files.common.widget.SingleAction;
import l.files.event.DeleteRequest;

final class DeleteAction extends SingleAction {

  private final AbsListView list;
  private final Bus bus;

  public DeleteAction(AbsListView list, Bus bus) {
    this.bus = checkNotNull(bus, "bus");
    this.list = checkNotNull(list, "list");
  }

  @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    menu.add(NONE, id(), NONE, R.string.delete)
        .setIcon(R.drawable.ic_menu_delete)
        .setShowAsAction(SHOW_AS_ACTION_IF_ROOM | SHOW_AS_ACTION_WITH_TEXT);
    return true;
  }

  @Override protected int id() {
    return R.id.delete;
  }

  @Override protected void handleActionItemClicked(final ActionMode mode, MenuItem item) {
    final List<File> files = getCheckedItems(list, File.class);
    new AlertDialog.Builder(list.getContext())
        .setMessage(getConfirmMessage(files.size()))
        .setNegativeButton(android.R.string.cancel, null)
        .setPositiveButton(R.string.delete, new OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            requestDelete(files);
            mode.finish();
          }
        })
        .show();
  }

  private void requestDelete(List<File> files) {
    for (File file : files) bus.post(new DeleteRequest(file));
  }

  private String getConfirmMessage(int size) {
    return list.getResources().getQuantityString(
        R.plurals.confirm_delete_question, size, size);
  }
}
