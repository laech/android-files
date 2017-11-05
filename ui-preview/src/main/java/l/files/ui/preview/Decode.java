package l.files.ui.preview;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.fs.media.MediaTypes;
import l.files.thumbnail.Thumbnailer;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static android.os.Process.setThreadPriority;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static l.files.base.Objects.requireNonNull;
import static l.files.ui.base.content.Contexts.isDebugBuild;

public final class Decode extends AsyncTask<Context, Object, Object> {

    private static final BlockingQueue<Runnable> queue =
            new LinkedBlockingQueue<>();

    private static final AtomicInteger threadSeq =
            new AtomicInteger(1);

    private static final Executor executor = new ThreadPoolExecutor(
            1,
            1,
            0L,
            MILLISECONDS,
            queue,
            r -> new Thread(r, "preview-task-" + threadSeq.getAndIncrement())
    );

    @Nullable
    private volatile Future<?> saveThumbnailToDiskTask;

    private final Path path;
    private final Stat stat;
    private final Rect constraint;
    private final Preview preview;
    private final Preview.Callback callback;

    Decode(
            Path path,
            Stat stat,
            Rect constraint,
            Preview.Callback callback,
            Preview preview
    ) {
        this.path = requireNonNull(path);
        this.stat = requireNonNull(stat);
        this.constraint = requireNonNull(constraint);
        this.callback = requireNonNull(callback);
        this.preview = requireNonNull(preview);
    }

    public void cancelAll() {
        cancel(true);
        Future<?> saveThumbnailToDiskTask = this.saveThumbnailToDiskTask;
        if (saveThumbnailToDiskTask != null) {
            saveThumbnailToDiskTask.cancel(true);
        }
    }

    void awaitAll(long timeout, TimeUnit unit) throws Exception {
        get(timeout, unit);
        Future<?> saveThumbnailToDiskTask = this.saveThumbnailToDiskTask;
        if (saveThumbnailToDiskTask != null) {
            saveThumbnailToDiskTask.get(timeout, unit);
        }
    }

    @Override
    protected final Object doInBackground(Context... params) {
        setThreadPriority(THREAD_PRIORITY_BACKGROUND);

        if (isCancelled()) {
            return null;
        }

        try {
            if (!checkThumbnailMemCache() &&
                    !checkThumbnailDiskCache() &&
                    !checkNoPreviewCache() &&
                    !checkIsCacheFile() &&
                    !decode(params[0]) &&
                    !isCancelled()) {
                publishProgress(NoPreview.DECODE_RETURNED_NULL);
            }
        } catch (Throwable e) {
            publishProgress(new NoPreview(e));
        }
        return null;
    }

    private boolean decode(Context context) throws Exception {
        if (isCancelled()) {
            return false;
        }
        String mediaType = decodeMediaType(context);
        for (Thumbnailer<Path> thumbnailer : Thumbnailer.all) {
            if (thumbnailer.accepts(path, mediaType.toLowerCase())) {
                decode(thumbnailer, context);
                return true;
            }
        }
        return false;
    }

    private void decode(Thumbnailer<Path> thumbnailer, Context context)
            throws Exception {

        ScaledBitmap result = thumbnailer.create(path, constraint, context);
        if (result == null) {
            publishProgress(NoPreview.DECODE_RETURNED_NULL);
            return;
        }

        publishProgress(result);
        saveThumbnailToDiskTask = preview.putThumbnailToDiskAsync(
                path, stat, constraint, result
        );
        publishBlurredIfNeeded(result.bitmap());
    }

    private String decodeMediaType(Context context) throws IOException {
        String mediaType = preview.getMediaType(path, stat, constraint);
        if (mediaType == null) {
            mediaType = MediaTypes.detect(context, path, stat);
            preview.putMediaType(path, stat, constraint, mediaType);
        }
        return mediaType;
    }

    private boolean checkIsCacheFile() {
        if (path.startsWith(preview.cacheDir)) {
            publishProgress(NoPreview.PATH_IN_CACHE_DIR);
            return true;
        }
        return false;
    }

    private boolean checkNoPreviewCache() {
        NoPreview reason = preview.getNoPreviewReason(path, stat, constraint);
        if (reason != null) {
            publishProgress(reason);
            return true;
        }
        return false;
    }

    private boolean checkThumbnailMemCache() {
        Bitmap thumbnail = preview.getThumbnail(path, stat, constraint, true);
        if (thumbnail != null) {
            publishProgress(thumbnail);
            publishBlurredIfNeeded(thumbnail);
            return true;
        }
        return false;
    }

    private boolean checkThumbnailDiskCache() {
        ScaledBitmap thumbnail = null;
        try {
            thumbnail = preview.getThumbnailFromDisk(path, stat, constraint);
        } catch (IOException e) {
            Log.w(getClass().getSimpleName(),
                    "Failed to get disk thumbnail for " + path, e);
        }

        if (thumbnail != null) {
            publishProgress(thumbnail);
            publishBlurredIfNeeded(thumbnail.bitmap());
            return true;
        }
        return false;
    }

    private void publishBlurredIfNeeded(Bitmap bitmap) {
        if (preview.getBlurredThumbnail(path, stat, constraint, true) == null) {
            publishProgress(generateBlurredThumbnail(bitmap));
        }
    }

    private BlurredThumbnail generateBlurredThumbnail(Bitmap bitmap) {
        return new BlurredThumbnail(StackBlur.blur(bitmap));
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);
        for (Object value : values) {
            handleProgressUpdateValue(value);
        }
    }

    private void handleProgressUpdateValue(Object value) {

        if (value instanceof Bitmap) {
            handleUpdate((Bitmap) value);

        } else if (value instanceof ScaledBitmap) {
            handleUpdate((ScaledBitmap) value);

        } else if (value instanceof BlurredThumbnail) {
            handleUpdate((BlurredThumbnail) value);

        } else if (value instanceof NoPreview) {
            handleUpdate((NoPreview) value);

        } else {
            throw new IllegalStateException(String.valueOf(value));
        }
    }

    private void handleUpdate(Bitmap value) {
        preview.putThumbnail(path, stat, constraint, value);
        preview.putPreviewable(path, stat, constraint, true);
        callback.onPreviewAvailable(path, stat, value);
    }

    private void handleUpdate(ScaledBitmap value) {
        preview.putThumbnail(path, stat, constraint, value.bitmap());
        preview.putSize(path, stat, constraint, value.originalSize());
        preview.putPreviewable(path, stat, constraint, true);
        callback.onPreviewAvailable(path, stat, value.bitmap());
    }

    private void handleUpdate(BlurredThumbnail value) {
        preview.putBlurredThumbnail(path, stat, constraint, value.bitmap);
        callback.onBlurredThumbnailAvailable(path, stat, value.bitmap);
    }

    private void handleUpdate(NoPreview value) {
        preview.putPreviewable(path, stat, constraint, false);
        callback.onPreviewFailed(path, stat, value.cause);
    }

    Decode executeOnPreferredExecutor(Context context) {
        if (isDebugBuild(context)) {
            Log.i(getClass().getSimpleName(),
                    "Current queue size " + queue.size() +
                            ", adding " + path);
        }
        return (Decode) executeOnExecutor(executor, context);
    }
}
