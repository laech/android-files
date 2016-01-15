package l.files.ui.preview;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.graphics.Palette;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;

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

import static android.graphics.Bitmap.createScaledBitmap;
import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static android.os.Process.setThreadPriority;
import static java.lang.Math.max;
import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static l.files.base.Objects.requireNonNull;
import static l.files.ui.preview.Preview.Using.MEDIA_TYPE;
import static l.files.ui.preview.Preview.decodePaletteColor;

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

    private static RenderScript rs;

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

        if (rs == null) {
            rs = RenderScript.create(context.context);
        }
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

            if (context.getPaletteColor(path, stat, constraint, true) == null) {
                Integer color = decodePaletteColor(thumbnail);
                if (color != null) {
                    publishProgress(new PaletteColor(color));
                }
            }

            if (context.getBlurredThumbnail(path, stat, constraint, true) == null) {
                publishProgress(generateBlurredThumbnail(thumbnail));
            }

            publishProgress(thumbnail);

            return true;
        }
        return false;
    }

    static BlurredThumbnail generateBlurredThumbnail(Bitmap bitmap) {
        int width = max(bitmap.getWidth() / 3, 1);
        int height = max(bitmap.getHeight() / 3, 1);
        Bitmap result = createScaledBitmap(bitmap, width, height, false);
        Allocation input = Allocation.createFromBitmap(rs, result);
        Allocation output = Allocation.createTyped(rs, input.getType());
        ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        script.setRadius(25);
        script.setInput(input);
        script.forEach(output);
        output.copyTo(result);
        return new BlurredThumbnail(result);
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

            } else if (value instanceof PaletteColor) {
                int color = ((PaletteColor) value).color;
                context.putPaletteColor(path, stat, constraint, color);
                callback.onPaletteColorAvailable(path, stat, color);

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
