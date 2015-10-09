package l.files.preview;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.graphics.Palette;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import l.files.fs.File;
import l.files.fs.Stat;

import static java.util.Objects.requireNonNull;

public abstract class Decode extends AsyncTask<Object, Object, Object> {

    final File file;
    final Stat stat;
    final Rect constraint;
    final Preview context;
    final PreviewCallback callback;

    private final List<Decode> subs;

    Decode(
            File file,
            Stat stat,
            Rect constraint,
            PreviewCallback callback,
            Preview context) {
        this.file = requireNonNull(file);
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
    @Override
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);
        for (Object value : values) {

            if (value instanceof Rect) {
                callback.onSizeAvailable(file, (Rect) value);
                context.putSize(file, stat, constraint, (Rect) value);

            } else if (value instanceof Palette) {
                callback.onPaletteAvailable(file, (Palette) value);
                context.putPalette(file, stat, constraint, (Palette) value);

            } else if (value instanceof Bitmap) {
                callback.onPreviewAvailable(file, (Bitmap) value);
                context.putThumbnail(file, stat, constraint, (Bitmap) value);
                context.putPreviewable(file, stat, constraint, true);

            } else if (value instanceof NoPreview) {
                callback.onPreviewFailed(file);
                context.putPreviewable(file, stat, constraint, false);

            } else if (value instanceof Decode) {
                Decode sub = (Decode) value;
                subs.add(sub);
                sub.executeOnPreferredExecutor();
            }
        }
    }

    abstract AsyncTask<Object, Object, Object> executeOnPreferredExecutor();

}
