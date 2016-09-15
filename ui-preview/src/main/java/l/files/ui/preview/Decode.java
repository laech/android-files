package l.files.ui.preview;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.fs.media.MediaTypes;
import l.files.thumbnail.ApkThumbnailer;
import l.files.thumbnail.ImageThumbnailer;
import l.files.thumbnail.MediaThumbnailer;
import l.files.thumbnail.PathStreamThumbnailer;
import l.files.thumbnail.PdfThumbnailer;
import l.files.thumbnail.SvgThumbnailer;
import l.files.thumbnail.TextThumbnailer;
import l.files.thumbnail.Thumbnailer;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static android.os.Process.setThreadPriority;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static l.files.base.Objects.requireNonNull;
import static l.files.ui.base.content.Contexts.isDebugBuild;

public final class Decode extends AsyncTask<Object, Object, Object> {

    private static final BlockingQueue<Runnable> queue =
            new LinkedBlockingQueue<>();

    private static final Executor executor = new ThreadPoolExecutor(
            1,
            1,
            0L,
            MILLISECONDS,
            queue,
            new ThreadFactory() {

                private final AtomicInteger threadNumber =
                        new AtomicInteger(1);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "preview-decode-task-" +
                            threadNumber.getAndIncrement());
                }
            });

    // Need to update NoPreview cache version to invalidate
    // cache when we add a new decoder so existing files
    // marked as not previewable will get re-evaluated.
    // Order matters, from specific to general
    private static final List<Thumbnailer<Path>> thumbnailers = asList(
            new PathStreamThumbnailer(new SvgThumbnailer()),
            new ImageThumbnailer(),
            new MediaThumbnailer(),
            new PdfThumbnailer(),
            new PathStreamThumbnailer(new TextThumbnailer()),
            new ApkThumbnailer());

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

    public void awaitAll(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {

        get(timeout, unit);
        Future<?> saveThumbnailToDiskTask = this.saveThumbnailToDiskTask;
        if (saveThumbnailToDiskTask != null) {
            saveThumbnailToDiskTask.get(timeout, unit);
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (isDebugBuild(preview.context)) {
            Log.i(getClass().getSimpleName(), "Decode enqueued" +
                    ", current queue size " + queue.size() +
                    ", " + path);
        }
    }

    @Override
    protected final Object doInBackground(Object... params) {
        setThreadPriority(THREAD_PRIORITY_BACKGROUND);

        if (isCancelled()) {
            return null;
        }

        try {

            checkThumbnailMemCache();
            checkThumbnailDiskCache();
            checkIsPreviewable();
            checkIsNotCacheFile();
            decode();

        } catch (Stop ignored) {
        } catch (Throwable e) {
            Log.w(getClass().getSimpleName(),
                    "Failed to get disk thumbnail for " + path, e);
            publishProgress(new NoPreview(e));
        }
        return null;
    }

    private void decode() throws Exception {
        if (isCancelled()) {
            return;
        }

        String mediaType = decodeMediaType();
        for (Thumbnailer<Path> thumbnailer : thumbnailers) {
            if (thumbnailer.accepts(path, mediaType.toLowerCase())) {
                decode(thumbnailer);
                break;
            }
        }
        publishProgress(NoPreview.DECODE_RETURNED_NULL);
    }

    private void decode(Thumbnailer<Path> thumbnailer) throws Exception {
        ScaledBitmap result = thumbnailer.create(path, constraint, preview.context);
        if (result != null) {
            publishProgress(result);
            saveThumbnailToDiskTask = preview.putThumbnailToDiskAsync(
                    path, stat, constraint, result);
            publishBlurredIfNeeded(result.bitmap());
            throw Stop.INSTANCE;
        }
    }

    private String decodeMediaType() throws IOException {
        String mediaType = preview.getMediaType(path, stat, constraint, true);
        if (mediaType == null) {
            mediaType = MediaTypes.detect(preview.context, path, stat);
            preview.putMediaType(path, stat, constraint, mediaType);
        }
        return mediaType;
    }

    private void checkIsNotCacheFile() {
        if (path.startsWith(preview.cacheDir)) {
            publishProgress(NoPreview.PATH_IN_CACHE_DIR);
            throw Stop.INSTANCE;
        }
    }

    private void checkIsPreviewable() {
        NoPreview reason = preview.getNoPreviewReason(path, stat, constraint);
        if (reason != null) {
            publishProgress(reason);
            throw Stop.INSTANCE;
        }
    }

    private void checkThumbnailMemCache() {
        Bitmap thumbnail = preview.getThumbnail(path, stat, constraint, true);
        if (thumbnail != null) {
            publishProgress(thumbnail);
            publishBlurredIfNeeded(thumbnail);
            throw Stop.INSTANCE;
        }
    }

    private void checkThumbnailDiskCache() {
        ScaledBitmap thumbnail = null;
        try {
            thumbnail = preview.getThumbnailFromDisk(path, stat, constraint, true);
        } catch (IOException e) {
            Log.w(getClass().getSimpleName(),
                    "Failed to get disk thumbnail for " + path, e);
        }

        if (thumbnail != null) {
            publishProgress(thumbnail);
            publishBlurredIfNeeded(thumbnail.bitmap());
            throw Stop.INSTANCE;
        }
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
            Bitmap result = (Bitmap) value;
            preview.putThumbnail(path, stat, constraint, result);
            preview.putPreviewable(path, stat, constraint, true);
            callback.onPreviewAvailable(path, stat, result);

        } else if (value instanceof ScaledBitmap) {
            ScaledBitmap result = (ScaledBitmap) value;
            preview.putThumbnail(path, stat, constraint, result.bitmap());
            preview.putSize(path, stat, constraint, result.originalSize());
            preview.putPreviewable(path, stat, constraint, true);
            callback.onPreviewAvailable(path, stat, result.bitmap());

        } else if (value instanceof BlurredThumbnail) {
            BlurredThumbnail blur = (BlurredThumbnail) value;
            preview.putBlurredThumbnail(path, stat, constraint, blur.bitmap);
            callback.onBlurredThumbnailAvailable(path, stat, blur.bitmap);

        } else if (value instanceof NoPreview) {
            preview.putPreviewable(path, stat, constraint, false);
            callback.onPreviewFailed(path, stat, ((NoPreview) value).cause);

        } else {
            throw new IllegalStateException(String.valueOf(value));
        }
    }

    Decode executeOnPreferredExecutor() {
        return (Decode) executeOnExecutor(executor);
    }

    private static final class Stop extends RuntimeException {

        static final Stop INSTANCE;

        static {
            INSTANCE = new Stop();
            INSTANCE.setStackTrace(new StackTraceElement[0]);
        }
    }
}
