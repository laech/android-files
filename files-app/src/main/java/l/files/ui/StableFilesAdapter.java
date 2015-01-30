package l.files.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;
import java.util.Map;

import l.files.fs.Path;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.emptyList;

abstract class StableFilesAdapter<T> extends BaseAdapter {

  private static final Map<Path, Long> ids = newHashMap();

  private List<T> items = emptyList();

  protected void setItems(List<T> items) {
    this.items = checkNotNull(items);
    notifyDataSetChanged();
  }

  @Override public boolean hasStableIds() {
    return true;
  }

  @Override public long getItemId(int position) {
    Path path = getPath(position);
    Long id = ids.get(path);
    if (id == null) {
      id = ids.size() + 1L;
      ids.put(path, id);
    }
    return id;
  }

  protected abstract Path getPath(int position);

  @Override public T getItem(int position) {
    return items.get(position);
  }

  @Override public int getCount() {
    return items.size();
  }

  protected View inflate(int layout, ViewGroup parent) {
    return LayoutInflater.from(parent.getContext())
        .inflate(layout, parent, false);
  }
}
