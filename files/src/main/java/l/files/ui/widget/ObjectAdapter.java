package l.files.ui.widget;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class ObjectAdapter extends ViewerAdapter {

  private final List<Object> items = newArrayList();

  @Override public int getCount() {
    return items.size();
  }

  @Override public Object getItem(int position) {
    return items.get(position);
  }

  @Override public long getItemId(int position) {
    return position;
  }

  public void clear() {
    items.clear();
  }

  public void add(Object item) {
    items.add(item);
  }

  public void addAll(Collection<?> coll) {
    items.addAll(coll);
  }

}
