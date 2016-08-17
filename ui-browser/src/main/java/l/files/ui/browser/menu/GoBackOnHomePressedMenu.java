package l.files.ui.browser.menu;

import android.app.Activity;
import android.view.MenuItem;

import l.files.ui.base.app.OptionsMenuAction;

import static l.files.base.Objects.requireNonNull;

public final class GoBackOnHomePressedMenu extends OptionsMenuAction {

    private final Activity activity;

    public GoBackOnHomePressedMenu(Activity activity) {
        super(android.R.id.home);
        this.activity = requireNonNull(activity, "activity");
    }

    @Override
    protected void onItemSelected(MenuItem item) {
        activity.onBackPressed();
    }
}
