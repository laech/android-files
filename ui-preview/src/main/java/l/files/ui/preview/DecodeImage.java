package l.files.ui.preview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;

import java.io.IOException;
import java.io.InputStream;

import l.files.base.io.Closer;
import l.files.fs.Files;
import l.files.fs.Path;
import l.files.fs.Stat;

import static android.graphics.BitmapFactory.decodeStream;

final class DecodeImage extends DecodeThumbnail {

    static final Previewer PREVIEWER = new Previewer() {

        @Override
        public boolean accept(Path path, String mediaType) {
            return mediaType.startsWith("image/");
        }

        @Override
        public Decode create(
                Path path,
                Stat stat,
                Rect constraint,
                PreviewCallback callback,
                Preview context) {
            return new DecodeImage(path, stat, constraint, callback, context);
        }

    };

    DecodeImage(
            Path path,
            Stat stat,
            Rect constraint,
            PreviewCallback callback,
            Preview context) {
        super(path, stat, constraint, callback, context);
    }

    @Override
    boolean shouldCacheToDisk(Result result, Bitmap scaledBitmap) {
        return result.originalSize.width() > scaledBitmap.getWidth() ||
                result.originalSize.height() > scaledBitmap.getHeight();
    }

    @Override
    Result decode() throws IOException {
        Rect size = context.getSize(file, stat, constraint, true);
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

        Closer closer = Closer.create();
        try {

            InputStream in = closer.register(Files.newBufferedInputStream(file));
            Bitmap bitmap = decodeStream(in, null, options(size));
            return bitmap != null ? new Result(bitmap, size) : null;

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
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
