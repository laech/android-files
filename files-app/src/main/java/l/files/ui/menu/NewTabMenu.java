package l.files.ui.menu;

import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import l.files.R;
import l.files.common.app.OptionsMenuAction;
import l.files.ui.browser.FilesActivity;

import static android.content.Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static java.util.Objects.requireNonNull;

/**
 * Menu to open a new tab to view files.
 */
public final class NewTabMenu extends OptionsMenuAction
{
    private final Context context;

    public NewTabMenu(final Context context)
    {
        super(R.id.new_tab);
        this.context = requireNonNull(context, "context");
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        menu.add(NONE, id(), NONE, R.string.new_tab)
                .setShowAsAction(SHOW_AS_ACTION_NEVER);
    }

    @Override
    protected void onItemSelected(final MenuItem item)
    {
        final int flags
                = FLAG_ACTIVITY_NEW_DOCUMENT
                | FLAG_ACTIVITY_MULTIPLE_TASK;

        context.startActivity(
                new Intent(context, FilesActivity.class)
                        .addFlags(flags));
    }
}
