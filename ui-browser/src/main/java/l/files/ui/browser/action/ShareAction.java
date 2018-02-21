package l.files.ui.browser.action;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import l.files.fs.Stat;
import l.files.ui.base.fs.FileInfo;
import l.files.ui.base.selection.Selection;
import l.files.ui.base.view.ActionModeItem;
import l.files.ui.browser.R;

import static android.content.Intent.ACTION_SEND_MULTIPLE;
import static android.content.Intent.EXTRA_STREAM;
import static android.content.Intent.createChooser;
import static kotlin.collections.CollectionsKt.arrayListOf;
import static kotlin.collections.CollectionsKt.mapTo;
import static l.files.base.Objects.requireNonNull;
import static l.files.fs.media.MediaTypes.MEDIA_TYPE_OCTET_STREAM;

public final class ShareAction extends ActionModeItem
        implements Selection.Callback {

    private final Selection<?, FileInfo> selection;
    private final Context context;

    public ShareAction(
            Selection<?, FileInfo> selection,
            Context context
    ) {
        super(R.id.share);
        this.selection = requireNonNull(selection);
        this.context = requireNonNull(context);
    }

    @Override
    public void onSelectionChanged() {
        if (mode != null) {
            updateMenuItem(mode.getMenu());
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        super.onCreateActionMode(mode, menu);
        mode.getMenuInflater().inflate(R.menu.share, menu);
        selection.addWeaklyReferencedCallback(this);
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        super.onDestroyActionMode(mode);
        selection.removeCallback(this);
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        super.onPrepareActionMode(mode, menu);
        updateMenuItem(menu);
        return true;
    }

    private void updateMenuItem(Menu menu) {
        MenuItem item = menu.findItem(id());
        if (item == null) {
            return;
        }

        if (selection.isEmpty()) {
            item.setEnabled(false);
            return;
        }

        for (FileInfo file : selection.values()) {
            Stat stat = file.linkTargetOrSelfStat();
            if (stat == null || !stat.isRegularFile()) {
                item.setEnabled(false);
                return;
            }
        }

        item.setEnabled(true);
    }

    @Override
    protected void onItemSelected(ActionMode mode, MenuItem item) {
        context.startActivity(createChooser(
                createShareIntent(),
                context.getText(R.string.share)
        ));
        mode.finish();
    }

    private Intent createShareIntent() {
        return new Intent(ACTION_SEND_MULTIPLE)
                .setType(MEDIA_TYPE_OCTET_STREAM)
                .putParcelableArrayListExtra(EXTRA_STREAM, selectionUris());
    }

    private ArrayList<Uri> selectionUris() {
        return mapTo(selection.values(), arrayListOf(), f -> f.linkTargetOrSelfPath().toUri());
    }

}
