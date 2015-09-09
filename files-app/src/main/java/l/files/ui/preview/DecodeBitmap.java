package l.files.ui.preview;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import java.io.IOException;

import l.files.common.graphics.Rect;
import l.files.fs.File;
import l.files.fs.Stat;

import static android.graphics.Bitmap.createScaledBitmap;
import static l.files.ui.preview.Preview.decodePalette;

abstract class DecodeBitmap extends Decode {

    DecodeBitmap(
            File res,
            Stat stat,
            Rect constraint,
            PreviewCallback callback,
            Preview context) {
        super(res, stat, constraint, callback, context);
    }

    @Override
    protected Void doInBackground(Object... params) {
        if (isCancelled()) {
            return null;
        }

        log.verbose("decode start");

        Result result;
        try {
            result = decode();
        } catch (Exception e) {
            log.warn(e);
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

        Rect scaledSize = result.originalSize.scale(constraint);
        Bitmap scaledBitmap = createScaledBitmap(
                result.maybeScaled,
                scaledSize.width(),
                scaledSize.height(),
                true);

        publishProgress(scaledBitmap);

        if (context.getPalette(file, stat, constraint) == null) {
            publishProgress(decodePalette(scaledBitmap));
        }

        if (result.maybeScaled != scaledBitmap) {
            result.maybeScaled.recycle();
        }

        log.verbose("decode end");

        if (isCancelled()) {
            return null;
        }

        boolean scaledDown =
                result.originalSize.width() > scaledBitmap.getWidth() ||
                        result.originalSize.height() > scaledBitmap.getHeight();

        if (scaledDown) {
            context.putBitmapToDiskAsync(file, stat, constraint, scaledBitmap);
        }

        return null;
    }

    @Nullable
    abstract Result decode() throws IOException;

    static final class Result {
        final Bitmap maybeScaled;
        final Rect originalSize;

        Result(Bitmap maybeScaled, Rect originalSize) {
            this.maybeScaled = maybeScaled;
            this.originalSize = originalSize;
        }
    }

}
