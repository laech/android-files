package l.files.ui.app.files;

import static android.text.format.Formatter.formatShortFileSize;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;

import l.files.R;
import l.files.media.ImageMap;
import l.files.ui.widget.AnimatedAdapter;
import l.files.util.DateTimeFormat;
import l.files.util.FileSystem;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public final class FilesAdapter extends AnimatedAdapter<File> {

  static class ViewHolder {
    TextView name;
    TextView info;
  }

  private final FileSystem files;
  private final ImageMap images;
  private final DateTimeFormat format;

  public FilesAdapter(
      ListView parent,
      FileSystem fileSystem,
      ImageMap images,
      DateTimeFormat format) {
    super(parent);
    this.files = checkNotNull(fileSystem, "files");
    this.images = checkNotNull(images, "images");
    this.format = checkNotNull(format, "format");
  }

  @Override protected View newView(File file, ViewGroup parent) {
    View view = inflate(R.layout.files_item, parent);
    ViewHolder holder = new ViewHolder();
    holder.name = (TextView) view.findViewById(R.id.name);
    holder.info = (TextView) view.findViewById(R.id.info);
    view.setTag(holder);
    return view;
  }

  @Override protected void bindView(File file, View view) {
    boolean hasPermissionToRead = files.hasPermissionToRead(file);
    view.setEnabled(hasPermissionToRead);
    ViewHolder holder = (ViewHolder) view.getTag();

    showFilename(file, holder);
    showFileInfo(file, holder);
  }

  void showFilename(File f, ViewHolder holder) {
    holder.name.setEnabled(files.hasPermissionToRead(f));
    holder.name.setText(f.getName());
    holder.name.setCompoundDrawablesWithIntrinsicBounds(images.get(f), 0, 0, 0);
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
}
