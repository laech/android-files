package l.files.premium;

import android.os.AsyncTask;
import android.os.RemoteException;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONException;

import l.files.ui.base.app.OptionsMenuAction;

import static android.view.Menu.NONE;
import static android.widget.Toast.LENGTH_SHORT;
import static l.files.base.Objects.requireNonNull;
import static l.files.premium.BuildConfig.DEBUG;

public final class ConsumeTestPurchasesOnDebug extends OptionsMenuAction {

    private final PremiumLock premiumLock;

    public ConsumeTestPurchasesOnDebug(
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
            item.setVisible(DEBUG);
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
                } catch (RemoteException e) {
                    return e;
                } catch (JSONException e) {
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
                }
            }

        }.execute();
    }
}
