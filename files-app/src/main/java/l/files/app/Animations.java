package l.files.app;

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
    final Set<Long> oldItems = getItemIds(list);
    final Map<Long, Integer> tops = getViewTopsById(list);
    final List<Long> oldVisibleItems = getVisibleItemIds(list);

    list.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {

      @Override public boolean onPreDraw() {
        list.getViewTreeObserver().removeOnPreDrawListener(this);

        for (int i = 0; i < list.getChildCount(); i++)
          animate(list.getChildAt(i));

        return true;
      }

      private void animate(View child) {
        int position = list.getPositionForView(child);
        long id = list.getItemIdAtPosition(position);
        int newTop = child.getTop();

        if (tops.containsKey(id)) {
          animateItemMovement(child, tops.get(id), newTop);
        } else if (oldItems.contains(id)) {
          animateOldItemEntrance(child, oldVisibleItems, list);
        } else {
          animateNewItemEntrance(child);
        }
      }
    });
  }

  private static Set<Long> getItemIds(ListView list) {
    Set<Long> ids = newHashSetWithExpectedSize(list.getCount());
    for (int i = 0; i < list.getCount(); i++) {
      ids.add(list.getItemIdAtPosition(i));
    }
    return ids;
  }

  private static List<Long> getVisibleItemIds(ListView list) {
    List<Long> items = newArrayListWithCapacity(list.getChildCount());
    for (int i = 0; i < list.getChildCount(); i++) {
      int position = list.getPositionForView(list.getChildAt(i));
      items.add(list.getItemIdAtPosition(position));
    }
    return items;
  }

  private static Map<Long, Integer> getViewTopsById(ListView list) {
    Map<Long, Integer> tops = newHashMapWithExpectedSize(list.getChildCount());
    for (int i = 0; i < list.getChildCount(); i++) {
      int position = list.getPositionForView(list.getChildAt(i));
      tops.put(list.getItemIdAtPosition(position), list.getChildAt(i).getTop());
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
      View child, List<Long> oldVisibleItemIds, ListView list) {

    List<Long> diff = newArrayList(oldVisibleItemIds);
    diff.removeAll(getVisibleItemIds(list));
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
