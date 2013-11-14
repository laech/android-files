package l.files.common.widget;

import android.widget.ListView;

import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static l.files.common.widget.Animations.animatePreDataSetChange;

public abstract class AnimatedAdapter extends ListViewerAdapter {

  public void replace(ListView list, Collection<?> items, boolean animate) {
    if (animate) animatePreDataSetChange(list);
    this.items = newArrayList(items);
    notifyDataSetChanged();
  }
}