package l.files.premium;

import android.content.pm.ApplicationInfo;
import android.os.AsyncTask;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import l.files.ui.base.app.OptionsMenuAction;

import static android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE;
import static android.view.Menu.NONE;
import static android.widget.Toast.LENGTH_SHORT;
import static l.files.base.Objects.requireNonNull;

public final class ConsumeTestPurchasesOnDebugMenu extends OptionsMenuAction {

    private final PremiumLock premiumLock;

    public ConsumeTestPurchasesOnDebugMenu(
            PremiumLock premiumLock
    ) {
        super(R.id.consume_test_purchase);
        this.premiumLock = requireNonNull(premiumLock);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(NONE, id(), NONE, R.string.consume_test_purchases);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(id());
        if (item != null) {
            ApplicationInfo app = premiumLock.getActivity().getApplicationInfo();
            boolean debug = 0 != (app.flags & FLAG_DEBUGGABLE);
            item.setVisible(debug);
        }
    }

    @Override
    protected void onItemSelected(MenuItem item) {
        new AsyncTask<Void, Void, Exception>() {

            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    premiumLock.consumeTestPurchases();
                    return null;
                } catch (Exception e) {
                    return e;
                }
            }

            @Override
            protected void onPostExecute(Exception e) {
                super.onPostExecute(e);

                if (e != null) {
                    Toast.makeText(
                            premiumLock.getActivity(),
                            e.getMessage(),
                            LENGTH_SHORT
                    ).show();
                } else {
                    Toast.makeText(
                            premiumLock.getActivity(),
                            android.R.string.ok,
                            LENGTH_SHORT
                    ).show();
                }
            }

        }.execute();
    }
}
