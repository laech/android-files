package l.files.app.mode;

import static android.content.DialogInterface.OnClickListener;
import static android.view.Menu.NONE;
import static android.view.MenuItem.OnMenuItemClickListener;
import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
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
import l.files.common.widget.MultiChoiceActionAdapter;
import l.files.event.DeleteRequest;

final class DeleteAction
    extends MultiChoiceActionAdapter implements OnMenuItemClickListener {

  private final AbsListView list;
  private final Bus bus;

  private ActionMode mode;

  public DeleteAction(AbsListView list, Bus bus) {
    this.bus = checkNotNull(bus, "bus");
    this.list = checkNotNull(list, "list");
  }

  @Override public void onCreate(ActionMode mode, Menu menu) {
    this.mode = mode;
    menu.add(NONE, R.id.delete, NONE, R.string.delete)
        .setOnMenuItemClickListener(this)
        .setIcon(R.drawable.ic_menu_delete)
        .setShowAsAction(SHOW_AS_ACTION_IF_ROOM);
  }

  @Override public boolean onMenuItemClick(MenuItem item) {
    final List<File> files = getCheckedItems(list, File.class);
    new AlertDialog.Builder(list.getContext())
        .setMessage(getConfirmMessage(files.size()))
        .setNegativeButton(android.R.string.cancel, null)
        .setPositiveButton(R.string.delete, new OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            requestDelete(files);
            if (mode != null) mode.finish();
          }
        })
        .show();
    return true;
  }

  private void requestDelete(List<File> files) {
    for (File file : files) bus.post(new DeleteRequest(file));
  }

  private String getConfirmMessage(int size) {
    return list.getResources().getQuantityString(
        R.plurals.confirm_delete_question, size, size);
  }
}