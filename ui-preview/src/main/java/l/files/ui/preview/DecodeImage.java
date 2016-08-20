package l.files.ui.preview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import l.files.base.io.Closer;
import l.files.fs.Path;
import l.files.fs.Stat;

import static android.graphics.BitmapFactory.decodeStream;
import static l.files.fs.Files.newBufferedInputStream;

final class DecodeImage extends DecodeThumbnail {

    static final Previewer PREVIEWER = new Previewer() {

        @Override
        public boolean acceptsFileExtension(Path path, String extensionInLowercase) {
            switch (extensionInLowercase) {
                case "bmp":
                case "gif":
                case "jpg":
                case "jpeg":
                case "png":
                case "webp":
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public boolean acceptsMediaType(Path path, String mediaTypeInLowercase) {
            return mediaTypeInLowercase.startsWith("image/");
        }

        @Override
        public Decode create(
                Path path,
                Stat stat,
                Rect constraint,
                Preview.Callback callback,
                Preview.Using using,
                Preview context) {
            return new DecodeImage(path, stat, constraint, callback, using, context);
        }

    };

    DecodeImage(
            Path path,
            Stat stat,
            Rect constraint,
            Preview.Callback callback,
            Preview.Using using,
            Preview context) {
        super(path, stat, constraint, callback, using, context);
    }

    @Override
    boolean shouldCacheToDisk(Result result, Bitmap scaledBitmap) {
        return result.originalSize.width() > scaledBitmap.getWidth() ||
                result.originalSize.height() > scaledBitmap.getHeight();
    }

    @Override
    Result decode() throws IOException {
        Rect size = context.getSize(path, stat, constraint, true);
        if (size == null) {
            size = decodeSize();
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

            InputStream in = closer.register(newBufferedInputStream(path));
            Bitmap bitmap = decodeStream(in, null, options(size));
            return bitmap != null ? new Result(bitmap, size) : null;

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    Rect decodeSize() throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        Closer closer = Closer.create();
        try {

            InputStream in = closer.register(newBufferedInputStream(path));
            decodeStream(in, null, options);

        } catch (Exception e) {
            Log.w(getClass().getSimpleName(),
                    "Failed to decode bitmap for " + path, e);
            return null;

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }

        if (options.outWidth > 0 && options.outHeight > 0) {
            return Rect.of(options.outWidth, options.outHeight);
        }
        return null;
    }


    private Options options(Rect original) {
        Rect scaled = original.scale(constraint);
        float scale = scaled.width() / (float) original.width();
        Options options = new Options();
        options.inSampleSize = (int) (1 / scale);
        return options;
    }

}
