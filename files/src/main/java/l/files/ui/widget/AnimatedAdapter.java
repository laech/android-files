package l.files.ui.widget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static l.files.ui.widget.Animations.animatePreDataSetChange;

public abstract class AnimatedAdapter<T> extends BaseAdapter {

  private List<T> items = newArrayList();

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
    return view;
  }

  protected abstract View newView(T item, ViewGroup parent);

  protected abstract void bindView(T item, View view);

  protected View inflate(int id, ViewGroup parent) {
    return LayoutInflater.from(parent.getContext()).inflate(id, parent, false);
  }

  public void replaceAll(
      ListView list, Collection<? extends T> newItems, boolean animate) {

    if (animate) animatePreDataSetChange(list);

    items = newArrayList(newItems);
    notifyDataSetChanged();
  }

}
