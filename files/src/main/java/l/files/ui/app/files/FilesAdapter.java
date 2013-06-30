package l.files.ui.app.files;

import static android.text.format.Formatter.formatShortFileSize;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;

import l.files.R;
import l.files.ui.widget.AnimatedAdapter;
import l.files.util.DateTimeFormat;
import l.files.util.FileSystem;
import za.co.immedia.pinnedheaderlistview.PinnedHeaderListView.PinnedSectionedHeaderAdapter;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.base.Function;

public final class FilesAdapter
    extends AnimatedAdapter<Object> implements PinnedSectionedHeaderAdapter {

  private final FileSystem files;
  private final Function<File, Drawable> drawables;
  private final DateTimeFormat format;

  public FilesAdapter(
      ListView parent,
      FileSystem fileSystem,
      Function<File, Drawable> drawables,
      DateTimeFormat format) {
    super(parent);
    this.files = checkNotNull(fileSystem, "files");
    this.drawables = checkNotNull(drawables, "drawables");
    this.format = checkNotNull(format, "format");
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

  @Override
  public View getSectionHeaderView(int section, View convertView, ViewGroup parent) {
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
    if (item instanceof File) bindFileView((File) item, view);
    else bindHeaderView(item, view);
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
    boolean hasPermissionToRead = files.hasPermissionToRead(file);
    view.setEnabled(hasPermissionToRead);
    ViewHolder holder = (ViewHolder) view.getTag();

    showFilename(file, holder);
    showFileInfo(file, holder);
  }

  private View newHeaderView(ViewGroup parent) {
    return inflate(R.layout.files_item_header, parent);
  }

  private void bindHeaderView(Object header, View view) {
    ((TextView) view.findViewById(R.id.title)).setText(header.toString());
  }

  void showFilename(File f, ViewHolder holder) {
    holder.name.setEnabled(files.hasPermissionToRead(f));
    holder.name.setText(f.getName());
    holder.name.setCompoundDrawablesWithIntrinsicBounds(drawables.apply(f), null, null, null);
  }

  void showFileInfo(File file, ViewHolder holder) {
    if (holder.info == null) return;

    holder.info.setEnabled(files.hasPermissionToRead(file));
    Context context = holder.info.getContext();
    String updated = format.format(file.lastModified());
    if (file.isFile()) {
      String size = formatShortFileSize(context, file.length());
      int template = R.string.file_size_updated;
      holder.info.setText(context.getString(template, size, updated));
    } else {
      holder.info.setText(updated);
    }
  }

  static class ViewHolder {
    TextView name;
    TextView info;
  }
}
