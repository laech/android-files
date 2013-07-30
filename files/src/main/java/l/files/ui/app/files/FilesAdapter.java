package l.files.ui.app.files;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.google.common.base.Function;
import l.files.R;
import l.files.common.widget.AnimatedAdapter;

import java.io.File;

import static com.google.common.base.Functions.toStringFunction;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.common.io.Files.canRead;
import static l.files.common.io.Files.name;
import static l.files.common.widget.Decorators.*;
import static l.files.common.widget.Viewers.decorate;
import static l.files.io.Files.*;
import static l.files.ui.format.Formats.date;
import static l.files.ui.format.Formats.size;

final class FilesAdapter extends AnimatedAdapter {

  /**
   * @param names the function to return the name of the file
   * @param drawables the function to return the icon of the file
   * @param summaries the function to return additional summary of the file
   */
  FilesAdapter(
      Function<? super File, ? extends CharSequence> names,
      Function<? super File, ? extends Drawable> drawables,
      Function<? super File, ? extends CharSequence> summaries) {

    checkNotNull(names, "names");
    checkNotNull(drawables, "drawables");
    checkNotNull(summaries, "summaries");

    addViewerForHeader();
    addViewerForFile(names, drawables, summaries);
  }

  static FilesAdapter get(Context context) {
    return new FilesAdapter(
        name(),
        drawable(context.getResources()),
        summary(context.getResources(),
            date(context),
            size(context)
        )
    );
  }

  @SuppressWarnings("unchecked")
  private void addViewerForHeader() {
    addViewer(Object.class, decorate(R.layout.files_item_header,
        text(android.R.id.title, toStringFunction())
    ));
  }

  @SuppressWarnings("unchecked")
  private void addViewerForFile(
      Function<? super File, ? extends CharSequence> names,
      Function<? super File, ? extends Drawable> drawables,
      Function<? super File, ? extends CharSequence> summaries) {

    addViewer(File.class, decorate(R.layout.files_item,
        text(android.R.id.title, names),
        draw(android.R.id.title, drawables),
        enable(android.R.id.title, canRead()),
        enable(android.R.id.content, canRead()),
        nullable(android.R.id.summary,
            text(android.R.id.summary, summaries),
            enable(android.R.id.summary, canRead())
        )
    ));
  }

  @Override public boolean isEnabled(int position) {
    return getItem(position) instanceof File;
  }
}
