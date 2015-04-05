package l.files.ui.browser;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Function;

import java.text.DateFormat;
import java.util.Date;

import l.files.R;
import l.files.fs.ResourceStatus;
import l.files.fs.local.LocalResourceStatus;
import l.files.ui.StableAdapter;

import static android.graphics.Typeface.BOLD;
import static android.graphics.Typeface.SANS_SERIF;
import static android.text.format.DateFormat.getDateFormat;
import static android.text.format.DateFormat.getTimeFormat;
import static android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
import static android.text.format.DateUtils.FORMAT_NO_YEAR;
import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.DateUtils.formatDateTime;
import static android.text.format.Formatter.formatShortFileSize;
import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.util.TypedValue.applyDimension;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static l.files.ui.FilesApp.getBitmapCache;
import static l.files.ui.IconFonts.getBackgroundResourceForFileMediaType;
import static l.files.ui.IconFonts.getDefaultBackgroundResource;
import static l.files.ui.IconFonts.getDefaultFileIcon;
import static l.files.ui.IconFonts.getDirectoryIcon;
import static l.files.ui.IconFonts.getIconForFileMediaType;

final class FilesAdapter extends StableAdapter<FileListItem> {

  // TODO decorator for symlink and others

  private final Function<ResourceStatus, CharSequence> dateFormatter;
  private final ImageDecorator imageDecorator;

  FilesAdapter(Context context, int maxThumbnailWidth, int maxThumbnailHeight) {
    this.dateFormatter = newDateFormatter(context);
    this.imageDecorator = new ImageDecorator(
        getBitmapCache(context), maxThumbnailWidth, maxThumbnailHeight);
  }

  @Override public int getViewTypeCount() {
    return 2;
  }

  @Override public int getItemViewType(int position) {
    return getItem(position).isFile() ? 1 : 0;
  }

  @Override public boolean isEnabled(int position) {
    return getItem(position).isFile();
  }

  @Override public boolean areAllItemsEnabled() {
    return false;
  }

  @Override public View getView(int position, View view, ViewGroup parent) {
    FileListItem item = getItem(position);
    if (item.isHeader()) {
      return getHeaderView(item, view, parent);
    } else {
      return getFileView((FileListItem.File) item, view, parent);
    }
  }

  private View getFileView(FileListItem.File file, View view, ViewGroup parent) {
    final FileViewHolder holder;
    if (view == null) {
      view = inflate(R.layout.files_item, parent);
      holder = new FileViewHolder(view);
      view.setTag(holder);
    } else {
      holder = (FileViewHolder) view.getTag();
    }

    holder.setTitle(file);
    holder.setIcon(file);
    holder.setSymlink(file);
    holder.setDate(file);
    holder.setSize(file);
    holder.setPreview(file);

    return view;
  }

  private View getHeaderView(Object item, View view, ViewGroup parent) {
    final HeaderViewHolder holder;
    if (view == null) {
      view = inflate(R.layout.files_item_header, parent);
      holder = new HeaderViewHolder(view);
      view.setTag(holder);
    } else {
      holder = (HeaderViewHolder) view.getTag();
    }
    holder.title.setText(item.toString());
    return view;
  }

  private static Function<ResourceStatus, CharSequence> newDateFormatter(final Context context) {
    final DateFormat dateFormat = getDateFormat(context);
    final DateFormat timeFormat = getTimeFormat(context);
    final Date date = new Date();
    final Time t1 = new Time();
    final Time t2 = new Time();
    final int flags = FORMAT_SHOW_DATE | FORMAT_ABBREV_MONTH | FORMAT_NO_YEAR;
    return new Function<ResourceStatus, CharSequence>() {
      @Override public CharSequence apply(ResourceStatus file) {
        long time = file.getLastModifiedTime();
        if (time == 0) {
          return context.getString(R.string.__);
        }
        date.setTime(time);
        t1.setToNow();
        t2.set(time);
        if (t1.year == t2.year) {
          if (t1.yearDay == t2.yearDay) {
            return timeFormat.format(date);
          } else {
            return formatDateTime(context, time, flags);
          }
        }
        return dateFormat.format(date);
      }
    };
  }

  static FilesAdapter get(Context context) {
    Resources res = context.getResources();
    DisplayMetrics metrics = res.getDisplayMetrics();

    int width = metrics.widthPixels
        - (int) (applyDimension(COMPLEX_UNIT_DIP, 90, metrics) + 0.5f);

    int height = (int) (metrics.heightPixels * 0.6f);

    return new FilesAdapter(context, width, height);
  }

