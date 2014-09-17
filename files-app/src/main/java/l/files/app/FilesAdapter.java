package l.files.app;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.base.Supplier;

import l.files.R;
import l.files.app.category.Categorizer;
import l.files.app.category.FileCategorizers;
import l.files.app.decorator.Decorator;
import l.files.app.decorator.decoration.Decoration;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.util.TypedValue.applyDimension;
import static l.files.R.id;
import static l.files.app.FilesApp.getBitmapCache;
import static l.files.app.decorator.Decorators.compose;
import static l.files.app.decorator.Decorators.enable;
import static l.files.app.decorator.Decorators.font;
import static l.files.app.decorator.Decorators.image;
import static l.files.app.decorator.Decorators.on;
import static l.files.app.decorator.Decorators.text;
import static l.files.app.decorator.Decorators.visible;
import static l.files.app.decorator.decoration.Decorations.all;
import static l.files.app.decorator.decoration.Decorations.category;
import static l.files.app.decorator.decoration.Decorations.categoryVisible;
import static l.files.app.decorator.decoration.Decorations.fileDate;
import static l.files.app.decorator.decoration.Decorations.fileHasDate;
import static l.files.app.decorator.decoration.Decorations.fileIcon;
import static l.files.app.decorator.decoration.Decorations.fileIsReadable;
import static l.files.app.decorator.decoration.Decorations.fileLocation;
import static l.files.app.decorator.decoration.Decorations.fileName;
import static l.files.app.decorator.decoration.Decorations.fileReadable;
import static l.files.app.decorator.decoration.Decorations.fileSize;
import static l.files.app.decorator.decoration.Decorations.isFile;
import static l.files.app.decorator.decoration.Decorations.memoize;
import static l.files.app.decorator.decoration.Decorations.uri;

final class FilesAdapter extends StableFilesAdapter implements Supplier<Categorizer> {

  private Categorizer categorizer;
  private final Decorator decorator;

  FilesAdapter(Context context, int width, int height) {
    Resources res = context.getResources();
    Decoration<String> category = memoize(category(this, res), this);
    Decoration<String> date = memoize(fileDate(context), this);
    Decoration<String> size = memoize(fileSize(context), this);
    Decoration<Boolean> readable = fileReadable();
    Decoration<Boolean> categoryVisibility = categoryVisible(category);
    Decoration<Typeface> icon = memoize(fileIcon(context.getAssets()), this);
    Decoration<Uri> uri = memoize(uri(fileLocation()), this);
    LruCache<Object, Bitmap> cache = getBitmapCache(context);
    this.decorator = compose(
        on(id.title, enable(readable), text(fileName())),
        on(id.icon, enable(readable), font(icon)),
        on(id.date, enable(readable), text(date), visible(fileHasDate())),
        on(id.size, enable(readable), text(size), visible(isFile())),
        on(id.preview, image(uri, all(isFile(), fileIsReadable()), cache, width, height)),
        on(id.header_title, text(category)),
        on(id.header_container, visible(categoryVisibility))
    );
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

    return new FilesAdapter(context, width, height);
  }

  @Override public void setCursor(Cursor cursor) {
    setCursor(cursor, null);
  }

  public void setCursor(Cursor cursor, String sortOrder) {
    if (cursor != null) {
      setCategorizer(sortOrder);
    }
    super.setCursor(cursor);
  }

  private void setCategorizer(String sortOrder) {
    categorizer = FileCategorizers.fromSortOrder(sortOrder);
  }

  @Override public View getView(int position, View view, ViewGroup parent) {
    if (view == null) {
      view = inflate(R.layout.files_item, parent);
    }
    decorator.decorate(position, this, view);
    return view;
  }

  @Override public Categorizer get() {
    return categorizer;
  }
}
