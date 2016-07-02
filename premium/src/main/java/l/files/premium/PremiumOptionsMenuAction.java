package l.files.premium;

import android.view.MenuItem;

import l.files.ui.base.app.OptionsMenuAction;

import static l.files.base.Objects.requireNonNull;

public abstract class PremiumOptionsMenuAction extends OptionsMenuAction {

    private final PremiumLock premiumLock;

    public PremiumOptionsMenuAction(
            int id,
            PremiumLock premiumLock
    ) {
        super(id);
        this.premiumLock = requireNonNull(premiumLock);
    }

    @Override
    protected void onItemSelected(MenuItem item) {
        if (premiumLock.isUnlocked()) {
            doOnItemSelected(item);
        } else {
            premiumLock.showPurchaseDialog();
        }
    }

    protected abstract void doOnItemSelected(MenuItem item);

}
