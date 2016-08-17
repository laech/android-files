package l.files.ui.browser;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.NativeExpressAdView;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.android.gms.ads.AdRequest.DEVICE_ID_EMULATOR;

final class AdViewHolder extends RecyclerView.ViewHolder {

    private static final AtomicBoolean init = new AtomicBoolean();

    private final NativeExpressAdView adView;
    private boolean adLoaded;

    AdViewHolder(View itemView) {
        super(itemView);

        // TODO need to call destroy/pause on ad view?
        Context context = itemView.getContext();
        adView = new NativeExpressAdView(context);
        adView.setAdUnitId(getAdUnitId(context));
        adView.setAdSize(calculateAdSize(itemView));
        ((ViewGroup) itemView).addView(adView);

        if (init.compareAndSet(false, true)) {
            MobileAds.initialize(
                    context.getApplicationContext(),
                    itemView.getResources().getString(R.string.ad_app_id));
        }
    }

    private static String getAdUnitId(Context context) {
        return context.getString(R.string.ad_unit_browser_express_id);
    }

    private static AdSize calculateAdSize(View itemView) {
        // width: 280dp - 1200dp, height: 80dp - 612dp
        Resources res = itemView.getResources();
        DisplayMetrics metrics = res.getDisplayMetrics();
        int pad = res.getDimensionPixelSize(R.dimen.files_item_space_horizontal);
        int widthDp = (int) ((metrics.widthPixels - pad * 2) / metrics.density);
        return new AdSize(widthDp, 80);
    }

    void bind() {
        if (adLoaded) {
            return;
        }
        adLoaded = true;

        adView.loadAd(new AdRequest.Builder()
                .addTestDevice(DEVICE_ID_EMULATOR)
                .addTestDevice("3D33A77247CFB6111C37C7D2B50E325A") // Nexus 5X
                .build());

        ViewGroup.LayoutParams params = itemView.getLayoutParams();
        if (params instanceof StaggeredGridLayoutManager.LayoutParams) {
            ((StaggeredGridLayoutManager.LayoutParams) params).setFullSpan(true);
        }
    }

}
