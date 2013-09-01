package l.files.app;

import static com.google.common.base.Functions.toStringFunction;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.app.format.Formats.*;
import static l.files.common.io.Files.canRead;
import static l.files.common.io.Files.name;
import static l.files.common.widget.Decorators.*;
import static l.files.common.widget.Viewers.decorate;

import android.content.Context;
import android.graphics.Typeface;
import com.google.common.base.Function;
import java.io.File;
import l.files.R;
import l.files.common.widget.AnimatedAdapter;

final class FilesAdapter extends AnimatedAdapter {

  /**
   * @param names the function to return the name of the file
   * @param icons the function to return the icon font of the file
   * @param summaries the function to return additional summary of the file
   */
  @SuppressWarnings("unchecked") FilesAdapter(
      Function<? super File, ? extends CharSequence> names,
      Function<? super File, ? extends Typeface> icons,
      Function<? super File, ? extends CharSequence> summaries) {

    checkNotNull(names, "names");
    checkNotNull(icons, "icons");
    checkNotNull(summaries, "summaries");

    addViewer(Object.class, decorate(R.layout.files_item_header,
        on(android.R.id.title, text(toStringFunction()))
    ));

    addViewer(File.class, decorate(R.layout.files_item,
        on(android.R.id.icon, font(icons)),
        on(android.R.id.title, text(names)),
        on(android.R.id.summary, text(summaries)),
        enable(canRead())
    ));
  }

  static FilesAdapter get(Context context) {
    return new FilesAdapter(
        name(),
        iconFont(context.getAssets()),
        summary(context.getResources(),
            date(context),
            size(context)
        )
    );
  }

  @Override public boolean isEnabled(int position) {
    return getItem(position) instanceof File;
  }
}
