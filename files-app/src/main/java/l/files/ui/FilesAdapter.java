package l.files.ui;

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
import com.google.common.base.Objects;
import com.google.common.base.Supplier;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import l.files.R;
import l.files.fs.FileStatus;
import l.files.fs.Path;

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
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.ui.FilesApp.getBitmapCache;

final class FilesAdapter extends StableFilesAdapter<FileStatus> implements Supplier<Categorizer> {

  private Categorizer categorizer;
  private final Function<FileStatus, CharSequence> dateFormatter;
  private final ImageDecorator imageDecorator;

  FilesAdapter(Context context, int maxThumbnailWidth, int maxThumbnailHeight) {
    this.dateFormatter = newDateFormatter(context);
    this.imageDecorator = new ImageDecorator(
        getBitmapCache(context), maxThumbnailWidth, maxThumbnailHeight);
  }

  @Override public View getView(int position, View view, ViewGroup parent) {
    final ViewHolder holder;
    if (view == null) {
      view = inflate(R.layout.files_item, parent);
      holder = new ViewHolder(view);
      view.setTag(holder);
    } else {
      holder = (ViewHolder) view.getTag();
    }

    Context context = view.getContext();
    AssetManager assets = context.getAssets();
    Resources res = view.getResources();
    FileStatus file = getItem(position);

    holder.title.setEnabled(file.isReadable());
    holder.title.setText(file.name());

    holder.icon.setEnabled(file.isReadable());
    holder.icon.setTypeface(getIcon(file, assets));

    holder.date.setEnabled(file.isReadable());
    holder.date.setText(dateFormatter.apply(file));

    holder.size.setEnabled(file.isReadable());
    holder.size.setText(file.isDirectory() ? "" : formatShortFileSize(context, file.size()));
    holder.size.setVisibility(file.isRegularFile() ? VISIBLE : GONE);

    holder.header.setText(categorizer.get(res, file));
    holder.headerContainer.setVisibility(showHeader(position, res, file) ? VISIBLE : GONE);

    imageDecorator.decorate(holder.preview, file);

    return view;
  }

  private Typeface getIcon(FileStatus file, AssetManager assets) {
    if (file.isDirectory()) {
      return IconFonts.forDirectoryLocation(assets, file.path());
    } else {
      return IconFonts.forFileMediaType(assets, file.basicMediaType());
    }
  }

  private static Function<FileStatus, CharSequence> newDateFormatter(final Context context) {
    final DateFormat dateFormat = getDateFormat(context);
    final DateFormat timeFormat = getTimeFormat(context);
    final Date date = new Date();
    final Time t1 = new Time();
    final Time t2 = new Time();
    final int flags = FORMAT_SHOW_DATE | FORMAT_ABBREV_MONTH | FORMAT_NO_YEAR;
    return new Function<FileStatus, CharSequence>() {
      @Override public CharSequence apply(FileStatus file) {
        long time = file.lastModifiedTime();
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

  private boolean showHeader(int position, Resources res, FileStatus file) {
    Object current = categorizer.get(res, file);
    if (position == 0) {
      return current != null;
    }
    Object previous = categorizer.get(res, getItem(position - 1));
    return !Objects.equal(current, previous);
  }

  static FilesAdapter get(Context context) {
    Resources res = context.getResources();
    DisplayMetrics metrics = res.getDisplayMetrics();

    int width = metrics.widthPixels
        - res.getDimensionPixelSize(R.dimen.files_list_icon_width)
        - (int) (applyDimension(COMPLEX_UNIT_DIP, 16, metrics) + 0.5f);

    int height = (int) (metrics.heightPixels * 0.6f);

    return new FilesAdapter(context, width, height);
  }

  public void setItems(List<FileStatus> items, Categorizer categorizer) {
    this.categorizer = checkNotNull(categorizer);
    super.setItems(items);
  }

  @Override public Categorizer get() {
    return categorizer;
  }

  @Override protected Path getPath(int position) {
    return getItem(position).path();
  }

  private static class ViewHolder {
    final TextView title;
    final TextView icon;
    final TextView date;
    final TextView size;
    final ImageView preview;
    final TextView header;
    final View headerContainer;

    ViewHolder(View root) {
      title = (TextView) root.findViewById(R.id.title);
      icon = (TextView) root.findViewById(R.id.icon);
      date = (TextView) root.findViewById(R.id.date);
      size = (TextView) root.findViewById(R.id.size);
      preview = (ImageView) root.findViewById(R.id.preview);
      header = (TextView) root.findViewById(R.id.header_title);
      headerContainer = root.findViewById(R.id.header_container);
    }
  }

}
