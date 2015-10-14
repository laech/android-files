package l.files.ui.operations;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Collection;

import l.files.fs.File;
import l.files.operations.OperationService;
import l.files.ui.base.selection.Selection;
import l.files.ui.base.view.ActionModeItem;

import static android.content.DialogInterface.OnClickListener;
import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static java.util.Objects.requireNonNull;

public final class DeleteAction extends ActionModeItem {
    private final Context context;
    private final Selection<File> selection;

    public DeleteAction(final Context context, final Selection<File> selection) {
        super(R.id.delete);
        this.context = requireNonNull(context);
        this.selection = requireNonNull(selection);
    }

    @Override
    public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
        super.onCreateActionMode(mode, menu);
        menu.add(NONE, id(), NONE, R.string.delete)
                .setShowAsAction(SHOW_AS_ACTION_NEVER);
        return true;
    }

    @Override
    protected void onItemSelected(final ActionMode mode, final MenuItem item) {
        final Collection<File> files = selection.copy();
        new AlertDialog.Builder(context)
                .setMessage(getConfirmMessage(files.size()))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.delete, new OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        requestDelete(files);
                        mode.finish();
                    }
                })
                .show();
    }

    private void requestDelete(final Collection<? extends File> files) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                OperationService.delete(context, files);
            }
        });
    }

    private String getConfirmMessage(final int size) {
        return context.getResources().getQuantityString(
                R.plurals.confirm_delete_question, size, size);
    }
}
