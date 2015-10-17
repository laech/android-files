package l.files.ui.browser;

import android.app.Activity;
import android.view.MenuItem;

import l.files.ui.base.app.OptionsMenuAction;

import static java.util.Objects.requireNonNull;

final class GoBackOnHomePressedAction extends OptionsMenuAction {

    private final Activity activity;

    GoBackOnHomePressedAction(Activity activity) {
        super(android.R.id.home);
        this.activity = requireNonNull(activity, "activity");
    }

    @Override
    protected void onItemSelected(MenuItem item) {
        activity.onBackPressed();
    }
}
