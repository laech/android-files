package l.files.ui.preview;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

abstract class DecodeThumbnail extends Decode {

    private volatile Future<?> saveThumbnailToDiskTask;

    DecodeThumbnail(
            Path path,
            Stat stat,
            Rect constraint,
            Preview.Callback callback,
            Preview.Using using,
            Preview context) {
        super(path, stat, constraint, callback, using, context);
    }

    boolean shouldCacheToDisk(ScaledBitmap result, Bitmap scaledBitmap) {
        return true;
    }

    @Override
    public void awaitAll(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        super.awaitAll(timeout, unit);
        Future<?> saveThumbnailToDiskTask = this.saveThumbnailToDiskTask;
        if (saveThumbnailToDiskTask != null) {
            saveThumbnailToDiskTask.get(timeout, unit);
        }
    }

    @Override
    Object onDoInBackground() {
        if (isCancelled()) {
            return null;
        }

        ScaledBitmap result;
        try {
            result = decode();
        } catch (Throwable e) {
            publishProgress(new NoPreview(e));
            return null;
        }

        if (isCancelled()) {
            if (result != null) {
                result.bitmap().recycle();
            }
            return null;
        }

        if (result == null) {
            publishProgress(NoPreview.DECODE_RETURNED_NULL);
            return null;
        }

        if (context.getSize(path, stat, constraint, true) == null) {
            publishProgress(result.originalSize());
        }

        publishProgress(result.bitmap());

        // TODO these ifs are also used else where, refactor this

        if (context.getBlurredThumbnail(path, stat, constraint, true) == null) {
            publishProgress(generateBlurredThumbnail(result.bitmap()));
        }

        if (isCancelled()) {
            return null;
        }

        if (shouldCacheToDisk(result, result.bitmap())) {
            saveThumbnailToDiskTask = context.putThumbnailToDiskAsync(
                    path, stat, constraint, result.bitmap());
        }

        return null;
    }

    @Nullable
    abstract ScaledBitmap decode() throws Exception;

}

