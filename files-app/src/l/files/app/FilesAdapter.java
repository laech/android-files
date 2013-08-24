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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.google.common.base.Function;
import com.squareup.picasso.Picasso;
import java.io.File;
import l.files.R;
import l.files.common.widget.AnimatedAdapter;

final class FilesAdapter extends AnimatedAdapter {

  /**
   * @param names the function to return the name of the file
   * @param fonts the function to return the icon font of the file
   * @param summaries the function to return additional summary of the file
   */
  FilesAdapter(
      Function<? super File, ? extends CharSequence> names,
      Function<? super File, ? extends Typeface> fonts,
      Function<? super File, ? extends CharSequence> summaries) {

    checkNotNull(names, "names");
    checkNotNull(fonts, "fonts");
    checkNotNull(summaries, "summaries");

    addViewerForHeader();
    addViewerForFile(names, fonts, summaries);
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

  @SuppressWarnings("unchecked")
  private void addViewerForHeader() {
    addViewer(Object.class, decorate(R.layout.files_item_header,
        text(android.R.id.title, toStringFunction())
    ));
  }

  @SuppressWarnings("unchecked")
  private void addViewerForFile(
      Function<? super File, ? extends CharSequence> names,
      Function<? super File, ? extends Typeface> fonts,
      Function<? super File, ? extends CharSequence> summaries) {

    addViewer(File.class, decorate(R.layout.files_item,
        font(android.R.id.icon, fonts),
        text(android.R.id.title, names),
        enable(android.R.id.icon, canRead()),
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
