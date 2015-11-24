package l.files.ui.preview;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.graphics.Palette;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import l.files.fs.File;
import l.files.fs.Stat;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static android.os.Process.setThreadPriority;
import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static l.files.base.Objects.requireNonNull;

public abstract class Decode extends AsyncTask<Object, Object, Object> {

    private static final Executor executor = newFixedThreadPool(
            getRuntime().availableProcessors() + 1,
            new ThreadFactory() {

                private final AtomicInteger threadNumber = new AtomicInteger(1);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "preview-decode-task-" + threadNumber.getAndIncrement());
                }

            });

    final File file;
    final Stat stat;
    final Rect constraint;
    final Preview context;
    final PreviewCallback callback;

    private final List<Decode> subs;

    private boolean publishedSize;

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

    @Override
    protected final Object doInBackground(Object... params) {
        setThreadPriority(THREAD_PRIORITY_BACKGROUND);
        return onDoInBackground();
    }

    abstract Object onDoInBackground();

    @SuppressWarnings("unchecked")
    @Override
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);
        for (Object value : values) {

            if (value instanceof Rect) {
                if (!publishedSize) {
                    publishedSize = true;
                    context.putSize(file, stat, constraint, (Rect) value);
                    callback.onSizeAvailable(file, stat, (Rect) value);
                }

            } else if (value instanceof Palette) {
                context.putPalette(file, stat, constraint, (Palette) value);
                callback.onPaletteAvailable(file, stat, (Palette) value);

            } else if (value instanceof Bitmap) {
                context.putThumbnail(file, stat, constraint, (Bitmap) value);
                context.putPreviewable(file, stat, constraint, true);
                callback.onPreviewAvailable(file, stat, (Bitmap) value);

            } else if (value instanceof NoPreview) {
                callback.onPreviewFailed(file, stat);
                context.putPreviewable(file, stat, constraint, false);

            } else if (value instanceof Decode) {
                Decode sub = (Decode) value;
                subs.add(sub);
                sub.executeOnPreferredExecutor();
            }
        }
    }

    AsyncTask<Object, Object, Object> executeOnPreferredExecutor() {
        return executeOnExecutor(executor);
    }

}
