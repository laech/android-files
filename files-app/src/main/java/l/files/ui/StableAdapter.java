package l.files.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.emptyList;

public abstract class StableAdapter<T> extends BaseAdapter {

  private static final Map<Object, Long> ids = newHashMap();

  private List<T> items = emptyList();

  @SuppressWarnings("unchecked")
  public void setItems(List<? extends T> items) {
    this.items = (List<T>) checkNotNull(items);
    notifyDataSetChanged();
  }

  @Override public boolean hasStableIds() {
    return true;
  }

  @Override public long getItemId(int position) {
    Object object = getItemIdObject(position);
    Long id = ids.get(object);
    if (id == null) {
      id = ids.size() + 1L;
      ids.put(object, id);
    }
    return id;
  }

  protected abstract Object getItemIdObject(int position);

  @Override public T getItem(int position) {
    return items.get(position);
  }

  @Override public int getCount() {
    return items.size();
  }

  protected View inflate(int layout, ViewGroup parent) {
    return LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
  }

}
