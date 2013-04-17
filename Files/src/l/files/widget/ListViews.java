package l.files.widget;

import static l.files.animation.Animations.listItemRemoval;
import static l.files.widget.ArrayAdapters.removeAll;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public final class ListViews {

  // TODO experimental

  public static void removeCheckedItems(ListView list, ArrayAdapter<?> adapter) {
    removeItems(list, adapter, getCheckedItems(list));
  }

  static List<Object> getCheckedItems(ListView list) {
    int n = list.getCheckedItemCount();
    List<Object> items = new ArrayList<Object>(n);
    for (int i = 0; i < list.getCount(); i++) {
      if (items.size() == n) break;
      if (list.isItemChecked(i)) items.add(list.getItemAtPosition(i));
    }
    return items;
  }

  static void removeItems(
      final ListView list, final ArrayAdapter<?> adapter, final List<Object> items) {

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
            removeItems(list, adapter, items);
          }
        });
      }
    });
    set.start();
  }

  private ListViews() {
  }
}
