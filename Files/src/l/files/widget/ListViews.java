package l.files.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static l.files.animation.Animations.listItemRemoval;
import static l.files.widget.ArrayAdapters.removeAll;

public final class ListViews {

  private ListViews() {
  }

  // TODO experimental

  public static List<Object> getCheckedItems(ListView list) {
    int n = list.getCheckedItemCount();
    List<Object> items = newArrayListWithCapacity(n);
    for (int i = 0; i < list.getCount(); i++) {
      if (items.size() == n) break;
      if (list.isItemChecked(i)) items.add(list.getItemAtPosition(i));
    }
    return items;
  }

  public static <T> Iterable<T> getCheckedItems(ListView list, Class<T> type) {
    return filter(getCheckedItems(list), type);
  }

  public static void removeItems(
      final ListView list,
      final ArrayAdapter<?> adapter,
      final Collection<? extends Object> items,
      final Runnable onFinish) {

    final List<Animator> animators = new ArrayList<Animator>();
    final List<Object> visibleItemsToRemove = new ArrayList<Object>();

    int first = list.getFirstVisiblePosition();
    int last = list.getLastVisiblePosition();

    for (int i = first; i <= last; i++) {
      Object item = list.getItemAtPosition(i);
      if (items.contains(item)) {
        visibleItemsToRemove.add(item);
        animators.add(listItemRemoval(list.getChildAt(i - first)));
      }
    }

    if (visibleItemsToRemove.isEmpty()) {
      if (!items.isEmpty()) removeAll(adapter, items);
      if (onFinish != null) onFinish.run();
      return;
    }

    AnimatorSet set = new AnimatorSet();
    set.playTogether(animators);
    set.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        removeAll(adapter, visibleItemsToRemove);
        items.removeAll(visibleItemsToRemove);
        list.post(new Runnable() {
          @Override
          public void run() {
            removeItems(list, adapter, items, onFinish);
          }
        });
      }
    });
    set.start();
  }
}
