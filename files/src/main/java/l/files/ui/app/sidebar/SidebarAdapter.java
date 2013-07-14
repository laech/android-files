package l.files.ui.app.sidebar;

import android.graphics.drawable.Drawable;
import com.google.common.base.Function;
import l.files.R;
import l.files.ui.widget.ObjectAdapter;
import l.files.ui.widget.Viewers;

import java.io.File;

import static com.google.common.base.Functions.toStringFunction;

final class SidebarAdapter extends ObjectAdapter {

  @SuppressWarnings("unchecked") SidebarAdapter(
      Function<? super File, ? extends String> labels,
      Function<? super File, ? extends Drawable> drawables) {

    addViewer(Object.class, Viewers.compose(
        Viewers.layout(R.layout.sidebar_item_header, Object.class),
        Viewers.text(android.R.id.title, toStringFunction())
    ));

    addViewer(File.class, Viewers.compose(
        Viewers.layout(R.layout.sidebar_item, File.class),
        Viewers.text(android.R.id.title, labels),
        Viewers.draw(android.R.id.title, drawables)
    ));

  }

  @Override public boolean isEnabled(int position) {
    return getItem(position) instanceof File;
  }

}
