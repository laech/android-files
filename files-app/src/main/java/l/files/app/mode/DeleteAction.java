package l.files.app.mode;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;

import java.util.List;

import l.files.R;
import l.files.analytics.AnalyticsAction;
import l.files.common.widget.MultiChoiceModeAction;
import l.files.provider.FilesContract;

import static android.content.DialogInterface.OnClickListener;
import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
import static android.view.MenuItem.SHOW_AS_ACTION_WITH_TEXT;
import static android.widget.AbsListView.MultiChoiceModeListener;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static l.files.common.widget.ListViews.getCheckedItemPositions;
import static l.files.provider.FileCursors.getLocation;

/**
 * Deletes selected files from the list view cursor.
 *
 * @see FilesContract.Files
 */
public final class DeleteAction extends MultiChoiceModeAction {

    private final AbsListView list;

    private DeleteAction(AbsListView list) {
        super(R.id.delete);
        this.list = checkNotNull(list, "list");
    }

    public static MultiChoiceModeListener create(AbsListView list) {
        Context context = list.getContext();
        MultiChoiceModeListener action = new DeleteAction(list);
        return new AnalyticsAction(context, action, "delete");
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        menu.add(NONE, id(), NONE, R.string.delete)
                .setIcon(R.drawable.ic_action_discard)
                .setShowAsAction(SHOW_AS_ACTION_IF_ROOM | SHOW_AS_ACTION_WITH_TEXT);
        return true;
    }

    @Override
    protected void onItemSelected(final ActionMode mode, MenuItem item) {
        final List<String> fileLocations = getCheckedFileLocations();
        new AlertDialog.Builder(list.getContext())
                .setMessage(getConfirmMessage(fileLocations.size()))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.delete, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestDelete(fileLocations);
                        mode.finish();
                    }
                })
                .show();
    }

    private List<String> getCheckedFileLocations() {
        List<Integer> positions = getCheckedItemPositions(list);
        List<String> fileLocations = newArrayListWithCapacity(positions.size());
        for (int position : positions) {
            Cursor cursor = (Cursor) list.getItemAtPosition(position);
            fileLocations.add(getLocation(cursor));
        }
        return fileLocations;
    }

    private void requestDelete(final List<String> fileLocations) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                FilesContract.delete(list.getContext(), fileLocations);
            }
        });
    }

    private String getConfirmMessage(int size) {
        return list.getResources().getQuantityString(R.plurals.confirm_delete_question, size, size);
    }
}
