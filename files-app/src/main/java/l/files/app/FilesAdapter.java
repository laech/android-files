package l.files.app;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
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

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

import l.files.R;
import l.files.app.category.Categorizer;
import l.files.app.category.FileDateCategorizer;
import l.files.app.category.FileNameCategorizer;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
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
import static java.lang.System.currentTimeMillis;
import static l.files.provider.FileCursors.getFileId;
import static l.files.provider.FileCursors.getFileName;
import static l.files.provider.FileCursors.getLastModified;
import static l.files.provider.FileCursors.getMediaType;
import static l.files.provider.FileCursors.getSize;
import static l.files.provider.FileCursors.isDirectory;
import static l.files.provider.FileCursors.isReadable;
import static l.files.provider.FilesContract.FileInfo.SORT_BY_LAST_MODIFIED;
import static l.files.provider.FilesContract.FileInfo.SORT_BY_NAME;

final class FilesAdapter extends StableFilesAdapter {

  private final SparseArray<Info> infos = new SparseArray<>();

  private final int thumbnailWidth;
  private final int thumbnailHeight;

  private DateFormat dateFormat;
  private DateFormat timeFormat;
  private Categorizer categorizer;

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
      setCategorizer(sortOrder);
    }
    infos.clear();
    super.setCursor(cursor);
  }

  private void setCategorizer(String sortOrder) {
    switch (nullToEmpty(sortOrder)) {
      case SORT_BY_LAST_MODIFIED:
        categorizer = new FileDateCategorizer(currentTimeMillis());
        break;
      case SORT_BY_NAME:
        categorizer = FileNameCategorizer.INSTANCE;
        break;
      default:
        categorizer = Categorizer.NULL;
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

  // TODO clean up
  private static final Map<String, Point> sizesPortrait = newHashMap();
  private static final Map<String, Point> sizesLandscape = newHashMap();

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
      preview.setMinimumWidth(0);
      preview.setMinimumHeight(0);
      preview.setVisibility(GONE);
      if (!info.directory && info.readable && errors.get(uri) == null) {
        Point size = getSizeCache().get(uri);
        if (size != null) {
          preview.setMinimumWidth(size.x);
          preview.setMinimumHeight(size.y);
          preview.setVisibility(VISIBLE);
        }
        Picasso.with(preview.getContext())
            .load(uri)
            .resize(width, height)
            .centerInside()
            .into(this);
      }
    }

    private Map<String, Point> getSizeCache() {
      int orientation = preview.getResources().getConfiguration().orientation;
      if (orientation == ORIENTATION_PORTRAIT) {
        return sizesPortrait;
      } else {
        return sizesLandscape;
      }
    }

    void setDirectory(boolean directory) {
      size.setVisibility(directory ? GONE : VISIBLE);
    }

    @Override public void onBitmapLoaded(Bitmap bitmap, LoadedFrom from) {
      Resources res = preview.getResources();
      if (!getSizeCache().containsKey(uri)) {
        int extra = res.getDimensionPixelSize(R.dimen.file_preview_padding) * 2;
        getSizeCache().put(uri, new Point(
            bitmap.getWidth() + extra,
            bitmap.getHeight() + extra));
      }
      if (!LoadedFrom.MEMORY.equals(from)) {
        TransitionDrawable drawable = new TransitionDrawable(new Drawable[]{
            new ColorDrawable(TRANSPARENT),
            new BitmapDrawable(res, bitmap)});
        preview.setImageDrawable(drawable);
        int duration = res.getInteger(android.R.integer.config_shortAnimTime);
        drawable.startTransition(duration);
      } else {
        preview.setImageBitmap(bitmap);
      }
      preview.setVisibility(VISIBLE);
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
      group = categorizer.getCategory(context.getResources(), cursor);
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
}
