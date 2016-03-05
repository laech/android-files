package l.files.ui.preview;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import l.files.fs.Path;
import l.files.fs.Stat;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static android.os.Process.setThreadPriority;
import static java.lang.Math.min;
import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static l.files.base.Objects.requireNonNull;
import static l.files.ui.preview.Preview.Using.MEDIA_TYPE;

public abstract class Decode extends AsyncTask<Object, Object, Object> {

    private static final Executor executor = newFixedThreadPool(
            min(getRuntime().availableProcessors(), 2),
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

    public void awaitAll(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {

        get(timeout, unit);
        for (Decode sub : subs) {
            sub.awaitAll(timeout, unit);
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

            if (context.getBlurredThumbnail(path, stat, constraint, true) == null) {
                publishProgress(generateBlurredThumbnail(thumbnail));
            }

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

            if (context.getBlurredThumbnail(path, stat, constraint, true) == null) {
                publishProgress(generateBlurredThumbnail(thumbnail));
            }

            // TODO should publish this first so caller waits less
            publishProgress(thumbnail);

            return true;
        }
        return false;
    }

    // TODO save this to disk
    BlurredThumbnail generateBlurredThumbnail(Bitmap bitmap) {
        return new BlurredThumbnail(StackBlur.blur(bitmap, 0.33f, 70));
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

            } else if (value instanceof Bitmap) {
                context.putThumbnail(path, stat, constraint, (Bitmap) value);
                context.putPreviewable(path, stat, constraint, true);
                callback.onPreviewAvailable(path, stat, (Bitmap) value);

            } else if (value instanceof BlurredThumbnail) {
                Bitmap thumbnail = ((BlurredThumbnail) value).bitmap;
                context.putBlurredThumbnail(path, stat, constraint, thumbnail);
                callback.onBlurredThumbnailAvailable(path, stat, thumbnail);

            } else if (value instanceof NoPreview) {
                if (using == MEDIA_TYPE) {
                    callback.onPreviewFailed(path, stat, using);
                    context.putPreviewable(path, stat, constraint, false);

                } else {
                    Decode sub = DecodeChain.run(
                            path, stat, constraint, callback, MEDIA_TYPE, context);
                    if (sub != null) {
                        subs.add(sub);
                    }
                }

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