  @Override protected Object getItemIdObject(int position) {
    Object item = getItem(position);
    if (item instanceof ResourceStatus) {
      return ((ResourceStatus) item).getPath();
    }
    return item;
  }

  private class FileViewHolder {
    final TextView icon;
    final TextView title;
    final TextView date;
    final TextView size;
    final TextView symlink;
    final ImageView preview;

    FileViewHolder(View root) {
      icon = (TextView) root.findViewById(R.id.icon);
      title = (TextView) root.findViewById(R.id.title);
      date = (TextView) root.findViewById(R.id.date);
      size = (TextView) root.findViewById(R.id.size);
      symlink = (TextView) root.findViewById(R.id.symlink);
      preview = (ImageView) root.findViewById(R.id.preview);
    }

    void setTitle(FileListItem.File file) {
      title.setText(file.getPath().getName());
      title.setEnabled(file.getStat() != null && file.getStat().isReadable());
    }

    void setIcon(FileListItem.File file) {
      Context context = icon.getContext();
      AssetManager assets = context.getAssets();
      icon.setEnabled(file.getTargetStat() != null && file.getTargetStat().isReadable());
      if (file.getTargetStat() != null && setLocalIcon(icon, file.getTargetStat())) {
        icon.setTypeface(SANS_SERIF, BOLD);
      } else {
        icon.setText(R.string.ic_font_char);
        icon.setTypeface(getIcon(file, assets));
      }
      icon.setBackgroundResource(getIconBackgroundResource(file));
    }

    boolean setLocalIcon(TextView icon, ResourceStatus status) {
      if (status instanceof LocalResourceStatus) {
        // Sets single character type consistent with ls command
        LocalResourceStatus local = (LocalResourceStatus) status;
        if (local.isBlockDevice()) {
          icon.setText("B");
        } else if (local.isCharacterDevice()) {
          icon.setText("C");
        } else if (local.isSocket()) {
          icon.setText("S");
        } else if (local.isFifo()) {
          icon.setText("P");
        } else {
          return false;
        }
        return true;
      }
      return false;
    }

    private Typeface getIcon(FileListItem.File file, AssetManager assets) {
      ResourceStatus stat = file.getStat();
      if (stat == null) {
        return getDefaultFileIcon(assets);
      }
      if (stat.isDirectory()
          || file.getTargetStat().isDirectory()) {
        return getDirectoryIcon(assets, file.getPath());
      } else {
        return getIconForFileMediaType(assets, stat.getBasicMediaType());
      }
    }

    private int getIconBackgroundResource(FileListItem.File file) {
      ResourceStatus stat = file.getStat();
      if (stat == null
          || stat.isDirectory()
          || file.getTargetStat().isDirectory()) {
        return getDefaultBackgroundResource();
      } else {
        return getBackgroundResourceForFileMediaType(stat.getBasicMediaType());
      }
    }

    void setDate(FileListItem.File file) {
      ResourceStatus stat = file.getStat();
      if (stat == null) {
        date.setText("");
        date.setVisibility(GONE);
      } else {
        date.setEnabled(stat.isReadable());
        date.setText(dateFormatter.apply(stat));
      }
    }

    void setSize(FileListItem.File file) {
      ResourceStatus stat = file.getStat();
      if (stat == null) {
        size.setText("");
        size.setVisibility(GONE);
      } else {
        size.setEnabled(stat.isReadable());
        size.setText(stat.isDirectory() ? "" : formatShortFileSize(size.getContext(), stat.getSize()));
        size.setVisibility(stat.isRegularFile() ? VISIBLE : GONE);
      }
    }

    void setPreview(FileListItem.File file) {
      if (file.getStat() == null) {
        preview.setImageDrawable(null);
        preview.setVisibility(GONE);
      } else {
        imageDecorator.decorate(preview, file.getStat());
      }
    }

    void setSymlink(FileListItem.File file) {
      ResourceStatus stat = file.getStat();
      if (stat == null || !stat.isSymbolicLink()) {
        symlink.setVisibility(GONE);
      } else {
        symlink.setEnabled(stat.isReadable());
        symlink.setVisibility(VISIBLE);
      }
    }

  }

  private class HeaderViewHolder {
    final TextView title;

    HeaderViewHolder(View root) {
      title = (TextView) root.findViewById(android.R.id.title);
    }
  }

}
