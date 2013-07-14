package l.files.ui.app.files;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.common.base.Function;
import l.files.R;
import l.files.ui.widget.AnimatedAdapter;
import za.co.immedia.pinnedheaderlistview.PinnedHeaderListView.PinnedSectionedHeaderAdapter;

import java.io.File;

import static com.google.common.base.Functions.toStringFunction;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.R.layout.files_item_header;
import static l.files.io.FilePredicates.canRead;
import static l.files.ui.FileFunctions.*;
import static l.files.ui.format.Formats.dateFormat;
import static l.files.ui.format.Formats.sizeFormat;
import static l.files.ui.widget.Viewers.*;

final class FilesAdapter
    extends AnimatedAdapter implements PinnedSectionedHeaderAdapter {

  static FilesAdapter get(Context context) {
    return new FilesAdapter(
        name(),
        drawable(context.getResources()),
        summary(context.getResources(),
            dateFormat(context),
            sizeFormat(context)
        )
    );
  }

  /**
   * @param names the function to return the name of the file
   * @param drawables the function to return the icon of the file
   * @param summaries the function to return additional summary of the file
   */
  @SuppressWarnings("unchecked") FilesAdapter(
      Function<? super File, ? extends CharSequence> names,
      Function<? super File, ? extends Drawable> drawables,
      Function<? super File, ? extends CharSequence> summaries) {

    checkNotNull(names, "names");
    checkNotNull(drawables, "drawables");
    checkNotNull(summaries, "summaries");

    addViewer(Object.class, compose(
        layout(files_item_header),
        text(android.R.id.title, toStringFunction())
    ));

    addViewer(File.class, compose(
        compose(
            layout(R.layout.files_item),
            enable(android.R.id.content, canRead())
        ),
        compose(
            text(android.R.id.title, names),
            draw(android.R.id.title, drawables),
            enable(android.R.id.title, canRead())
        ),
        nullable(android.R.id.summary, compose(
            text(android.R.id.summary, summaries),
            enable(android.R.id.summary, canRead())
        ))
    ));

  }

  // TODO below

  protected View inflate(int id, ViewGroup parent) {
    return LayoutInflater.from(parent.getContext()).inflate(id, parent, false);
  }

  @Override public boolean isSectionHeader(int position) {
    return !(getItem(position) instanceof File);
  }

  @Override public int getSectionForPosition(int position) {
    while (position >= 0 && getItem(position) instanceof File) {
      position--;
    }
    return position; // This will be -1 if no header
  }

  @Override public View getSectionHeaderView(
      int section, View convertView, ViewGroup parent) {
    if (section == -1) {// See getSectionForPosition
      return emptySectionHeaderView(convertView, parent);
    }

    if (convertView == null) {
      convertView = inflate(R.layout.files_item_header_sticky, parent);
    }
    Object item = getItem(section);
    return getViewer(item).getView(item, convertView, parent);
  }

  private View emptySectionHeaderView(View convertView, ViewGroup parent) {
    if (convertView == null) {
      convertView = inflate(R.layout.files_item_header_none, parent);
    }
    return convertView;
  }

  @Override public int getSectionHeaderViewType(int section) {
    return 0;
  }

  @Override public boolean isEnabled(int position) {
    return getItem(position) instanceof File;
  }

}
