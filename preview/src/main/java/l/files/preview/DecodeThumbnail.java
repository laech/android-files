package l.files.preview;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import java.io.IOException;

import l.files.fs.File;
import l.files.fs.Stat;

import static android.graphics.Bitmap.createScaledBitmap;
import static java.util.Objects.requireNonNull;
import static l.files.preview.Preview.decodePalette;

abstract class DecodeThumbnail extends Decode {

    DecodeThumbnail(
            File res,
            Stat stat,
            Rect constraint,
            PreviewCallback callback,
            Preview context) {
        super(res, stat, constraint, callback, context);
    }

    boolean shouldScale() {
        return true;
    }

    boolean shouldCacheToDisk(Result result, Bitmap scaledBitmap) {
        return true;
    }

    @Override
    protected Void doInBackground(Object... params) {
        if (isCancelled()) {
            return null;
        }

        Result result;
        try {
            result = decode();
        } catch (Exception e) {
            e.printStackTrace();
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

        if (context.getSize(file, stat, constraint) == null) {
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

        if (context.getPalette(file, stat, constraint) == null) {
            publishProgress(decodePalette(scaledBitmap));
        }

        if (result.maybeScaled != scaledBitmap) {
            result.maybeScaled.recycle();
        }

        if (isCancelled()) {
            return null;
        }

        if (shouldCacheToDisk(result, scaledBitmap)) {
            context.putThumbnailToDiskAsync(file, stat, constraint, scaledBitmap);
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
