package l.files.ui.browser;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
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
import l.files.ui.StableFilesAdapter;

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
import static l.files.ui.IconFonts.getColorForFileMediaType;
import static l.files.ui.IconFonts.getDefaultColor;
import static l.files.ui.IconFonts.getDefaultFileIcon;
import static l.files.ui.IconFonts.getDirectoryIcon;
import static l.files.ui.IconFonts.getIconForFileMediaType;

final class FilesAdapter extends StableFilesAdapter<FileListItem> {

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
    return getItem(position).getIsFile() ? 1 : 0;
  }

  @Override public boolean isEnabled(int position) {
    return getItem(position).getIsFile();
  }

  @Override public boolean areAllItemsEnabled() {
    return false;
  }

  @Override public View getView(int position, View view, ViewGroup parent) {
    FileListItem item = getItem(position);
    if (item.getIsHeader()) {
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
      title.setEnabled(file.getStat() != null && file.getStat().getIsReadable());
    }

    void setIcon(FileListItem.File file) {
      Context context = icon.getContext();
      AssetManager assets = context.getAssets();
      icon.setEnabled(file.getStat() != null && file.getStat().getIsReadable());
      icon.setTypeface(getIcon(file, assets));
      if (icon.getBackground() instanceof ShapeDrawable) {
        ((ShapeDrawable) icon.getBackground())
            .getPaint().setColor(getIconColor(file, context));
      } else {
        PaintDrawable background = new PaintDrawable(getIconColor(file, context));
        background.setShape(new OvalShape());
        icon.setBackground(background);
      }
    }

    private Typeface getIcon(FileListItem.File file, AssetManager assets) {
      ResourceStatus stat = file.getStat();
      if (stat == null) {
        return getDefaultFileIcon(assets);
      }
      if (stat.getIsDirectory()
          || file.getTargetStat().getIsDirectory()) {
        return getDirectoryIcon(assets, file.getPath());
      } else {
        return getIconForFileMediaType(assets, stat.getBasicMediaType());
      }
    }

    private int getIconColor(FileListItem.File file, Context context) {
      ResourceStatus stat = file.getStat();
      if (stat == null
          || stat.getIsDirectory()
          || file.getTargetStat().getIsDirectory()) {
        return getDefaultColor(context);
      } else {
        return getColorForFileMediaType(context, stat.getBasicMediaType());
      }
    }

    void setDate(FileListItem.File file) {
      ResourceStatus stat = file.getStat();
      if (stat == null) {
        date.setText("");
        date.setVisibility(GONE);
      } else {
        date.setEnabled(stat.getIsReadable());
        date.setText(dateFormatter.apply(stat));
      }
    }

    void setSize(FileListItem.File file) {
      ResourceStatus stat = file.getStat();
      if (stat == null) {
        size.setText("");
        size.setVisibility(GONE);
      } else {
        size.setEnabled(stat.getIsReadable());
        size.setText(stat.getIsDirectory() ? "" : formatShortFileSize(size.getContext(), stat.getSize()));
        size.setVisibility(stat.getIsRegularFile() ? VISIBLE : GONE);
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
      if (file.getStat() == null || !file.getStat().getIsSymbolicLink()) {
        symlink.setVisibility(GONE);
      } else {
        symlink.setTextColor(getIconColor(file, symlink.getContext()));
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
