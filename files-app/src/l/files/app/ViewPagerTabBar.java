package l.files.app;

import android.animation.LayoutTransition;
import android.annotation.TargetApi;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.squareup.otto.Bus;
import l.files.R;

import static android.animation.LayoutTransition.CHANGING;
import static android.animation.LayoutTransition.TransitionListener;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.support.v4.view.ViewPager.OnPageChangeListener;
import static android.view.View.*;
import static android.view.ViewGroup.LayoutParams;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.google.common.base.Preconditions.checkNotNull;

public final class ViewPagerTabBar
        implements OnPageChangeListener, OnClickListener, TransitionListener {

    private final HorizontalScrollView mRoot;
    private final LinearLayout mTabs;
    private final LayoutTransition mTransition;
    private final Bus mBus;
    private ViewPager mPager;

    public ViewPagerTabBar(Context context, Bus bus) {
        mBus = checkNotNull(bus, "bus");
        mRoot = (HorizontalScrollView) inflate(context, R.layout.tab_container);
        mRoot.setHorizontalFadingEdgeEnabled(true);
        mTabs = (LinearLayout) mRoot.findViewById(R.id.tab_container);
        mTransition = mTabs.getLayoutTransition();
        mTransition.addTransitionListener(this);
        if (SDK_INT >= JELLY_BEAN) {
            enableChangingTransition();
        }
    }

    @TargetApi(JELLY_BEAN)
    private void enableChangingTransition() {
        mTransition.enableTransitionType(CHANGING);
    }

    public void setViewPager(ViewPager p) {
        if (mPager != null) {
            throw new IllegalStateException("ViewPager is already set");
        }
        mPager = p;
        mPager.setOnPageChangeListener(this);
        final PagerAdapter adapter = p.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            addTab(adapter.getPageTitle(i));
        }
        onPageSelected(p.getCurrentItem());
    }

    public View getRootContainer() {
        return mRoot;
    }

    @SuppressWarnings("ConstantConditions")
    public Tab getTabAt(int position) {
        return (Tab) mTabs.getChildAt(position).getTag();
    }

    public int getTabCount() {
        return mTabs.getChildCount();
    }

    public void addTab(CharSequence title) {
        final Tab holder = Tab.create(inflate(mPager.getContext(), R.layout.tab));
        holder.mRoot.setOnClickListener(this);
        holder.mTitle.setText(title);
        mTabs.addView(holder.mRoot, new LayoutParams(WRAP_CONTENT, MATCH_PARENT));
    }

    public void removeTab(int position) {
        mTabs.removeViewAt(position);
    }

    public void updateTab(final int position, CharSequence title, boolean showBack) {
        final View view = mTabs.getChildAt(position);
        if (view != null) {
            final Tab holder = Tab.get(view);
            holder.mTitle.setText(title);
            holder.mBackIndicator.setVisibility(showBack ? VISIBLE : GONE);
        }
    }

    private void updateTabsStatus(int currentPosition) {
        for (int i = 0; i < mTabs.getChildCount(); i++) {
            final boolean selected = i == currentPosition;
            final Tab holder = Tab.get(mTabs.getChildAt(i));
            holder.mRoot.setSelected(selected);
            if (!selected) {
                holder.mBackIndicator.setVisibility(GONE);
            }
        }
    }

    private void scrollToTab(int position) {
        scrollToTab(mTabs.getChildAt(position));
    }

    private void scrollToTab(View tab) {
        final int tabLeft = tab.getLeft();
        final int tabRight = tabLeft + tab.getMeasuredWidth();
        final int scrollLeft = mRoot.getScrollX();
        final int scrollRight = scrollLeft + mRoot.getMeasuredWidth();
        final int padding = 100;
        if (tabLeft < scrollLeft) {
            mRoot.smoothScrollBy(tabLeft - scrollLeft - padding, 0);
        } else if (tabRight > scrollRight) {
            mRoot.smoothScrollBy(tabRight - scrollRight + padding, 0);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.isSelected()) {
            mBus.post(OnBackSelected.INSTANCE);
        } else {
            mPager.setCurrentItem(mTabs.indexOfChild(v), true);
        }
    }

    @Override
    public void onPageSelected(int position) {
        updateTabsStatus(position);
        if (!mTransition.isRunning()) {
            scrollToTab(position);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void startTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {
    }

    @Override
    public void endTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {
        final int position = container.indexOfChild(view);
        if (position == mPager.getCurrentItem()) {
            scrollToTab(position);
        }
        updateTabsStatus(mPager.getCurrentItem());
    }

    private View inflate(Context context, int layoutResId) {
        return LayoutInflater.from(context).inflate(layoutResId, null, false);
    }

    public static final class Tab {
        final View mRoot;
        final TextView mTitle;
        final ImageView mBackIndicator;

        private Tab(View root) {
            this.mRoot = root;
            this.mTitle = (TextView) root.findViewById(R.id.title);
            this.mBackIndicator = (ImageView) root.findViewById(R.id.back_indicator);
        }

        static Tab create(View root) {
            final Tab holder = new Tab(root);
            root.setTag(holder);
            return holder;
        }

        static Tab get(View root) {
            return (Tab) root.getTag();
        }

        public View getRootView() {
            return mRoot;
        }

        public TextView getTitleView() {
            return mTitle;
        }

        public ImageView getBackIndicatorView() {
            return mBackIndicator;
        }
    }

    enum OnBackSelected {
        INSTANCE
    }
}
