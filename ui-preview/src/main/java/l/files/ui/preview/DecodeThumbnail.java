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


    /* TODO

02-22 22:08:30.470 504-504/? A/DEBUG: Build fingerprint: 'htc/m8_google/htc_m8:6.0/MRA58K.H10/666671:user/release-keys'
02-22 22:08:30.470 504-504/? A/DEBUG: Revision: '0'
02-22 22:08:30.471 504-504/? A/DEBUG: ABI: 'arm'
02-22 22:08:30.471 504-504/? A/DEBUG: pid: 20272, tid: 20289, name: ationTestRunner  >>> l.files.ui.preview.test <<<
02-22 22:08:30.471 504-504/? A/DEBUG: signal 7 (SIGBUS), code 2 (BUS_ADRERR), fault addr 0x9e050000
02-22 22:08:30.496 504-504/? A/DEBUG:     r0 9e0840b0  r1 9e04d124  r2 000001e0  r3 00000110
02-22 22:08:30.497 504-504/? A/DEBUG:     r4 0000009c  r5 00000019  r6 00000019  r7 00000019
02-22 22:08:30.497 504-504/? A/DEBUG:     r8 b36be4a0  r9 a92c94a0  sl 9e04a414  fp 9e04fff4
02-22 22:08:30.497 504-504/? A/DEBUG:     ip a92c9430  sp a92c93f8  lr b36bddbc  pc b36be4a4  cpsr 200f0010
02-22 22:08:30.500 504-504/? A/DEBUG:     #00 pc 0001b4a4  /system/lib/libRSCpuRef.so
02-22 22:08:30.500 504-504/? A/DEBUG:     #01 pc 0001adb8  /system/lib/libRSCpuRef.so

     */

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
