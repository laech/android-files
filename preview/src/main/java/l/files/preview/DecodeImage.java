package l.files.preview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;

import java.io.IOException;
import java.io.InputStream;

import l.files.fs.File;
import l.files.fs.Stat;

import static android.graphics.BitmapFactory.decodeStream;

final class DecodeImage extends DecodeThumbnail {

    static final Previewer PREVIEWER = new Previewer() {

        @Override
        public boolean accept(String mediaType) {
            return mediaType.startsWith("image/");
        }

        @Override
        public Decode create(
                File res,
                Stat stat,
                Rect constraint,
                PreviewCallback callback,
                Preview context) {
            return new DecodeImage(res, stat, constraint, callback, context);
        }

    };

    DecodeImage(
            File res,
            Stat stat,
            Rect constraint,
            PreviewCallback callback,
            Preview context) {
        super(res, stat, constraint, callback, context);
    }

    @Override
    DecodeImage executeOnPreferredExecutor() {
        return (DecodeImage) executeOnExecutor(THREAD_POOL_EXECUTOR);
    }

    @Override
    boolean shouldCacheToDisk(Result result, Bitmap scaledBitmap) {
        return result.originalSize.width() > scaledBitmap.getWidth() ||
                result.originalSize.height() > scaledBitmap.getHeight();
    }

    @Override
    Result decode() throws IOException {
        Rect size = context.getSize(file, stat, constraint);
        if (size == null) {
            size = context.decodeSize(file);
            if (size != null) {
                publishProgress(size);
            }
        }

        if (isCancelled()) {
            return null;
        }

        if (size == null) {
            return null;
        }

        try (InputStream in = file.newBufferedInputStream()) {
            Bitmap bitmap = decodeStream(in, null, options(size));
            return bitmap != null ? new Result(bitmap, size) : null;
        }
    }

    private Options options(Rect original) {
        Rect scaled = original.scale(constraint);
        float scale = scaled.width() / (float) original.width();
        Options options = new Options();
        options.inSampleSize = (int) (1 / scale);
        return options;
    }

}
