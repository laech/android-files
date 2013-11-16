package l.files.app;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.util.LruCache;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import l.files.R;
import l.files.app.format.IconFonts;

import static android.text.format.DateUtils.isToday;
import static android.text.format.Formatter.formatShortFileSize;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.squareup.picasso.Callback.EmptyCallback;
import static l.files.provider.FilesContract.FileInfo.COLUMN_ID;
import static l.files.provider.FilesContract.FileInfo.COLUMN_LAST_MODIFIED;
import static l.files.provider.FilesContract.FileInfo.COLUMN_MEDIA_TYPE;
import static l.files.provider.FilesContract.FileInfo.COLUMN_NAME;
import static l.files.provider.FilesContract.FileInfo.COLUMN_READABLE;
import static l.files.provider.FilesContract.FileInfo.COLUMN_SIZE;
import static l.files.provider.FilesContract.FileInfo.MEDIA_TYPE_DIR;

final class FilesAdapter extends CursorAdapter {

  private final int thumbnailSize;

  FilesAdapter(int thumbnailSize) {
    this.thumbnailSize = thumbnailSize;
  }

  static FilesAdapter get(Context context) {
    TypedValue value = new TypedValue();
    context.getTheme().resolveAttribute(
        android.R.attr.listPreferredItemHeight, value, true);
    int thumbnailSize = (int) value.getDimension(
        context.getResources().getDisplayMetrics());
    return new FilesAdapter(thumbnailSize);
  }

  private static final TObjectLongMap<String> ids = new TObjectLongHashMap<>();

  private DateFormat dateFormat;
  private DateFormat timeFormat;

  private int columnId = -1;
  private int columnName = -1;
  private int columnSize = -1;
  private int columnModified = -1;
  private int columnReadable = -1;
  private int columnMediaType = -1;

  private final SparseArray<Info> infos = new SparseArray<>();

  @Override public void setCursor(Cursor cursor) {
    if (cursor != null) {
      columnId = cursor.getColumnIndexOrThrow(COLUMN_ID);
      columnName = cursor.getColumnIndexOrThrow(COLUMN_NAME);
      columnSize = cursor.getColumnIndexOrThrow(COLUMN_SIZE);
      columnReadable = cursor.getColumnIndexOrThrow(COLUMN_READABLE);
      columnMediaType = cursor.getColumnIndexOrThrow(COLUMN_MEDIA_TYPE);
      columnModified = cursor.getColumnIndexOrThrow(COLUMN_LAST_MODIFIED);
    }
    infos.clear();
    super.setCursor(cursor);
  }

  @Override public View getView(int position, View view, ViewGroup parent) {
    if (view == null) {
      view = inflate(R.layout.files_item, parent);
      view.setTag(new ViewHolder(view));
    }

    Info info = infos.get(position);
    if (info == null) {
      info = new Info(parent.getContext(), position);
      infos.put(position, info);
    }

    ViewHolder holder = (ViewHolder) view.getTag();
    holder.setTitle(info.title);
    holder.setEnabled(info.readable);
    holder.setIcon(info.icon);
    holder.setSummary(info.summary);
    holder.setImage(info, thumbnailSize);

    return view;
  }

  @Override public boolean hasStableIds() {
    return true;
  }

  @Override public long getItemId(int position) {
    String fileId = getItem(position).getString(columnId);
    ids.putIfAbsent(fileId, ids.size() + 1);
    return ids.get(fileId);
  }

  private static final class ViewHolder extends EmptyCallback {
    private static final LruCache<String, Object> errors = new LruCache<>(1000);

    final View root;
    final TextView title;
    final TextView icon;
    final TextView summary;
    final ImageView image;
    private String uri;

    ViewHolder(View root) {
      this.root = root;
      this.icon = (TextView) root.findViewById(android.R.id.icon);
      this.title = (TextView) root.findViewById(android.R.id.title);
      this.image = (ImageView) root.findViewById(android.R.id.background);
      this.summary = (TextView) root.findViewById(android.R.id.summary);
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
      icon.setVisibility(VISIBLE);
    }

    void setImage(Info info, int thumbnailSize) {
      this.uri = info.uri;
      this.image.setImageBitmap(null);
      if (!info.directory && info.readable && errors.get(uri) == null) {
        Picasso.with(image.getContext())
            .load(uri)
            .resize(thumbnailSize, thumbnailSize)
            .centerCrop()
            .into(image, this);
      }
    }

    @Override public void onSuccess() {
      icon.setVisibility(INVISIBLE);
    }

    @Override public void onError() {
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
}
