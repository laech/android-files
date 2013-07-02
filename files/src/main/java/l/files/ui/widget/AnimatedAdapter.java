package l.files.ui.widget;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static l.files.util.Sets.minus;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

public abstract class AnimatedAdapter<T> extends BaseAdapter {

  private static final int ANIMATE_DURATION = 300;

  private long seq = 0;

  private final Map<T, Long> ids = newHashMap();

  private final List<T> items = newArrayList();

  @Override public int getCount() {
    return items.size();
  }

  @Override public T getItem(int position) {
    return items.get(position);
  }

  @Override public long getItemId(int position) {
    return ids.get(getItem(position));
  }

  @Override public boolean hasStableIds() {
    return true;
  }

  @Override public View getView(int position, View view, ViewGroup parent) {
    T item = getItem(position);
    if (view == null) view = newView(item, parent);
    bindView(item, view);
    return view;
  }

  protected abstract View newView(T item, ViewGroup parent);

  protected abstract void bindView(T item, View view);

  protected View inflate(int id, ViewGroup parent) {
    return LayoutInflater.from(parent.getContext()).inflate(id, parent, false);
  }

  public void replaceAll(final ListView list,
      final Collection<? extends T> newItems, boolean animate) {

    final Map<Long, Integer> idToTops = mapIdToViewTops(list);
    final List<Long> oldVisibleIds = getVisibleIds(list);
    final List<Long> oldIds = newArrayList(ids.values());
    updateIds(newItems);
    updateItems(newItems);
    notifyDataSetChanged();
    
    if (!animate) return;

    list.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
      @Override public boolean onPreDraw() {
        list.getViewTreeObserver().removeOnPreDrawListener(this);

        int nRemovedVisibleItems = minus(oldVisibleIds, getVisibleIds(list))
            .size();
        for (int i = 0; i < list.getChildCount(); i++) {

          View child = list.getChildAt(i);
          int position = list.getPositionForView(child);
          int newTop = child.getTop();
          Integer oldTop = idToTops.get(getItemId(position));

          if (oldTop != null) {
            animateItemMovement(child, oldTop, newTop);
          } else if (oldIds.contains(getItemId(position))) {
            animateOldItemEntrance(child, nRemovedVisibleItems, list);
          } else {
            animateNewItemEntrance(child);
          }
        }

        return true;
      }
    });
  }

  private void updateItems(final Collection<? extends T> newItems) {
    items.clear();
    items.addAll(newItems);
  }

  private List<Long> getVisibleIds(ListView list) {
    int n = list.getChildCount();
    List<Long> items = newArrayListWithCapacity(n);
    for (int i = 0; i < n; i++) {
      int position = list.getPositionForView(list.getChildAt(i));
      items.add(list.getItemIdAtPosition(position));
    }
    return items;
  }

  private Map<Long, Integer> mapIdToViewTops(ListView list) {
    int n = list.getChildCount();
    Map<Long, Integer> idToTops = newHashMapWithExpectedSize(n);
    for (int i = 0; i < n; i++) {
      int position = list.getPositionForView(list.getChildAt(i));
      idToTops.put(getItemId(position), list.getChildAt(i).getTop());
    }
    return idToTops;
  }

  private void updateIds(final Collection<? extends T> items) {
    for (T item : items) {
      if (!ids.containsKey(item)) ids.put(item, seq++);
    }
    ids.keySet().retainAll(items);
  }

  private void animateItemMovement(View child, int oldTop, int newTop) {
    if (oldTop != newTop) {
      int delta = oldTop - newTop;
      child.setTranslationY(delta);
      child.animate().setDuration(ANIMATE_DURATION).translationY(0);
    }
  }

  private void animateOldItemEntrance(View child, int nVisibleItemsRemoved,
      ListView list) {
    child.setTranslationY(list.indexOfChild(child) == 0
        ? child.getHeight() * -1
        : child.getHeight() * nVisibleItemsRemoved);
    child.animate().setDuration(300).translationY(0);
  }

  private void animateNewItemEntrance(View child) {
    child.setAlpha(0);
    child.setTranslationY(-child.getHeight());
    child.animate().setDuration(ANIMATE_DURATION * 2).alpha(1).translationY(0);
  }

}
