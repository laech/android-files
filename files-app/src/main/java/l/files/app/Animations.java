package l.files.app;

import android.view.View;
import android.widget.ListView;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.TLongIntMap;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;

import static android.view.ViewTreeObserver.OnPreDrawListener;

final class Animations {

  private static final long ANIMATE_DURATION = 300;

  private Animations() {}

  /**
   * Animates the changes in the list view. This method needs to be called
   * before the content of the list adapter is changed.
   */
  public static void animatePreDataSetChange(final ListView list) {
    final TLongSet oldItems = getItemIds(list);
    final TLongIntMap tops = getViewTopsById(list);
    final TLongList oldVisibleItems = getVisibleItemIds(list);

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

  private static TLongSet getItemIds(ListView list) {
    float loadFactor = 0.75f;
    int capacity = (int) (list.getCount() / loadFactor + 1);
    TLongSet ids = new TLongHashSet(capacity, loadFactor);
    for (int i = 0; i < list.getCount(); i++) {
      ids.add(list.getItemIdAtPosition(i));
    }
    return ids;
  }

  private static TLongList getVisibleItemIds(ListView list) {
    TLongList items = new TLongArrayList(list.getChildCount());
    for (int i = 0; i < list.getChildCount(); i++) {
      int position = list.getPositionForView(list.getChildAt(i));
      items.add(list.getItemIdAtPosition(position));
    }
    return items;
  }

  private static TLongIntMap getViewTopsById(ListView list) {
    float loadFactor = 0.75f;
    int capacity = (int) (list.getCount() / loadFactor + 1);
    TLongIntMap tops = new TLongIntHashMap(capacity, loadFactor);
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
      View child, TLongList oldVisibleItemIds, ListView list) {

    TLongList diff = new TLongArrayList(oldVisibleItemIds);
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
