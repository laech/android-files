package l.files.ui.app.sidebar;

import android.graphics.drawable.Drawable;
import com.google.common.base.Function;
import l.files.R;
import l.files.ui.widget.ObjectAdapter;

import java.io.File;

import static com.google.common.base.Functions.toStringFunction;
import static l.files.ui.widget.Decorators.draw;
import static l.files.ui.widget.Decorators.text;
import static l.files.ui.widget.Viewers.decorate;

final class SidebarAdapter extends ObjectAdapter {

  // TODO animate

  @SuppressWarnings("unchecked") SidebarAdapter(
      Function<? super File, ? extends String> labels,
      Function<? super File, ? extends Drawable> drawables) {

    addViewer(Object.class, decorate(R.layout.sidebar_item_header,
        text(android.R.id.title, toStringFunction())
    ));

    addViewer(File.class, decorate(R.layout.sidebar_item,
        text(android.R.id.title, labels),
        draw(android.R.id.title, drawables)
    ));

  }

  @Override public boolean isEnabled(int position) {
    return getItem(position) instanceof File;
  }

}
