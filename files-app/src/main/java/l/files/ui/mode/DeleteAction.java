package l.files.ui.mode;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.google.common.base.Supplier;

import java.util.Set;

import l.files.R;
import l.files.common.widget.MultiChoiceModeAction;
import l.files.fs.Path;
import l.files.operations.OperationService;

import static android.content.DialogInterface.OnClickListener;
import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
import static android.view.MenuItem.SHOW_AS_ACTION_WITH_TEXT;
import static com.google.common.base.Preconditions.checkNotNull;

public final class DeleteAction extends MultiChoiceModeAction {

  private final Context context;
  private final Supplier<Set<Path>> supplier;

  public DeleteAction(Context context, Supplier<Set<Path>> supplier) {
    super(R.id.delete);
    this.context = checkNotNull(context);
    this.supplier = checkNotNull(supplier);
  }

  @Override
  public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    menu.add(NONE, id(), NONE, R.string.delete)
        .setShowAsAction(SHOW_AS_ACTION_IF_ROOM | SHOW_AS_ACTION_WITH_TEXT);
    return true;
  }

  @Override
  protected void onItemSelected(final ActionMode mode, MenuItem item) {
    final Set<Path> paths = supplier.get();
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

  private void requestDelete(final Set<Path> paths) {
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
