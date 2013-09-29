package l.files.app;

import static android.animation.LayoutTransition.CHANGING;
import static android.animation.LayoutTransition.TransitionListener;
import static android.support.v4.view.ViewPager.OnPageChangeListener;
import static android.view.View.*;
import static android.view.ViewGroup.LayoutParams;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

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
import l.files.R;

public final class ViewPagerTabBar
    implements OnPageChangeListener, OnClickListener, TransitionListener {

  private final HorizontalScrollView root;
  private final LinearLayout tabs;
  private final LayoutTransition transition;
  private ViewPager pager;

  public ViewPagerTabBar(Context context) {
    root = (HorizontalScrollView) inflate(context, R.layout.tab_container);
    root.setHorizontalFadingEdgeEnabled(true);
    tabs = (LinearLayout) root.findViewById(R.id.tab_container);
    transition = tabs.getLayoutTransition();
    transition.enableTransitionType(CHANGING);
    transition.addTransitionListener(this);
  }

  public void setViewPager(ViewPager p) {
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

  public void addTab(CharSequence title) {
    ViewHolder holder = new ViewHolder(inflate(pager.getContext(), R.layout.tab));
    holder.root.setOnClickListener(this);
    holder.title.setText(title);
    tabs.addView(holder.root, new LayoutParams(WRAP_CONTENT, MATCH_PARENT));
  }

  public void removeTab(int position) {
    tabs.removeViewAt(position);
  }

  private View inflate(Context context, int layoutResId) {
    return LayoutInflater.from(context).inflate(layoutResId, null, false);
  }

  @Override public void onClick(View v) {
    pager.setCurrentItem(tabs.indexOfChild(v), true);
  }

  public void updateTab(final int position, CharSequence title, boolean showBack) {
    ViewHolder holder = ViewHolder.get(tabs.getChildAt(position));
    holder.title.setText(title);
    holder.back.setVisibility(showBack ? VISIBLE : GONE);
  }

  private void scrollToTab(int position) {
    View tab = tabs.getChildAt(position);
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

  @Override public void onPageSelected(int position) {
    updateTabsStatus(position);
    if (!transition.isRunning()) {
      scrollToTab(position);
    }
  }

  @Override public void onPageScrollStateChanged(int state) {}

  @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

  @Override public void startTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {}

  @Override public void endTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {
    final int position = container.indexOfChild(view);
    if (position == pager.getCurrentItem()) {
      scrollToTab(position);
    }
    updateTabsStatus(pager.getCurrentItem());
  }

  private void updateTabsStatus(int current) {
    for (int i = 0; i < tabs.getChildCount(); i++) {
      boolean selected = i == current;
      ViewHolder holder = ViewHolder.get(tabs.getChildAt(i));
      holder.root.setSelected(selected);
      if (!selected) {
        holder.back.setVisibility(GONE);
      }
    }
  }

  static class ViewHolder {
    final View root;
    final TextView title;
    final ImageView back;

    ViewHolder(View root) {
      this.root = root;
      this.title = (TextView) root.findViewById(R.id.title);
      this.back = (ImageView) root.findViewById(R.id.back_indicator);
      root.setTag(this);
    }

    static ViewHolder get(View root) {
      return (ViewHolder) root.getTag();
    }
  }
}
