package l.files.app.mode;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;

import java.util.List;

import l.files.R;
import l.files.common.widget.SingleAction;
import l.files.provider.FilesContract;

import static android.content.DialogInterface.OnClickListener;
import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
import static android.view.MenuItem.SHOW_AS_ACTION_WITH_TEXT;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static l.files.common.widget.ListViews.getCheckedItemPositions;
import static l.files.provider.FilesContract.FileInfo.COLUMN_ID;

final class DeleteAction extends SingleAction {

  private final AbsListView list;
  private final ContentResolver resolver;

  public DeleteAction(AbsListView list, ContentResolver resolver) {
    this.list = checkNotNull(list, "list");
    this.resolver = checkNotNull(resolver, "resolver");
  }

  @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    menu.add(NONE, id(), NONE, R.string.delete)
        .setIcon(R.drawable.ic_action_discard)
        .setShowAsAction(SHOW_AS_ACTION_IF_ROOM | SHOW_AS_ACTION_WITH_TEXT);
    return true;
  }

  @Override protected int id() {
    return R.id.delete;
  }

  @Override
  protected void handleActionItemClicked(final ActionMode mode, MenuItem item) {
    final List<String> fileIds = getCheckedFileIds();
    new AlertDialog.Builder(list.getContext())
        .setMessage(getConfirmMessage(fileIds.size()))
        .setNegativeButton(android.R.string.cancel, null)
        .setPositiveButton(R.string.delete, new OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            requestDelete(fileIds);
            mode.finish();
          }
        })
        .show();
  }

  private List<String> getCheckedFileIds() {
    List<Integer> positions = getCheckedItemPositions(list);
    List<String> fileIds = newArrayListWithCapacity(positions.size());
    for (int position : positions) {
      Cursor cursor = (Cursor) list.getItemAtPosition(position);
      String id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID));
      fileIds.add(id);
    }
    return fileIds;
  }

  private void requestDelete(final List<String> fileIds) {
    AsyncTask.execute(new Runnable() {
      @Override public void run() {
        FilesContract.delete(resolver, fileIds);
      }
    });
  }

  private String getConfirmMessage(int size) {
    return list.getResources().getQuantityString(
        R.plurals.confirm_delete_question, size, size);
  }
}
