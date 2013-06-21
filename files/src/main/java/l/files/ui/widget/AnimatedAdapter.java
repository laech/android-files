package l.files.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static l.files.ui.animation.Animations.*;
import static l.files.ui.util.ListViews.getFirstChildScrollOffset;
import static l.files.ui.util.ListViews.getVisibleItems;
import static l.files.util.Sets.minus;

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

  private void removeAllWithCallback(final Collection<?> items, final Runnable callback) {
    if (items.isEmpty()) {
      if (callback != null) callback.run();
      return;
    }

    final Map<?, Animator> animators = newRemovalAnimationForVisibleItems(items, list);

    if (animators.isEmpty()) {
      removeAndRestoreState(items);
      if (callback != null) callback.run();
      return;
    }

    newAnimatorSet(animators.values(), new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        removeAndRestoreState(animators.keySet());
        list.post(new Runnable() {
          @Override public void run() {
            removeAllWithCallback(minus(items, animators.keySet()), callback);
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

  @Override public void replaceAll(final Collection<? extends T> newItems) {
    removeAllWithCallback(minus(items, newItems), new Runnable() {
      @Override public void run() {
        newItemsToBeAnimated.clear();
        newItemsToBeAnimated.addAll(minus(newItems, items));
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
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
