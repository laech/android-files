package l.files.ui.widget;

import java.util.Collection;

public class ObjectAdapter extends ListViewerAdapter {

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
