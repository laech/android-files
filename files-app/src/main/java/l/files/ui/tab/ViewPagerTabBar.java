package l.files.ui.tab;

import android.animation.LayoutTransition;
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

import de.greenrobot.event.EventBus;
import l.files.R;

import static android.animation.LayoutTransition.TransitionListener;
import static android.support.v4.view.ViewPager.OnPageChangeListener;
import static android.view.View.GONE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;
import static android.view.ViewGroup.LayoutParams;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static java.util.Objects.requireNonNull;

public final class ViewPagerTabBar
    implements OnPageChangeListener, OnClickListener, TransitionListener {

  private final HorizontalScrollView root;
  private final LinearLayout tabs;
  private final EventBus bus;
  private ViewPager pager;

  public ViewPagerTabBar(Context context, EventBus bus) {
    this.bus = requireNonNull(bus, "bus");
    this.root = (HorizontalScrollView) inflate(context, R.layout.tab_container);
    this.tabs = (LinearLayout) root.findViewById(R.id.tab_container);
    this.tabs.getLayoutTransition().addTransitionListener(this);
  }

  public void setViewPager(ViewPager p) {
    if (pager != null) {
      throw new IllegalStateException("ViewPager is already set");
    }
    pager = p;
    pager.setOnPageChangeListener(this);
    PagerAdapter adapter = p.getAdapter();
    for (int i = 0; i < adapter.getCount(); i++) {
      addTab(adapter.getPageTitle(i));
    }
    onPageSelected(p.getCurrentItem());
  }

  public View getRootContainer() {
    return root;
  }

  public Tab getTabAt(int position) {
    return (Tab) tabs.getChildAt(position).getTag();
  }

  public int getTabCount() {
    return tabs.getChildCount();
  }

  public void addTab(CharSequence title) {
    Tab holder = Tab.create(inflate(pager.getContext(), R.layout.tab));
    holder.root.setOnClickListener(this);
    holder.title.setText(title);
    tabs.addView(holder.root, new LayoutParams(WRAP_CONTENT, MATCH_PARENT));
  }

  public void removeTab(int position) {
    tabs.removeViewAt(position);
  }

  public void updateTab(int position, CharSequence title, boolean showBack) {
    View view = tabs.getChildAt(position);
    if (view != null) {
      Tab holder = Tab.get(view);
      holder.title.setText(title);
      holder.backIndicator.setVisibility(showBack ? VISIBLE : GONE);
      scrollToTab(pager.getCurrentItem());
    }
  }

  private void updateTabsStatus(int currentPosition) {
    for (int i = 0; i < tabs.getChildCount(); i++) {
      boolean selected = i == currentPosition;
      Tab holder = Tab.get(tabs.getChildAt(i));
      holder.root.setSelected(selected);
      if (!selected) {
        holder.backIndicator.setVisibility(GONE);
      }
    }
  }

  private void scrollToTab(int position) {
    scrollToTab(tabs.getChildAt(position));
  }

  private void scrollToTab(View tab) {
    int tabLeft = tab.getLeft();
    int tabRight = tabLeft + tab.getMeasuredWidth();
    int scrollLeft = root.getScrollX();
    int scrollRight = scrollLeft + root.getMeasuredWidth();
    int padding = 100;
    if (tabLeft < scrollLeft) {
      root.smoothScrollBy(tabLeft - scrollLeft - padding, 0);
    } else if (tabRight > scrollRight) {
      root.smoothScrollBy(tabRight - scrollRight + padding, 0);
    }
  }

  @Override public void onClick(View v) {
    if (v.isSelected()) {
      bus.post(OnUpSelected.INSTANCE);
    } else {
      pager.setCurrentItem(tabs.indexOfChild(v), true);
    }
  }

  @Override public void onPageSelected(int position) {
    updateTabsStatus(position);
    scrollToTab(position);
  }

  @Override public void onPageScrollStateChanged(int state) {
  }

  @Override
  public void onPageScrolled(int position, float positionOffset,
                             int positionOffsetPixels) {
  }

  @Override
  public void startTransition(LayoutTransition transition, ViewGroup container,
                              View view, int transitionType) {
  }

  @Override
  public void endTransition(LayoutTransition transition, ViewGroup container,
                            View view, int transitionType) {
    int position = container.indexOfChild(view);
    if (position == pager.getCurrentItem()) {
      scrollToTab(position);
    }
    updateTabsStatus(pager.getCurrentItem());
  }

  private View inflate(Context context, int layoutResId) {
    return LayoutInflater.from(context).inflate(layoutResId, null, false);
  }

  public static final class Tab {
    final View root;
    final TextView title;
    final ImageView backIndicator;

    private Tab(View root) {
      this.root = root;
      this.title = (TextView) root.findViewById(R.id.title);
      this.backIndicator = (ImageView) root.findViewById(R.id.back_indicator);
    }

    static Tab create(View root) {
      Tab holder = new Tab(root);
      root.setTag(holder);
      return holder;
    }

    static Tab get(View root) {
      return (Tab) root.getTag();
    }

    public View getRootView() {
      return root;
    }

    public TextView getTitleView() {
      return title;
    }

    public ImageView getBackIndicatorView() {
      return backIndicator;
    }
  }

  public enum OnUpSelected {
    INSTANCE
  }
}
