package l.files.app.decorator.decoration;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.SparseArray;
import android.widget.Adapter;

import com.google.common.base.Objects;
import com.google.common.base.Supplier;

import java.text.DateFormat;
import java.util.Date;

import l.files.app.IconFonts;
import l.files.app.category.Categorizer;

import static android.text.format.DateFormat.getDateFormat;
import static android.text.format.DateFormat.getTimeFormat;
import static android.text.format.DateUtils.isToday;
import static android.text.format.Formatter.formatShortFileSize;
import static l.files.common.database.Cursors.getBoolean;
import static l.files.common.database.Cursors.getLong;
import static l.files.common.database.Cursors.getString;
import static l.files.provider.FileCursors.getFileId;
import static l.files.provider.FileCursors.getMediaType;
import static l.files.provider.FileCursors.getSize;
import static l.files.provider.FileCursors.isDirectory;
import static l.files.provider.FilesContract.FileInfo;
import static l.files.provider.FilesContract.FileInfo.COLUMN_ID;
import static l.files.provider.FilesContract.FileInfo.COLUMN_LAST_MODIFIED;
import static l.files.provider.FilesContract.FileInfo.COLUMN_NAME;
import static l.files.provider.FilesContract.FileInfo.COLUMN_READABLE;

public final class Decorations {
  private Decorations() {}

  /**
   * Returns a function that will only call the given delegate once per position
   * and caches the returned values. The cached values will be cleared once the
   * given adapter's data set changes.
   */
  public static <T> Decoration<T> memoize(
      final Decoration<T> delegate, Adapter adapter) {
    final SparseArray<T> cache = new SparseArray<>();
    adapter.registerDataSetObserver(clearCacheObserver(cache));
    return new Decoration<T>() {
      @Override public T get(int position, Adapter adapter) {
        T cached = cache.get(position);
        if (cached == null) {
          cached = delegate.get(position, adapter);
          cache.put(position, cached);
        }
        return cached;
      }
    };
  }

  private static <T> DataSetObserver clearCacheObserver(
      final SparseArray<T> cache) {
    return new DataSetObserver() {
      @Override public void onChanged() {
        super.onChanged();
        cache.clear();
      }

      @Override public void onInvalidated() {
        super.onInvalidated();
        cache.clear();
      }
    };
  }

  private static Cursor getCursor(Adapter adapter, int position) {
    return (Cursor) adapter.getItem(position);
  }

  /**
   * Returns a function to return the string at the given cursor column name.
   */
  public static Decoration<String> cursorString(final String column) {
    return new Decoration<String>() {
      @Override public String get(int position, Adapter adapter) {
        return getString(getCursor(adapter, position), column);
      }
    };
  }

  /**
   * Returns a function to return the boolean value at the given cursor column
   * name. The boolean value will be true if the column value is 1 (int),
   * otherwise false.
   */
  public static Decoration<Boolean> cursorBoolean(final String column) {
    return new Decoration<Boolean>() {
      @Override public Boolean get(int position, Adapter adapter) {
        return getBoolean(getCursor(adapter, position), column);
      }
    };
  }

  /**
   * Returns a function to format the timestamp (milliseconds) at the given
   * cursor column name.
   */
  public static Decoration<String> cursorDateFormat(
      final String column, Context context) {
    final DateFormat dateFormat = getDateFormat(context);
    final DateFormat timeFormat = getTimeFormat(context);
    final Date date = new Date();
    return new Decoration<String>() {
      @Override public String get(int position, Adapter adapter) {
        long time = getLong(getCursor(adapter, position), column);
        date.setTime(time);
        return (isToday(time) ? timeFormat : dateFormat).format(date);
      }
    };
  }

  /**
   * Returns a function to get the ID for each {@link FileInfo}.
   */
  public static Decoration<String> fileId() {
    return cursorString(COLUMN_ID);
  }

  /**
   * Returns a function to get the name for each {@link FileInfo}.
   */
  public static Decoration<String> fileName() {
    return cursorString(COLUMN_NAME);
  }

  /**
   * Returns a function to get the readability of each {@link FileInfo}.
   */
  public static Decoration<Boolean> fileReadable() {
    return cursorBoolean(COLUMN_READABLE);
  }

  /**
   * Returns a function to get the formatted last modified date of each {@link
   * FileInfo}.
   */
  public static Decoration<String> fileDate(final Context context) {
    return cursorDateFormat(COLUMN_LAST_MODIFIED, context);
  }

  /**
   * Returns a function to get the icon font for each {@link FileInfo}.
   */
  public static Decoration<Typeface> fileIcon(final AssetManager assets) {
    return new Decoration<Typeface>() {
      @Override public Typeface get(int position, Adapter adapter) {
        Cursor cursor = getCursor(adapter, position);
        if (isDirectory(cursor)) {
          return IconFonts.forDirectoryId(assets, getFileId(cursor));
        } else {
          return IconFonts.forFileMediaType(assets, getMediaType(cursor));
        }
      }
    };
  }

  /**
   * Returns a function to format the file size for each {@link FileInfo}.
   */
  public static Decoration<String> fileSize(final Context context) {
    return new Decoration<String>() {
      @Override public String get(int position, Adapter adapter) {
        Cursor cursor = getCursor(adapter, position);
        if (isDirectory(cursor)) {
          return "";
        } else {
          return formatShortFileSize(context, getSize(cursor));
        }
      }
    };
  }

  /**
   * Returns a function to indicate whether a {@link FileInfo} is a file instead
   * of an directory.
   */
  public static Decoration<Boolean> isFile() {
    return new Decoration<Boolean>() {
      @Override public Boolean get(int position, Adapter adapter) {
        return !isDirectory(getCursor(adapter, position));
      }
    };
  }

  /**
   * Returns a function to return the category at each position.
   */
  public static Decoration<String> category(
      final Supplier<Categorizer> categorizer, final Resources res) {
    return new Decoration<String>() {
      @Override public String get(int position, Adapter adapter) {
        return categorizer.get().getCategory(res, getCursor(adapter, position));
      }
    };
  }

  /**
   * Returns a function to indicate whether the category should be visible at
   * each position.
   */
  public static Decoration<Boolean> categoryVisible(
      final Decoration<?> decoration) {
    return new Decoration<Boolean>() {
      @Override public Boolean get(int position, Adapter adapter) {
        if (position == 0) {
          return true;
        }
        Object previous = decoration.get(position - 1, adapter);
        Object current = decoration.get(position, adapter);
        return !Objects.equal(current, previous);
      }
    };
  }

  /**
   * Returns a function to transform each string to a {@link Uri}.
   */
  public static Decoration<Uri> uri(final Decoration<String> decoration) {
    return new Decoration<Uri>() {
      @Override public Uri get(int position, Adapter adapter) {
        return Uri.parse(decoration.get(position, adapter));
      }
    };
  }
}
