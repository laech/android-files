package l.files.app;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.joda.time.DateMidnight;
import org.joda.time.MutableDateTime;

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

import l.files.R;
import l.files.common.graphics.drawable.SizedColorDrawable;

import static android.graphics.Color.TRANSPARENT;
import static android.text.format.DateUtils.isToday;
import static android.text.format.Formatter.formatShortFileSize;
import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.util.TypedValue.applyDimension;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.collect.Maps.newHashMap;
import static com.squareup.picasso.Picasso.LoadedFrom;
import static l.files.provider.FileCursors.getFileId;
import static l.files.provider.FileCursors.getFileName;
import static l.files.provider.FileCursors.getLastModified;
import static l.files.provider.FileCursors.getMediaType;
import static l.files.provider.FileCursors.getSize;
import static l.files.provider.FileCursors.isDirectory;
import static l.files.provider.FileCursors.isReadable;
import static l.files.provider.FilesContract.FileInfo.SORT_BY_LAST_MODIFIED;
import static l.files.provider.FilesContract.FileInfo.SORT_BY_NAME;
import static org.joda.time.DateTimeConstants.MILLIS_PER_DAY;

final class FilesAdapter extends StableFilesAdapter {

  private final SparseArray<Info> infos = new SparseArray<>();

  private final int thumbnailWidth;
  private final int thumbnailHeight;

  private DateFormat dateFormat;
  private DateFormat timeFormat;
  private Grouper grouper;

  FilesAdapter(int thumbnailWidth, int thumbnailHeight) {
    this.thumbnailWidth = thumbnailWidth;
    this.thumbnailHeight = thumbnailHeight;
  }

  static FilesAdapter get(Context context) {
    Resources res = context.getResources();
    DisplayMetrics metrics = res.getDisplayMetrics();

    TypedValue value = new TypedValue();
    context.getTheme().resolveAttribute(
        android.R.attr.listPreferredItemPaddingRight, value, true);

    int width = metrics.widthPixels
        - res.getDimensionPixelSize(R.dimen.files_list_icon_width)
        - res.getDimensionPixelSize(R.dimen.files_list_padding_side) * 2
        - (int) (value.getDimension(metrics) + 0.5f)
        - (int) (applyDimension(COMPLEX_UNIT_DIP, 2, metrics) + 0.5f);

    int height = (int) (metrics.heightPixels * 0.6f);

    return new FilesAdapter(width, height);
  }

  @Override public void setCursor(Cursor cursor) {
    setCursor(cursor, null);
  }

  public void setCursor(Cursor cursor, String sortOrder) {
    if (cursor != null) {
      setGrouper(sortOrder);
    }
    infos.clear();
    super.setCursor(cursor);
  }

  private void setGrouper(String sortOrder) {
    switch (nullToEmpty(sortOrder)) {
      case SORT_BY_LAST_MODIFIED:
        grouper = new DateGrouper();
        break;
      case SORT_BY_NAME:
        grouper = new NameGrouper();
        break;
      default:
        grouper = Grouper.NULL;
        break;
    }
  }

  @Override public View getView(int position, View view, ViewGroup parent) {
    if (view == null) {
      view = inflate(R.layout.files_item, parent);
      view.setTag(new ViewHolder(view));
    }

    Context context = parent.getContext();
    Info info = getInfo(context, position);
    ViewHolder holder = (ViewHolder) view.getTag();
    holder.setTitle(info.title);
    holder.setEnabled(info.readable);
    holder.setIcon(info.icon);
    holder.setDate(info.date);
    holder.setSize(info.size);
    holder.setDirectory(info.directory);
    holder.setImage(info, thumbnailWidth, thumbnailHeight);
    holder.setGroup(info.group, position == 0 ? null
        : getInfo(context, position - 1).group);

    return view;
  }

  private Info getInfo(Context context, int position) {
    Info info = infos.get(position);
    if (info == null) {
      info = new Info(context, position);
      infos.put(position, info);
    }
    return info;
  }

  private final Map<String, Drawable> placeholders = newHashMap();

  private static final LruCache<String, Object> errors = new LruCache<>(1000);

  private final class ViewHolder implements Target {

    final View root;
    final TextView title;
    final TextView icon;
    final TextView date;
    final TextView size;
    final ImageView preview;
    final TextView header;
    final View headerContainer;
    private String uri;

    ViewHolder(View root) {
      this.root = root;
      this.icon = (TextView) root.findViewById(R.id.icon);
      this.title = (TextView) root.findViewById(R.id.title);
      this.preview = (ImageView) root.findViewById(R.id.preview);
      this.date = (TextView) root.findViewById(R.id.date);
      this.size = (TextView) root.findViewById(R.id.size);
      this.header = (TextView) root.findViewById(R.id.header_title);
      this.headerContainer = root.findViewById(R.id.header_container);
    }

    void setEnabled(boolean enabled) {
      root.setEnabled(enabled);
      icon.setEnabled(enabled);
      title.setEnabled(enabled);
      date.setEnabled(enabled);
      size.setEnabled(enabled);
    }

