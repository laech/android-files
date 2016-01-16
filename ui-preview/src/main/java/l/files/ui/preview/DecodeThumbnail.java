package l.files.ui.preview;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import l.files.fs.Path;
import l.files.fs.Stat;

import static android.graphics.Bitmap.createScaledBitmap;
import static l.files.base.Objects.requireNonNull;

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

    boolean shouldScale() {
        return true;
    }

    boolean shouldCacheToDisk(Result result, Bitmap scaledBitmap) {
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

        Result result;
        try {
            result = decode();
        } catch (Exception e) {
            e.printStackTrace();
            publishProgress(NoPreview.INSTANCE);
            return null;
        }

        if (isCancelled()) {
            if (result != null) {
                result.maybeScaled.recycle();
            }
            return null;
        }

        if (result == null) {
            publishProgress(NoPreview.INSTANCE);
            return null;
        }

        if (context.getSize(path, stat, constraint, true) == null) {
            publishProgress(result.originalSize);
        }

        final Bitmap scaledBitmap;
        if (shouldScale()) {

            Rect scaledSize = result.originalSize.scale(constraint);
            scaledBitmap = createScaledBitmap(
                    result.maybeScaled,
                    scaledSize.width(),
                    scaledSize.height(),
                    true);

        } else {
            scaledBitmap = result.maybeScaled;
        }

        publishProgress(scaledBitmap);

        // TODO these ifs are also used else where, refactor this

        if (context.getBlurredThumbnail(path, stat, constraint, true) == null) {
            publishProgress(generateBlurredThumbnail(result.maybeScaled));
        }

        if (result.maybeScaled != scaledBitmap) {
            result.maybeScaled.recycle();
        }

        if (isCancelled()) {
            return null;
        }

        if (shouldCacheToDisk(result, scaledBitmap)) {
            saveThumbnailToDiskTask = context.putThumbnailToDiskAsync(
                    path, stat, constraint, scaledBitmap);
        }

        return null;
    }

    @Nullable
    abstract Result decode() throws IOException;

    static final class Result {
        final Bitmap maybeScaled;
        final Rect originalSize;

        Result(Bitmap maybeScaled, Rect originalSize) {
            this.maybeScaled = requireNonNull(maybeScaled);
            this.originalSize = requireNonNull(originalSize);
        }
    }

}
