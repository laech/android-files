package l.files.premium;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.Context.BIND_AUTO_CREATE;
import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;
import static l.files.base.Objects.requireNonNull;
import static l.files.base.content.Contexts.isDebugBuild;

public final class PremiumLock implements ServiceConnection {

    private static final int REQUEST_CODE = 123;
    private static final int BILLING_API_VERSION = 3;
    private static final String PREF_KEY_PREMIUM_UNLOCKED = "premium_unlocked";
    private static final String ITEM_TYPE_INAPP = "inapp";

    private final String skuPremium;

    private final Activity activity;
    private final SharedPreferences pref;
    private volatile IInAppBillingService billingService;

    public PremiumLock(Activity activity) {
        this.activity = requireNonNull(activity);
        this.pref = getDefaultSharedPreferences(activity);
        this.skuPremium = isDebugBuild(activity)
                ? "android.test.purchased"
                : "l.files.premium";
    }

    public Activity getActivity() {
        return activity;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        billingService = IInAppBillingService.Stub.asInterface(service);
        if (billingService != null) {
            new UpdatePurchaseStatus().execute(THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        billingService = null;
    }

    public void onCreate() {
        Intent intent = new Intent(
                "com.android.vending.billing.InAppBillingService.BIND"
        );
        intent.setPackage("com.android.vending");
        activity.bindService(intent, this, BIND_AUTO_CREATE);
    }

    public void onDestroy() {
        activity.unbindService(this);
    }

    public void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {
        if (requestCode != REQUEST_CODE) {
            return;
        }

        int responseCode = data.getIntExtra("RESPONSE_CODE", -1);
        if (resultCode == RESULT_OK && responseCode == 0) {

            pref.edit()
                    .putBoolean(PREF_KEY_PREMIUM_UNLOCKED, true)
                    .apply();

            Toast.makeText(
                    activity,
                    R.string.billing_thank_you,
                    LENGTH_LONG
            ).show();

        } else if (resultCode == RESULT_CANCELED) {

            Toast.makeText(
                    activity,
                    R.string.billing_canceled,
                    LENGTH_SHORT
            ).show();

        } else {

            Toast.makeText(
                    activity,
                    R.string.billing_failed,
                    LENGTH_SHORT
            ).show();
        }
    }

    public static boolean isPremiumPreferenceKey(String key) {
        return PREF_KEY_PREMIUM_UNLOCKED.equals(key);
    }

    public boolean isUnlocked() {
        return pref.getBoolean(PREF_KEY_PREMIUM_UNLOCKED, false);
    }

    public void showPurchaseDialog() {

        if (isUnlocked()) {
            return;
        }

        if (billingService == null) {
            int msg = R.string.billing_unavailable;
            Toast.makeText(activity, msg, LENGTH_LONG).show();
            return;
        }

        try {
            startActivityForPurchase();
        } catch (RemoteException | SendIntentException e) {
            Toast.makeText(activity, e.getMessage(), LENGTH_LONG).show();
        }
    }

    private void startActivityForPurchase() throws RemoteException, SendIntentException {

        Bundle intent = billingService.getBuyIntent(
                BILLING_API_VERSION,
                activity.getPackageName(),
                skuPremium,
                ITEM_TYPE_INAPP,
                null
        );

        PendingIntent pendingIntent = intent.getParcelable("BUY_INTENT");
        if (pendingIntent != null) {
            activity.startIntentSenderForResult(
                    pendingIntent.getIntentSender(),
                    REQUEST_CODE,
                    new Intent(),
                    0,
                    0,
                    0
            );
        } else {
            Toast.makeText(
                    activity,
                    R.string.billing_unavailable,
                    LENGTH_LONG
            ).show();
        }
    }

    private final class UpdatePurchaseStatus extends AsyncTask<Object, Void, Object> {

        @Override
        protected Object doInBackground(Object... params) {
            try {

                return billingService.getPurchases(
                        BILLING_API_VERSION,
                        activity.getPackageName(),
                        ITEM_TYPE_INAPP,
                        null
                );

            } catch (RemoteException e) {
                return e;
            }
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            if (o instanceof Throwable) {
                Log.w(PremiumLock.class.getSimpleName(),
                        "Failed to get purchases.", (Throwable) o);
                return;
            }

            Bundle result = (Bundle) o;
            int response = result.getInt("RESPONSE_CODE");
            if (response != 0) {
                return;
            }

            List<String> items = result.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
            if (items != null) {
                pref.edit()
                        .putBoolean(
                                PREF_KEY_PREMIUM_UNLOCKED,
                                items.contains(skuPremium)
                        )
                        .apply();
            }
        }

    }

    void consumeTestPurchases() throws RemoteException, JSONException {

        if (!isDebugBuild(activity)) {
            throw new IllegalStateException("Only available for debug build.");
        }

        IInAppBillingService service = billingService;
        if (service == null) {
            throw new IllegalStateException("Billing service not available.");
        }

        Bundle result = service.getPurchases(
                BILLING_API_VERSION,
                activity.getPackageName(),
                ITEM_TYPE_INAPP,
                null
        );

        List<String> items = result.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
        if (items == null || items.isEmpty()) {
            return;
        }

        for (String json : items) {

            JSONObject object = new JSONObject(json);
            String productId = object.getString("productId");
            if ("android.test.purchased".equals(productId)) {
                service.consumePurchase(
                        BILLING_API_VERSION,
                        activity.getPackageName(),
                        object.getString("purchaseToken")
                );
            }

        }

        new UpdatePurchaseStatus().execute();
    }

}
