package l.files.ui.preview;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.google.common.net.MediaType;

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

  private final List<AsyncTask<?, ?, ?>> subs;

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
    for (AsyncTask<?, ?, ?> sub : subs) {
      sub.cancel(true);
    }
  }

  @SuppressWarnings("unchecked")
  @Override protected void onProgressUpdate(Object... values) {
    super.onProgressUpdate(values);
    for (Object value : values) {

      if (value instanceof Rect) {
        callback.onSizeAvailable(res, (Rect) value);
        context.putSize(res, stat, constraint, (Rect) value);

      } else if (value instanceof Bitmap) {
        callback.onPreviewAvailable(res, (Bitmap) value);
        context.putBitmap(res, stat, constraint, (Bitmap) value);
        context.putPreviewable(res, stat, constraint, true);

      } else if (value instanceof MediaType) {
        context.putMediaType(res, stat, constraint, (MediaType) value);

      } else if (value instanceof NoPreview) {
        callback.onPreviewFailed(res);
        context.putPreviewable(res, stat, constraint, false);

      } else if (value instanceof AsyncTask<?, ?, ?>) {
        subs.add(((AsyncTask<Object, Object, Object>) value)
            .executeOnExecutor(SERIAL_EXECUTOR));
      }
    }
  }

}