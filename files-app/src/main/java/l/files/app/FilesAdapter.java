package l.files.app;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.joda.time.DateMidnight;
import org.joda.time.MutableDateTime;

import java.text.DateFormat;
import java.util.Date;

import l.files.R;
import l.files.app.format.IconFonts;

import static android.text.format.DateUtils.isToday;
import static android.text.format.Formatter.formatShortFileSize;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.google.common.base.Strings.nullToEmpty;
import static com.squareup.picasso.Callback.EmptyCallback;
import static l.files.provider.FilesContract.FileInfo.COLUMN_ID;
import static l.files.provider.FilesContract.FileInfo.COLUMN_LAST_MODIFIED;
import static l.files.provider.FilesContract.FileInfo.COLUMN_MEDIA_TYPE;
import static l.files.provider.FilesContract.FileInfo.COLUMN_NAME;
import static l.files.provider.FilesContract.FileInfo.COLUMN_READABLE;
import static l.files.provider.FilesContract.FileInfo.COLUMN_SIZE;
import static l.files.provider.FilesContract.FileInfo.MEDIA_TYPE_DIR;
import static l.files.provider.FilesContract.FileInfo.SORT_BY_LAST_MODIFIED;
import static org.joda.time.DateTimeConstants.MILLIS_PER_DAY;

final class FilesAdapter extends StableFilesAdapter {

  private final int thumbnailWidth;
  private final int thumbnailHeight;

  FilesAdapter(int thumbnailWidth, int thumbnailHeight) {
    this.thumbnailWidth = thumbnailWidth;
    this.thumbnailHeight = thumbnailHeight;
  }

  static FilesAdapter get(Context context) {
    DisplayMetrics metrics = context.getResources().getDisplayMetrics();

    TypedValue value = new TypedValue();
    Resources.Theme theme = context.getTheme();
    theme.resolveAttribute(android.R.attr.listPreferredItemHeight, value, true);

    int width = metrics.widthPixels - (int) (value.getDimension(metrics) * 2);
    int height = (int) (metrics.heightPixels * 0.6f);

    return new FilesAdapter(width, height);
  }

  private final SparseArray<Info> infos = new SparseArray<>();

  private DateFormat dateFormat;
  private DateFormat timeFormat;
  private Grouper grouper;

  private int columnId = -1;
  private int columnName = -1;
  private int columnSize = -1;
  private int columnModified = -1;
  private int columnReadable = -1;
  private int columnMediaType = -1;

  @Override public void setCursor(Cursor cursor) {
    setCursor(cursor, null);
  }

  public void setCursor(Cursor cursor, String sortOrder) {
    if (cursor != null) {
      setColumns(cursor);
      setGrouper(sortOrder);
    }
    infos.clear();
    super.setCursor(cursor);
  }

  private void setColumns(Cursor cursor) {
    columnId = cursor.getColumnIndexOrThrow(COLUMN_ID);
    columnName = cursor.getColumnIndexOrThrow(COLUMN_NAME);
    columnSize = cursor.getColumnIndexOrThrow(COLUMN_SIZE);
    columnReadable = cursor.getColumnIndexOrThrow(COLUMN_READABLE);
    columnMediaType = cursor.getColumnIndexOrThrow(COLUMN_MEDIA_TYPE);
    columnModified = cursor.getColumnIndexOrThrow(COLUMN_LAST_MODIFIED);
  }

