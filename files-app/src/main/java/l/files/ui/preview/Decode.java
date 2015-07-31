package l.files.ui.preview;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.graphics.Palette;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import l.files.common.graphics.Rect;
import l.files.fs.Resource;
import l.files.fs.Stat;
import l.files.logging.Logger;

import static java.util.Objects.requireNonNull;

public abstract class Decode extends AsyncTask<Object, Object, Object> {

  final Logger log = Logger.get(getClass());
  final Resource res;
  final Stat stat;
  final Rect constraint;
  final Preview context;
  final PreviewCallback callback;

  private final List<Decode> subs;

  Decode(
      Resource res,
      Stat stat,
      Rect constraint,
      PreviewCallback callback,
      Preview context) {
    this.res = requireNonNull(res);
    this.stat = requireNonNull(stat);
    this.constraint = requireNonNull(constraint);
    this.context = requireNonNull(context);
    this.callback = requireNonNull(callback);
    this.subs = new CopyOnWriteArrayList<>();
  }

  public void cancelAll() {
    cancel(true);
    for (Decode sub : subs) {
      sub.cancelAll();
    }
  }

  @SuppressWarnings("unchecked")
  @Override protected void onProgressUpdate(Object... values) {
    super.onProgressUpdate(values);
    for (Object value : values) {

      if (value instanceof Rect) {
        callback.onSizeAvailable(res, (Rect) value);
        context.putSize(res, stat, constraint, (Rect) value);

      } else if (value instanceof Palette) {
        callback.onPaletteAvailable(res, (Palette) value);
        context.putPalette(res, stat, constraint, (Palette) value);

      } else if (value instanceof Bitmap) {
        callback.onPreviewAvailable(res, (Bitmap) value);
        context.putBitmap(res, stat, constraint, (Bitmap) value);
        context.putPreviewable(res, stat, constraint, true);

      } else if (value instanceof NoPreview) {
        callback.onPreviewFailed(res);
        context.putPreviewable(res, stat, constraint, false);

      } else if (value instanceof Decode) {
        Decode sub = (Decode) value;
        subs.add(sub);
        sub.executeOnPreferredExecutor();
      }
    }
  }

  abstract AsyncTask<Object, Object, Object> executeOnPreferredExecutor();

}
