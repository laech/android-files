package l.files.ui.browser;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.NativeExpressAdView;

import java.util.concurrent.atomic.AtomicBoolean;

import l.files.premium.PremiumLock;
import l.files.ui.base.fs.FileIcons;

import static com.google.android.gms.ads.AdRequest.DEVICE_ID_EMULATOR;
import static l.files.ui.base.view.Views.find;
import static l.files.ui.browser.FilesAdapter.calculateCardContentWidthPixels;

final class AdViewHolder extends RecyclerView.ViewHolder {

    static final int LAYOUT_ID = R.layout.files_grid_ad;

    private static final AtomicBoolean init = new AtomicBoolean();

    private final NativeExpressAdView adView;
    private boolean adLoaded;

    AdViewHolder(View itemView, PremiumLock premiumLock) {
        super(itemView);

        // TODO need to call destroy/pause on ad view?
        Context context = itemView.getContext();
        CardView card = find(R.id.card, this);
        adView = new NativeExpressAdView(context);
        adView.setAdUnitId(getAdUnitId(context));
        adView.setAdSize(calculateAdSize(card));
        adView.setAdListener(newAdListener());
        card.addView(adView);

        configureRemoveAdView(premiumLock);

        if (init.compareAndSet(false, true)) {
            MobileAds.initialize(
                    context.getApplicationContext(),
                    itemView.getResources().getString(R.string.ad_app_id));
        }
    }

    private AdListener newAdListener() {
        return new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                int duration = adView.getResources().getInteger(
                        android.R.integer.config_shortAnimTime);
                adView.setAlpha(0);
                adView.animate().alpha(1).setDuration(duration);
            }
        };
    }

    private static String getAdUnitId(Context context) {
        return context.getString(R.string.ad_unit_browser_express_id);
    }

    private static AdSize calculateAdSize(CardView card) {
        // width: 280dp - 1200dp, height: 80dp - 612dp
        DisplayMetrics metrics = card.getResources().getDisplayMetrics();
        int widthPx = calculateCardContentWidthPixels(card, 1);
        int widthDp = (int) (widthPx / metrics.density);
        if (widthDp > 1200) {
            widthDp = 1200;
        }
        if (widthDp < 280) {
            widthDp = 280;
        }
        return new AdSize(widthDp, 80);
    }

    private void configureRemoveAdView(final PremiumLock premiumLock) {
        AssetManager assets = itemView.getContext().getAssets();
        TextView removeAdView = find(R.id.remove_ad, this);
        removeAdView.setTypeface(FileIcons.font(assets));
        removeAdView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                premiumLock.showPurchaseDialog();
            }
        });
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
