package l.files.ui.widget;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMapWithExpectedSize;

public abstract class ViewerAdapter extends BaseAdapter {

  private final Map<Class<?>, Viewer<Object>> viewers;
  private final Map<Class<?>, Integer> types;

  public ViewerAdapter() {
    viewers = newHashMapWithExpectedSize(1);
    types = newHashMapWithExpectedSize(1);
  }

  @SuppressWarnings("unchecked")
  public <T> void addViewer(Class<? extends T> c, Viewer<? super T> viewer) {
    viewers.put(c, (Viewer<Object>) viewer);
    types.put(c, types.size());
  }

  @Override public int getItemViewType(int position) {
    Object item = getItem(position);
    return types.get(findClass(item));
  }

  @Override public int getViewTypeCount() {
    return types.size();
  }

  @Override public View getView(int position, View view, ViewGroup parent) {
    Object item = getItem(position);
    Viewer<Object> viewer = viewers.get(findClass(item));
    return viewer.getView(item, view, parent);
  }

  private Class<?> findClass(Object item) {
    for (Class<?> c = item.getClass(); c != null; c = c.getSuperclass()) {
      if (viewers.containsKey(c)) return c;

      for (Class<?> interfaceClass : c.getInterfaces())
        if (viewers.containsKey(interfaceClass)) return interfaceClass;
    }

    throw new IllegalStateException("No viewer found for: " + item);
  }
}
