package l.files.ui.mode;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import l.files.R;
import l.files.common.widget.MultiChoiceModeAction;
import l.files.fs.Path;
import l.files.operations.OperationService;
import l.files.ui.ListProvider;

import static android.content.DialogInterface.OnClickListener;
import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.base.Preconditions.checkNotNull;

public final class DeleteAction extends MultiChoiceModeAction {

  private final Context context;
  private final ListProvider<Path> supplier;

  @SuppressWarnings("unchecked")
  public DeleteAction(Context context, ListProvider<? extends Path> supplier) {
    super(R.id.delete);
    this.context = checkNotNull(context);
    this.supplier = (ListProvider<Path>) checkNotNull(supplier);
  }

  @Override
  public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    menu.add(NONE, id(), NONE, R.string.delete)
        .setShowAsAction(SHOW_AS_ACTION_NEVER);
    return true;
  }

  @Override
  protected void onItemSelected(final ActionMode mode, MenuItem item) {
    final List<Path> paths = supplier.getCheckedItems();
    new AlertDialog.Builder(context)
        .setMessage(getConfirmMessage(paths.size()))
        .setNegativeButton(android.R.string.cancel, null)
        .setPositiveButton(R.string.delete, new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            requestDelete(paths);
            mode.finish();
          }
        })
        .show();
  }

  private void requestDelete(final Iterable<Path> paths) {
    AsyncTask.execute(new Runnable() {
      @Override
      public void run() {
        OperationService.delete(context, paths);
      }
    });
  }

  private String getConfirmMessage(int size) {
    return context.getResources().getQuantityString(
        R.plurals.confirm_delete_question, size, size);
  }
}
