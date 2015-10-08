package l.files.ui.preview;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import java.io.IOException;

import l.files.common.graphics.Rect;
import l.files.fs.File;
import l.files.fs.Stat;

import static android.graphics.Bitmap.createScaledBitmap;
import static l.files.ui.preview.Preview.decodePalette;
import static l.files.ui.preview.Thumbnail.Type.PICTURE;

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

    Thumbnail.Type thumbnailType() {
        return PICTURE;
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
            log.warn(e);
            return null;
        }

        if (isCancelled()) {
            if (result != null) {
                result.thumbnail.bitmap.recycle();
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
                    result.thumbnail.bitmap,
                    scaledSize.width(),
                    scaledSize.height(),
                    true);

        } else {
            scaledBitmap = result.thumbnail.bitmap;
        }

        publishProgress(new Thumbnail(scaledBitmap, thumbnailType()));

        if (context.getPalette(file, stat, constraint) == null) {
            publishProgress(decodePalette(scaledBitmap));
        }

        if (result.thumbnail.bitmap != scaledBitmap) {
            result.thumbnail.bitmap.recycle();
        }

        if (isCancelled()) {
            return null;
        }

        boolean scaledDown =
                result.originalSize.width() > scaledBitmap.getWidth() ||
                        result.originalSize.height() > scaledBitmap.getHeight();

        if (scaledDown) {
            Thumbnail cache = new Thumbnail(scaledBitmap, result.thumbnail.type);
            context.putThumbnailToDiskAsync(file, stat, constraint, cache);
        }

        return null;
    }

    @Nullable
    abstract Result decode() throws IOException;

    static final class Result {
        final Thumbnail thumbnail;
        final Rect originalSize;

        Result(Thumbnail thumbnail, Rect originalSize) {
            this.thumbnail = thumbnail;
            this.originalSize = originalSize;
        }
    }

}
