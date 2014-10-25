package l.files.ui;

import android.view.View;
import android.widget.ListView;

import java.util.Map;
import java.util.Set;

import static android.view.ViewTreeObserver.OnPreDrawListener;
import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.newHashSetWithExpectedSize;
import static com.google.common.collect.Sets.union;
import static java.lang.Math.max;
import static java.lang.Math.min;

final class Animations {

  private static final long ANIMATE_DURATION = 300;

  private Animations() {}

  /**
   * Animates the changes in the list view. This method needs to be called
   * before the content of the list adapter is changed.
   */
  public static void animatePreDataSetChange(final ListView list) {
    final Map<Long, Integer> oldTops = getViewTops(list);
    final Set<Long> oldVisibleItems = getOnScreenItems(list);
    /*
     * Note: do not make oldItems contain all IDs, as that will cause
     * significant performance impact with large lists. Only include what's on
     * screen and those that are near the top/bottom of the screen.
     */
    final Set<Long> oldItems = union(oldVisibleItems,
        getOffScreenItems(list, list.getChildCount()));

    list.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
      @Override public boolean onPreDraw() {
        list.getViewTreeObserver().removeOnPreDrawListener(this);

        int diff = difference(oldVisibleItems, getOnScreenItems(list)).size();
        for (int i = 0; i < list.getChildCount(); i++) {
          animate(list.getChildAt(i), diff);
        }
        return true;
      }

      private void animate(View child, int nNewItemsOnScreen) {
        long id = list.getItemIdAtPosition(list.getPositionForView(child));

        Integer oldTop = oldTops.get(id);
        if (oldTop != null) {
          animateItemMovement(child, oldTop, child.getTop());
        } else if (oldItems.contains(id)) {
          animateOldItemEntrance(child, nNewItemsOnScreen, list);
        } else {
          animateNewItemEntrance(child);
        }
      }
    });
  }

  /**
   * Gets the IDs of the items on screen.
   */
  private static Set<Long> getOnScreenItems(ListView list) {
    Set<Long> items = newHashSetWithExpectedSize(list.getChildCount());
    for (int i = 0; i < list.getChildCount(); i++) {
      int pos = list.getPositionForView(list.getChildAt(i));
      long id = list.getItemIdAtPosition(pos);
      items.add(id);
    }
    return items;
  }

  /**
   * Gets the IDs of the {@code n} items above and below the screen.
   */
  private static Set<Long> getOffScreenItems(ListView list, int n) {
    Set<Long> items = newHashSetWithExpectedSize(n * 2);
    int first = list.getFirstVisiblePosition();
    int last = list.getLastVisiblePosition();
    for (int i = max(0, first - n); i < first; i++) {
      items.add(list.getItemIdAtPosition(i));
    }
    int end = min(list.getCount(), last + 1 + n);
    for (int i = last + 1; i < end; i++) {
      items.add(list.getItemIdAtPosition(i));
    }
    return items;
  }

  /**
   * Collects the {@link View#getTop()}s of the items on screen, indexed by item
   * IDs.
   */
  private static Map<Long, Integer> getViewTops(ListView list) {
    Map<Long, Integer> tops = newHashMapWithExpectedSize(list.getChildCount());
    for (int i = 0; i < list.getChildCount(); i++) {
      View child = list.getChildAt(i);
      int pos = list.getPositionForView(child);
      long id = list.getItemIdAtPosition(pos);
      tops.put(id, child.getTop());
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
      View child, int nNewItemsOnScreen, ListView list) {
    child.setTranslationY(list.indexOfChild(child) == 0
        ? child.getHeight() * -1
        : child.getHeight() * nNewItemsOnScreen);
    child.animate().setDuration(ANIMATE_DURATION).translationY(0);
  }

  private static void animateNewItemEntrance(View child) {
    child.setAlpha(0);
    child.setTranslationY(-child.getHeight());
    child.animate().setDuration(ANIMATE_DURATION * 2).alpha(1).translationY(0);
  }
}
