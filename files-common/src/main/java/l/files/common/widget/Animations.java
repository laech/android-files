package l.files.common.widget;

import android.view.View;
import android.widget.ListView;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.view.ViewTreeObserver.OnPreDrawListener;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static com.google.common.collect.Sets.newHashSetWithExpectedSize;

final class Animations {

  private static final long ANIMATE_DURATION = 300;

  private Animations() {}

  /**
   * Animates the changes in the list view. This method needs to be called
   * before the content of the list adapter is changed.
   */
  public static void animatePreDataSetChange(final ListView list) {
    final Set<Object> oldItems = getItems(list);
    final Map<Object, Integer> tops = getViewTops(list);
    final List<Object> oldVisibleItems = getVisibleItems(list);

    list.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {

      @Override public boolean onPreDraw() {
        list.getViewTreeObserver().removeOnPreDrawListener(this);

        for (int i = 0; i < list.getChildCount(); i++)
          animate(list.getChildAt(i));

        return true;
      }

      private void animate(View child) {
        int position = list.getPositionForView(child);
        Object item = list.getItemAtPosition(position);
        int newTop = child.getTop();
        Integer oldTop = tops.get(item);

        if (oldTop != null) {
          animateItemMovement(child, oldTop, newTop);
        } else if (oldItems.contains(item)) {
          animateOldItemEntrance(child, oldVisibleItems, list);
        } else {
          animateNewItemEntrance(child);
        }
      }
    });
  }

  private static Set<Object> getItems(ListView list) {
    Set<Object> oldItems = newHashSetWithExpectedSize(list.getCount());
    for (int i = 0; i < list.getCount(); i++) {
      oldItems.add(list.getItemAtPosition(i));
    }
    return oldItems;
  }

  private static List<Object> getVisibleItems(ListView list) {
    List<Object> items = newArrayListWithCapacity(list.getChildCount());
    for (int i = 0; i < list.getChildCount(); i++) {
      int position = list.getPositionForView(list.getChildAt(i));
      items.add(list.getItemAtPosition(position));
    }
    return items;
  }

  private static Map<Object, Integer> getViewTops(ListView list) {
    Map<Object, Integer> tops = newHashMapWithExpectedSize(list.getChildCount());
    for (int i = 0; i < list.getChildCount(); i++) {
      int position = list.getPositionForView(list.getChildAt(i));
      tops.put(list.getItemAtPosition(position), list.getChildAt(i).getTop());
    }
    return tops;
  }

  private static void animateItemMovement(View child, int oldTop, int newTop) {
    if (oldTop != newTop) {
      int delta = oldTop - newTop;
      child.setTranslationY(delta);
      child.animate().setDuration(ANIMATE_DURATION).translationY(0);
    }
  }

  private static void animateOldItemEntrance(
      View child, List<Object> oldVisibleItems, ListView list) {

    List<Object> diff = newArrayList(oldVisibleItems);
    diff.removeAll(getVisibleItems(list));
    child.setTranslationY(list.indexOfChild(child) == 0
        ? child.getHeight() * -1
        : child.getHeight() * diff.size());
    child.animate().setDuration(ANIMATE_DURATION).translationY(0);
  }

  private static void animateNewItemEntrance(View child) {
    child.setAlpha(0);
    child.setTranslationY(-child.getHeight());
    child.animate().setDuration(ANIMATE_DURATION * 2).alpha(1).translationY(0);
  }
}
