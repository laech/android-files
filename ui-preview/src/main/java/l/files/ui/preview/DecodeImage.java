package l.files.ui.preview;

import android.graphics.Bitmap;

import java.io.IOException;
import java.io.InputStream;

import l.files.base.io.Closer;
import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.ui.base.graphics.Rect;

import static android.graphics.BitmapFactory.decodeStream;
import static l.files.fs.Files.newBufferedInputStream;
import static l.files.ui.base.graphics.Bitmaps.decodeBounds;
import static l.files.ui.base.graphics.Bitmaps.scaleOptions;

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
        if (size != null) {
            publishProgress(size);
        }

        if (isCancelled()) {
            return null;
        }

        Closer closer = Closer.create();
        try {

            InputStream in = closer.register(newBufferedInputStream(path));
            if (size == null) {
                size = decodeBounds(in);
            }
            if (size == null) {
                return null;
            }
            Bitmap bitmap = decodeStream(in, null, scaleOptions(size, constraint));
            return bitmap != null ? new Result(bitmap, size) : null;

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

}
