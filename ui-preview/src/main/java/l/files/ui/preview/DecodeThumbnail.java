package l.files.ui.preview;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static l.files.ui.base.graphics.Bitmaps.scaleDownBitmap;

abstract class DecodeThumbnail extends Decode {

    /* TODO


02-11 17:43:16.311 14022-14082/? E/AndroidRuntime: FATAL EXCEPTION: preview-decode-task-2
Process: l.files.debug, PID: 14022
java.lang.RuntimeException: An error occurred while executing doInBackground()
   at android.os.AsyncTask$3.done(AsyncTask.java:309)
   at java.util.concurrent.FutureTask.finishCompletion(FutureTask.java:354)
   at java.util.concurrent.FutureTask.setException(FutureTask.java:223)
   at java.util.concurrent.FutureTask.run(FutureTask.java:242)
   at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1113)
   at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:588)
   at java.lang.Thread.run(Thread.java:818)
Caused by: java.lang.OutOfMemoryError: Failed to allocate a 942852 byte allocation with 457208 free bytes and 446KB until OOM
   at dalvik.system.VMRuntime.newNonMovableArray(Native Method)
   at android.graphics.Bitmap.nativeCreate(Native Method)
   at android.graphics.Bitmap.createBitmap(Bitmap.java:831)
   at android.graphics.Bitmap.createBitmap(Bitmap.java:808)
   at android.graphics.Bitmap.createBitmap(Bitmap.java:739)
   at android.graphics.Bitmap.createScaledBitmap(Bitmap.java:615)
   at l.files.ui.preview.DecodeThumbnail.onDoInBackground(DecodeThumbnail.java:85)
   at l.files.ui.preview.Decode.doInBackground(Decode.java:121)
   at android.os.AsyncTask$2.call(AsyncTask.java:295)
   at java.util.concurrent.FutureTask.run(FutureTask.java:237)
   at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1113) 
   at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:588) 
   at java.lang.Thread.run(Thread.java:818) 


     */

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

    boolean shouldScaleDown() {
        return true;
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
        } catch (Exception e) {
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

        final Bitmap scaledBitmap;
        if (shouldScaleDown()) {
            // TODO remove this, let thumbnailers do this
            scaledBitmap = scaleDownBitmap(result.bitmap(), constraint).bitmap();
        } else {
            scaledBitmap = result.bitmap();
        }

        publishProgress(scaledBitmap);

        // TODO these ifs are also used else where, refactor this

        if (context.getBlurredThumbnail(path, stat, constraint, true) == null) {
            publishProgress(generateBlurredThumbnail(result.bitmap()));
        }

        if (result.bitmap() != scaledBitmap) {
            result.bitmap().recycle();
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
    abstract ScaledBitmap decode() throws Exception;

    static Bitmap createBitmap(
            DisplayMetrics display,
            int width,
            int height,
            Bitmap.Config config) {

        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        bitmap.setDensity(display.densityDpi);
        return bitmap;
    }

}

