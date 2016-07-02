package l.files.premium;

import android.support.v7.view.ActionMode;
import android.view.MenuItem;

import l.files.ui.base.view.ActionModeItem;

import static l.files.base.Objects.requireNonNull;

public abstract class PremiumActionModeItem extends ActionModeItem {

    private final PremiumLock premiumLock;

    public PremiumActionModeItem(
            int id,
            PremiumLock premiumLock
    ) {
        super(id);
        this.premiumLock = requireNonNull(premiumLock);
    }

    @Override
    protected final void onItemSelected(ActionMode mode, MenuItem item) {
        if (premiumLock.isUnlocked()) {
            doOnItemSelected(mode, item);
        } else {
            premiumLock.showPurchaseDialog();
        }
    }

    protected abstract void doOnItemSelected(ActionMode mode, MenuItem item);

}