    void setTitle(CharSequence text) {
      title.setText(text);
    }

    void setDate(CharSequence text) {
      date.setText(text);
    }

    void setSize(CharSequence text) {
      size.setText(text);
    }

    void setIcon(Typeface typeface) {
      icon.setTypeface(typeface);
    }

    void setGroup(String group, String prevGroup) {
      if (group == null || group.equals(prevGroup)) {
        headerContainer.setVisibility(GONE);
      } else {
        header.setText(group);
        headerContainer.setVisibility(VISIBLE);
      }
    }

    void setImage(Info info, int width, int height) {
      uri = info.uri;
      preview.setImageBitmap(null);
      preview.setVisibility(GONE);
      if (!info.directory && info.readable && errors.get(uri) == null) {
        Drawable placeholder = placeholders.get(uri);
        Picasso.with(preview.getContext())
            .load(uri)
            .placeholder(placeholder)
            .resize(width, height)
            .centerInside()
            .into(this);
      }
    }

    void setDirectory(boolean directory) {
      size.setVisibility(directory ? GONE : VISIBLE);
    }

    @Override public void onBitmapLoaded(Bitmap bitmap, LoadedFrom from) {
      if (!placeholders.containsKey(uri)) {
        placeholders.put(uri, new SizedColorDrawable(
            TRANSPARENT,
            bitmap.getWidth(),
            bitmap.getHeight()));
      }
      preview.setImageBitmap(bitmap);
      preview.setVisibility(VISIBLE);
      // TODO animate
    }

    @Override public void onBitmapFailed(Drawable errorDrawable) {
      preview.setVisibility(GONE);
      errors.put(uri, uri);
    }

    @Override public void onPrepareLoad(Drawable placeHolderDrawable) {
      preview.setImageDrawable(placeHolderDrawable);
      if (placeHolderDrawable != null) {
        preview.setVisibility(VISIBLE);
      }
    }
  }

  private final class Info {
    final String uri;
    final CharSequence title;
    final CharSequence date;
    final CharSequence size;
    final Typeface icon;
    final boolean readable;
    final boolean directory;
    final String group;

    Info(Context context, int position) {
      Cursor cursor = getItem(position);
      uri = getFileId(cursor); // TODO fix
      title = getFileName(cursor);
      readable = isReadable(cursor);
      directory = isDirectory(cursor);
      date = formatLastModified(context, cursor);
      if (directory) {
        icon = getDirectoryIcon(context, cursor);
        size = "";
      } else {
        icon = getFileIcon(context, cursor);
        size = formatShortFileSize(context, getSize(cursor));
      }
      group = grouper.getGroup(context.getResources(), cursor);
    }

    private Typeface getFileIcon(Context context, Cursor cursor) {
      String media = getMediaType(cursor);
      return IconFonts.forFileMediaType(context.getAssets(), media);
    }

    private Typeface getDirectoryIcon(Context context, Cursor cursor) {
      String id = getFileId(cursor);
      return IconFonts.forDirectoryId(context.getAssets(), id);
    }

    private String formatLastModified(Context context, Cursor cursor) {
      if (dateFormat == null) {
        dateFormat = android.text.format.DateFormat.getDateFormat(context);
      }
      if (timeFormat == null) {
        timeFormat = android.text.format.DateFormat.getTimeFormat(context);
      }
      long modified = getLastModified(cursor);
      return (isToday(modified) ? timeFormat : dateFormat)
          .format(new Date(modified));
    }
  }

  private static class Grouper {
    static final Grouper NULL = new Grouper();

    String getGroup(Resources res, Cursor cursor) {
      return null;
    }
  }

  private static final class NameGrouper extends Grouper {
    @Override String getGroup(Resources res, Cursor cursor) {
      return res.getString(R.string.name);
    }
  }

  private static final class DateGrouper extends Grouper {
    private final MutableDateTime timestamp = new MutableDateTime();
    private final long startOfToday = DateMidnight.now().getMillis();
    private final long startOfTomorrow = startOfToday + MILLIS_PER_DAY;
    private final long startOfYesterday = startOfToday - MILLIS_PER_DAY;
    private final long startOf7Days = startOfToday - MILLIS_PER_DAY * 7L;
    private final long startOf30Days = startOfToday - MILLIS_PER_DAY * 30L;

    @Override public String getGroup(Resources res, Cursor cursor) {

      long modified = getLastModified(cursor);
      if (modified >= startOfTomorrow)
        return res.getString(R.string.unknown);
      if (modified >= startOfToday)
        return res.getString(R.string.today);
      if (modified >= startOfYesterday)
        return res.getString(R.string.yesterday);
      if (modified >= startOf7Days)
        return res.getString(R.string.previous_7_days);
      if (modified >= startOf30Days)
        return res.getString(R.string.previous_30_days);

      timestamp.setMillis(startOfToday);
      int currentYear = timestamp.getYear();

      timestamp.setMillis(modified);
      int thatYear = timestamp.getYear();

      if (currentYear != thatYear) {
        return String.valueOf(thatYear);
      }

      return timestamp.monthOfYear().getAsText();
    }
  }
}
