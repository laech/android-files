package l.files.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.sort;
import static l.files.ui.animation.Animations.*;
import static l.files.ui.util.ListViews.getFirstChildScrollOffset;
import static l.files.ui.util.ListViews.getVisibleItems;
import static l.files.util.Sets.difference;

public abstract class AnimatedAdapter<T>
    extends BaseAdapter implements UpdatableAdapter<T> {

  private final ListView list;
  private final List<T> items;
  private final Set<T> newItemsToBeAnimated;
  private boolean initialDataSet;

  public AnimatedAdapter(ListView parent) {
    list = checkNotNull(parent, "parent");
    items = newArrayList();
    newItemsToBeAnimated = newHashSet();
    initialDataSet = true;
  }

  @Override public int getCount() {
    return items.size();
  }

  @Override public T getItem(int position) {
    return items.get(position);
  }

  @Override public long getItemId(int position) {
    return position;
  }

  @Override public View getView(int position, View view, ViewGroup parent) {
    T item = getItem(position);
    if (view == null) view = newView(item, parent);
    bindView(item, view);
    animateAddition(item, view);
    return view;
  }

  protected abstract View newView(T item, ViewGroup parent);

  protected abstract void bindView(T item, View view);

  protected boolean animateAddition(T item, View view) {
    boolean animate = newItemsToBeAnimated.remove(item);
    if (animate) newListItemAdditionAnimation(view).start();
    return animate;
  }

  protected View inflate(int viewId, ViewGroup parent) {
    Context context = parent.getContext();
    return LayoutInflater.from(context).inflate(viewId, parent, false);
  }

  @Override
  public void addAll(
      Collection<? extends T> itemsToAdd, Comparator<? super T> comparator) {
    if (itemsToAdd.isEmpty()) return;

    newItemsToBeAnimated.addAll(itemsToAdd);
    items.addAll(itemsToAdd);
    sort(items, comparator);
    notifyDataSetChanged();
  }

  @Override public void removeAll(final Collection<?> itemsToRemove) {
    if (itemsToRemove.isEmpty()) return;

    final Map<?, Animator> animators =
        newRemovalAnimationForVisibleItems(itemsToRemove, list);

    if (animators.isEmpty()) {
      removeAndRestoreState(itemsToRemove);
      return;
    }

    newAnimatorSet(animators.values(), new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        removeAndRestoreState(animators.keySet());
        list.post(new Runnable() {
          @Override public void run() {
            removeAll(difference(itemsToRemove, animators.keySet()));
          }
        });
      }
    }).start();
  }

  private void removeAndRestoreState(Collection<?> itemsToRemove) {
    final List<Object> visibleItems = getVisibleItems(list);
    final int top = getFirstChildScrollOffset(list);
    removeAndNotify(itemsToRemove);
    restore(visibleItems, top);
  }

  private void removeAndNotify(Collection<?> itemsToRemove) {
    items.removeAll(itemsToRemove);
    notifyDataSetChanged();
  }

  private void restore(List<Object> visibleItems, int top) {
    for (Object item : visibleItems) {
      final int index = items.indexOf(item);
      if (index > -1) {
        list.setSelectionFromTop(index, top);
        return;
      }
    }
  }

  @Override public void replaceAll(
      Collection<? extends T> newItems, Comparator<? super T> comparator) {

    newItemsToBeAnimated.clear();
    newItemsToBeAnimated.addAll(newItems);
    newItemsToBeAnimated.removeAll(items);

    final List<T> toBeRemoved = newArrayList(items);
    toBeRemoved.removeAll(newItems);

    items.removeAll(newItems);
    items.addAll(newItems);
    sort(items, comparator);
    notifyDataSetChanged();

    list.post(new Runnable() {
      @Override public void run() {
        removeAll(toBeRemoved);
      }
    });
  }

  @Override public void notifyDataSetChanged() {
    super.notifyDataSetChanged();
    noAdditionAnimationOnInitialDataSet();
  }

  private void noAdditionAnimationOnInitialDataSet() {
    if (initialDataSet) {
      initialDataSet = false;
      newItemsToBeAnimated.clear();
    }
  }

  public void clearPendingAnimations() {
    newItemsToBeAnimated.clear();
  }
}
