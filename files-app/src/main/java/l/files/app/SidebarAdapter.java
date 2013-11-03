package l.files.app;

import static com.google.common.base.Functions.toStringFunction;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.sort;
import static l.files.app.UserDirs.DIR_HOME;
import static l.files.app.UserDirs.DIR_ROOT;
import static l.files.app.format.Formats.iconFont;
import static l.files.app.format.Formats.label;
import static l.files.common.widget.Decorators.*;
import static l.files.common.widget.Viewers.decorate;

import android.content.res.Resources;
import android.graphics.Typeface;
import com.google.common.base.Function;
import java.io.File;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import l.files.R;
import l.files.common.widget.ObjectAdapter;

final class SidebarAdapter extends ObjectAdapter {

  private final Function<File, CharSequence> labels;

  // TODO animate

  @SuppressWarnings("unchecked") SidebarAdapter(
      Function<? super File, ? extends CharSequence> labels,
      Function<? super File, ? extends Typeface> icons) {

    this.labels = (Function<File, CharSequence>) checkNotNull(labels, "labels");
    checkNotNull(icons, "icons");

    addViewer(Object.class, decorate(R.layout.sidebar_item_header,
        on(android.R.id.title, text(toStringFunction()))
    ));

    addViewer(File.class, decorate(R.layout.sidebar_item,
        on(android.R.id.title, text(labels)),
        on(android.R.id.icon, font(icons))
    ));
  }

  static SidebarAdapter get(Resources res) {
    return new SidebarAdapter(label(res), iconFont(res.getAssets()));
  }

  @Override public boolean isEnabled(int position) {
    return getItem(position) instanceof File;
  }

  void set(Set<File> bookmarks, Resources res) {

    // TODO remove root, rename home to something more appropriate, name of user?

    clear();
    add(res.getString(R.string.bookmarks));
    addAll(arrange(bookmarks));
    add(res.getString(R.string.device));
    add(DIR_HOME);
    add(DIR_ROOT);
    notifyDataSetChanged();
  }

  private Collection<File> arrange(Set<File> bookmarks) {
    List<File> dirs = newArrayList(bookmarks);
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
