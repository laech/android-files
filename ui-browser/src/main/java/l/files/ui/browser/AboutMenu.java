package l.files.ui.browser;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;

import l.files.ui.R;
import l.files.ui.base.app.OptionsMenuAction;

import static android.content.Intent.ACTION_VIEW;
import static android.view.Menu.CATEGORY_SECONDARY;
import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static java.util.Objects.requireNonNull;

final class AboutMenu extends OptionsMenuAction {

    private final Context context;

    AboutMenu(Context context) {
        super(R.id.about);
        this.context = requireNonNull(context, "context");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(NONE, id(), CATEGORY_SECONDARY, R.string.about)
                .setShowAsAction(SHOW_AS_ACTION_NEVER);
    }

    @Override
    protected void onItemSelected(MenuItem item) {
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        context.startActivity(new Intent(ACTION_VIEW, uri));
    }
}
