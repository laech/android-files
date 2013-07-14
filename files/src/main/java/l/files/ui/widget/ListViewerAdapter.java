package l.files.ui.widget;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class ListViewerAdapter extends ViewerAdapter {

  protected List<Object> items = newArrayList();

  @Override public int getCount() {
    return items.size();
  }

  @Override public Object getItem(int position) {
    return items.get(position);
  }

  @Override public long getItemId(int position) {
    return position;
  }

}