  private void setGrouper(String sortOrder) {
    switch (nullToEmpty(sortOrder)) {
      case SORT_BY_LAST_MODIFIED:
        grouper = new DateGrouper();
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
    holder.setSummary(info.summary);
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

  private static final class ViewHolder extends EmptyCallback {
    private static final LruCache<String, Object> errors = new LruCache<>(1000);

    final View root;
    final TextView title;
    final TextView icon;
    final TextView summary;
    final ImageView preview;
    final TextView header;
    final View headerContainer;
    private String uri;

    ViewHolder(View root) {
      this.root = root;
      this.icon = (TextView) root.findViewById(R.id.icon);
      this.title = (TextView) root.findViewById(R.id.title);
      this.preview = (ImageView) root.findViewById(R.id.preview);
      this.summary = (TextView) root.findViewById(R.id.summary);
      this.header = (TextView) root.findViewById(R.id.header_title);
      this.headerContainer = root.findViewById(R.id.header_container);
    }

    void setEnabled(boolean enabled) {
      root.setEnabled(enabled);
      icon.setEnabled(enabled);
      title.setEnabled(enabled);
      summary.setEnabled(enabled);
    }

    void setTitle(CharSequence text) {
      title.setText(text);
    }

    void setSummary(CharSequence text) {
      summary.setText(text);
    }

    void setIcon(Typeface typeface) {
      icon.setTypeface(typeface);
    }

    void setGroup(String group, String prevGroup) {
      if (group == null || group.equals(prevGroup)) {
        if (headerContainer.getVisibility() != GONE) {
          headerContainer.setVisibility(GONE);
        }
      } else {
        header.setText(group);
        if (headerContainer.getVisibility() != VISIBLE) {
          headerContainer.setVisibility(VISIBLE);
        }
      }
    }

    void setImage(Info info, int width, int height) {
      this.uri = info.uri;
      this.preview.setImageBitmap(null);
      if (preview.getVisibility() != GONE) {
        preview.setVisibility(GONE);
      }
      if (!info.directory && info.readable && errors.get(uri) == null) {
        Picasso.with(preview.getContext())
            .load(uri)
            .resize(width, height)
            .centerInside()
            .into(preview, this);
      }
    }

    @Override public void onSuccess() {
      if (preview.getVisibility() != VISIBLE) {
        preview.setVisibility(VISIBLE);
      }
    }

    @Override public void onError() {
      if (preview.getVisibility() != GONE) {
        preview.setVisibility(GONE);
      }
      errors.put(uri, uri);
    }
  }

  private final class Info {
    final String uri;
    final CharSequence title;
    final CharSequence summary;
    final Typeface icon;
    final boolean readable;
    final boolean directory;
    final String group;

    Info(Context context, int position) {
      Cursor cursor = getItem(position);
      uri = cursor.getString(columnId);
      title = cursor.getString(columnName);
      readable = cursor.getInt(columnReadable) == 1;
      directory = MEDIA_TYPE_DIR.equals(cursor.getString(columnMediaType));
      if (directory) {
        icon = getDirectoryIcon(context, cursor);
        summary = geDirectorySummary(context, cursor);
      } else {
        icon = getFileIcon(context, cursor);
        summary = getFileSummary(context, cursor);
      }
      group = grouper.getGroup(context.getResources(), cursor);
    }

    private String getFileSummary(Context context, Cursor cursor) {
      String size = formatShortFileSize(context, cursor.getLong(columnSize));
      String date = formatLastModified(context, cursor);
      return context.getString(R.string.file_summary, date, size);
    }

    private String geDirectorySummary(Context context, Cursor cursor) {
      return formatLastModified(context, cursor);
    }

    private Typeface getFileIcon(Context context, Cursor cursor) {
      String media = cursor.getString(columnMediaType);
      return IconFonts.forFileMediaType(context.getAssets(), media);
    }

    private Typeface getDirectoryIcon(Context context, Cursor cursor) {
      String id = cursor.getString(columnId);
      return IconFonts.forDirectoryId(context.getAssets(), id);
    }

    private String formatLastModified(Context context, Cursor cursor) {
      if (dateFormat == null) {
        dateFormat = android.text.format.DateFormat.getDateFormat(context);
      }
      if (timeFormat == null) {
        timeFormat = android.text.format.DateFormat.getTimeFormat(context);
      }
      long modified = cursor.getLong(columnModified);
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

  private final class DateGrouper extends Grouper {
    private final MutableDateTime timestamp = new MutableDateTime();
    private final long startOfToday = DateMidnight.now().getMillis();
    private final long startOfTomorrow = startOfToday + MILLIS_PER_DAY;
    private final long startOfYesterday = startOfToday - MILLIS_PER_DAY;
    private final long startOf7Days = startOfToday - MILLIS_PER_DAY * 7L;
    private final long startOf30Days = startOfToday - MILLIS_PER_DAY * 30L;

    @Override public String getGroup(Resources res, Cursor cursor) {

      long modified = cursor.getLong(columnModified);
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
