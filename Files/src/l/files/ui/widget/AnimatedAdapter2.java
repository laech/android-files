package l.files.ui.widget;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

public abstract class AnimatedAdapter2<T>
    extends BaseAdapter implements UpdatableAdapter<T>, OnPreDrawListener {

  private final Map<T, Integer> ids = new HashMap<T, Integer>();
  private final Map<Long, Integer> idToTops = new HashMap<Long, Integer>();
  private int seq = 0;

  private final ListView list;
  private final List<T> items;

  public AnimatedAdapter2(ListView parent) {
    list = checkNotNull(parent, "parent");
    items = newArrayList();
  }

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

  protected View inflate(int viewId, ViewGroup parent) {
    Context context = parent.getContext();
    return LayoutInflater.from(context).inflate(viewId, parent, false);
  }

  @Override public void replaceAll(final Collection<? extends T> newItems) {
    int firstVisiblePosition = list.getFirstVisiblePosition();
    for (int i = 0; i < list.getChildCount(); ++i) {
      final View child = list.getChildAt(i);
      int position = firstVisiblePosition + i;
      idToTops.put(getItemId(position), child.getTop());
    }

    for (T item : newItems)
      if (!ids.containsKey(item)) ids.put(item, seq++);

    ids.keySet().retainAll(newItems);

    items.clear();
    items.addAll(newItems);
    notifyDataSetChanged();

    list.getViewTreeObserver().addOnPreDrawListener(this);
  }

  @Override public boolean onPreDraw() {
    list.getViewTreeObserver().removeOnPreDrawListener(this);

    int firstVisiblePosition = list.getFirstVisiblePosition();
    for (int i = 0; i < list.getChildCount(); ++i) {
      View child = list.getChildAt(i);
      int position = firstVisiblePosition + i;
      long id = getItemId(position);
      Integer startTop = idToTops.get(id);
      int top = child.getTop();
      if (startTop != null) {
        if (startTop != top) {
          int delta = startTop - top;
          child.setTranslationY(delta);
          child.animate().setDuration(150).translationY(0);
        }
      } else {
        child.setAlpha(0);
        child.animate().setStartDelay(300).setDuration(150).alpha(1);
      }
    }

    idToTops.clear();
    Log.w("HELLO", "a");
    return true;
  }
}
