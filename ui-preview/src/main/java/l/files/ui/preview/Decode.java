package l.files.ui.preview;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.graphics.Palette;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import l.files.fs.Path;
import l.files.fs.Stat;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static android.os.Process.setThreadPriority;
import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static l.files.base.Objects.requireNonNull;
import static l.files.ui.preview.Preview.decodePalette;

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

    final Path path;
    final Stat stat;
    final Rect constraint;
    final Preview context;
    final Preview.Callback callback;
    final Preview.Using using;

    private final List<Decode> subs;

    private boolean publishedSize;

    Decode(
            Path path,
            Stat stat,
            Rect constraint,
            Preview.Callback callback,
            Preview.Using using,
            Preview context) {

        this.path = requireNonNull(path);
        this.stat = requireNonNull(stat);
        this.constraint = requireNonNull(constraint);
        this.callback = requireNonNull(callback);
        this.using = requireNonNull(using);
        this.context = requireNonNull(context);
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

        if (isCancelled()) {
            return null;
        }

        if (!checkPreviewable()) {
            return null;
        }

        if (checkIsCache()) {
            return null;
        }

        if (checkThumbnailMemCache()) {
            return null;
        }

        if (checkThumbnailDiskCache()) {
            return null;
        }

        return onDoInBackground();
    }

    abstract Object onDoInBackground();

    private boolean checkIsCache() {
        if (path.startsWith(context.cacheDir)) {
            publishProgress(NoPreview.INSTANCE);
            return true;
        }
        return false;
    }

    private boolean checkPreviewable() {
        if (context.isPreviewable(path, stat, constraint)) {
            return true;
        }
        publishProgress(NoPreview.INSTANCE);
        return false;
    }

    private boolean checkThumbnailMemCache() {
        Bitmap thumbnail = context.getThumbnail(path, stat, constraint, true);
        if (thumbnail != null) {
            publishProgress(thumbnail);
            return true;
        }
        return false;
    }

    private boolean checkThumbnailDiskCache() {
        Bitmap thumbnail = null;
        try {
            thumbnail = context.getThumbnailFromDisk(path, stat, constraint, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (thumbnail != null) {

            if (context.getSize(path, stat, constraint, true) == null) {
                context.putSize(path, stat, constraint, Rect.of(
                        thumbnail.getWidth(),
                        thumbnail.getHeight()));
            }

            if (context.getPalette(path, stat, constraint, true) == null) {
                publishProgress(decodePalette(thumbnail));
            }

            publishProgress(thumbnail);

            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);
        for (Object value : values) {

            if (value instanceof Rect) {
                if (!publishedSize) {
                    publishedSize = true;
                    context.putSize(path, stat, constraint, (Rect) value);
                    callback.onSizeAvailable(path, stat, (Rect) value);
                }

            } else if (value instanceof Palette) {
                context.putPalette(path, stat, constraint, (Palette) value);
                callback.onPaletteAvailable(path, stat, (Palette) value);

            } else if (value instanceof Bitmap) {
                context.putThumbnail(path, stat, constraint, (Bitmap) value);
                context.putPreviewable(path, stat, constraint, true);
                callback.onPreviewAvailable(path, stat, (Bitmap) value);

            } else if (value instanceof NoPreview) {
                callback.onPreviewFailed(path, stat, using);
                context.putPreviewable(path, stat, constraint, false);

            } else if (value instanceof Decode) {
                Decode sub = (Decode) value;
                subs.add(sub);
                sub.executeOnPreferredExecutor();
            }
        }
    }

    Decode executeOnPreferredExecutor() {
        return (Decode) executeOnExecutor(executor);
    }

}
