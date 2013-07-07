package l.files.ui.app.files;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.common.base.Function;
import l.files.R;
import l.files.ui.widget.AnimatedAdapter;
import za.co.immedia.pinnedheaderlistview.PinnedHeaderListView.PinnedSectionedHeaderAdapter;

import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;

public final class FilesAdapter
    extends AnimatedAdapter<Object> implements PinnedSectionedHeaderAdapter {

  private final Function<File, Drawable> drawables;
  private final Function<Long, String> dateFormatter;
  private final Function<Long, String> sizeFormatter;

  public FilesAdapter(
      Function<File, Drawable> drawables,
      Function<Long, String> dateFormatter,
      Function<Long, String> sizeFormatter) {
    this.drawables = checkNotNull(drawables, "drawables");
    this.dateFormatter = checkNotNull(dateFormatter, "dateFormatter");
    this.sizeFormatter = checkNotNull(sizeFormatter, "sizeFormatter");
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
    bindHeaderView(getItem(section), convertView);
    return convertView;
  }

  private View emptySectionHeaderView(View convertView, ViewGroup parent) {
    if (convertView == null) {
      convertView = inflate(android.R.layout.simple_list_item_1, parent);
    }
    return convertView;
  }

  @Override public int getSectionHeaderViewType(int section) {
    return 0;
  }

  @Override public int getViewTypeCount() {
    return 2;
  }

  @Override public int getItemViewType(int position) {
    return getItem(position) instanceof File ? 0 : 1;
  }

  @Override public boolean isEnabled(int position) {
    return getItem(position) instanceof File;
  }

  @Override protected View newView(Object item, ViewGroup parent) {
    return (item instanceof File)
        ? newFileView(parent)
        : newHeaderView(parent);
  }

  @Override protected void bindView(Object item, View view) {
    if (item instanceof File) {
      bindFileView((File) item, view);
    } else {
      bindHeaderView(item, view);
    }
  }

  private View newFileView(ViewGroup parent) {
    View view = inflate(R.layout.files_item, parent);
    ViewHolder holder = new ViewHolder();
    holder.name = (TextView) view.findViewById(R.id.name);
    holder.info = (TextView) view.findViewById(R.id.info);
    view.setTag(holder);
    return view;
  }

  private void bindFileView(File file, View view) {
    boolean canRead = file.canRead();
    view.setEnabled(canRead);
    ViewHolder holder = (ViewHolder) view.getTag();
    showFilename(file, holder, canRead);
    showFileInfo(file, holder, canRead);
  }

  private View newHeaderView(ViewGroup parent) {
    return inflate(R.layout.files_item_header, parent);
  }

  private void bindHeaderView(Object header, View view) {
    ((TextView) view.findViewById(R.id.title)).setText(header.toString());
  }

  void showFilename(File f, ViewHolder holder, boolean enable) {
    holder.name.setEnabled(enable);
    holder.name.setText(f.getName());
    holder.name.setCompoundDrawablesWithIntrinsicBounds(
        drawables.apply(f), null, null, null);
  }

  void showFileInfo(File file, ViewHolder holder, boolean enable) {
    if (holder.info == null) return;

    holder.info.setEnabled(enable);
    if (file.isFile()) {
      holder.info.setText(holder.info.getResources().getString(
          R.string.file_size_updated,
          sizeFormatter.apply(file.length()),
          dateFormatter.apply(file.lastModified())));
    } else {
      holder.info.setText(dateFormatter.apply(file.lastModified()));
    }
  }

  static class ViewHolder {
    TextView name;
    TextView info;
  }
}
