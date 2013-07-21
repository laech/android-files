package l.files.ui.app.sidebar;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import com.google.common.base.Function;
import l.files.R;
import l.files.setting.SetSetting;
import l.files.ui.widget.ObjectAdapter;

import java.io.File;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static com.google.common.base.Functions.toStringFunction;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.sort;
import static l.files.ui.FileFunctions.drawable;
import static l.files.ui.FileFunctions.label;
import static l.files.ui.UserDirs.DIR_HOME;
import static l.files.ui.UserDirs.DIR_ROOT;
import static l.files.ui.widget.Decorators.draw;
import static l.files.ui.widget.Decorators.text;
import static l.files.ui.widget.Viewers.decorate;

final class SidebarAdapter extends ObjectAdapter {

  static SidebarAdapter get(Resources res) {
    return new SidebarAdapter(label(res), drawable(res));
  }

  private final Function<File, CharSequence> labels;

  // TODO animate

  @SuppressWarnings("unchecked") SidebarAdapter(
      Function<? super File, ? extends CharSequence> labels,
      Function<? super File, ? extends Drawable> drawables) {

    this.labels = (Function<File, CharSequence>) checkNotNull(labels, "labels");
    checkNotNull(drawables, "drawables");

    addHeaderViewer();
    addFileViewer(labels, drawables);
  }

  @SuppressWarnings("unchecked") private void addFileViewer(
      Function<? super File, ? extends CharSequence> labels,
      Function<? super File, ? extends Drawable> drawables) {

    addViewer(File.class, decorate(R.layout.sidebar_item,
        text(android.R.id.title, labels),
        draw(android.R.id.title, drawables)
    ));

  }

  @SuppressWarnings("unchecked") private void addHeaderViewer() {
    addViewer(Object.class, decorate(R.layout.sidebar_item_header,
        text(android.R.id.title, toStringFunction())
    ));
  }

  @Override public boolean isEnabled(int position) {
    return getItem(position) instanceof File;
  }

  void set(SetSetting<File> setting, Resources res) {

    // TODO remove root, rename home to something more appropriate, name of user?

    clear();
    add(res.getString(R.string.bookmarks));
    addAll(getBookmarks(setting));
    add(res.getString(R.string.device));
    add(DIR_HOME);
    add(DIR_ROOT);
    notifyDataSetChanged();
  }

  private Collection<File> getBookmarks(SetSetting<File> setting) {
    List<File> dirs = newArrayList(setting.get());
    sort(dirs, new Comparator<File>() {
      @Override public int compare(File a, File b) {
        String x = labels.apply(a).toString();
        String y = labels.apply(b).toString();
        return x.compareToIgnoreCase(y);
      }
    });
    return dirs;
  }
}
