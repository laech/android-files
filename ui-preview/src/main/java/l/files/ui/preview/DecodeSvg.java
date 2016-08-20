package l.files.ui.preview;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.caverock.androidsvg.SVG;

import java.io.IOException;
import java.io.InputStream;

import l.files.base.io.Closer;
import l.files.fs.Path;
import l.files.fs.Stat;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Color.WHITE;
import static l.files.fs.Files.newBufferedInputStream;

final class DecodeSvg extends DecodeThumbnail {

    static final Previewer PREVIEWER = new Previewer() {

        @Override
        public boolean acceptsFileExtension(
                Path path,
                String extensionInLowercase) {
            return extensionInLowercase.equals("svg");
        }

        @Override
        public boolean acceptsMediaType(
                Path path,
                String mediaTypeInLowercase) {
            return mediaTypeInLowercase.startsWith("image/svg+xml");
        }

        @Override
        public Decode create(
                Path path,
                Stat stat,
                Rect constraint,
                Preview.Callback callback,
                Preview.Using using,
                Preview context) {
            return new DecodeSvg(path, stat, constraint, callback, using, context);
        }

    };

    DecodeSvg(
            Path path,
            Stat stat,
            Rect constraint,
            Preview.Callback callback,
            Preview.Using using,
            Preview context) {
        super(path, stat, constraint, callback, using, context);
    }

    @Override
    Result decode() throws IOException {
        Closer closer = Closer.create();
        try {

            InputStream in = closer.register(newBufferedInputStream(path));
            SVG svg = SVG.getFromInputStream(in);
            if (svg == null ||
                    svg.getDocumentWidth() < 1 ||
                    svg.getDocumentHeight() < 1) {
                return null;
            }

            Rect originalSize = Rect.of(
                    (int) svg.getDocumentWidth(),
                    (int) svg.getDocumentHeight());

            Rect scaledSize = originalSize.scale(constraint);
            svg.setDocumentWidth(scaledSize.width());
            svg.setDocumentHeight(scaledSize.height());

            Bitmap bitmap = createBitmap(
                    context.displayMetrics,
                    scaledSize.width(),
                    scaledSize.height(),
                    ARGB_8888);

            bitmap.eraseColor(WHITE);
            Canvas canvas = new Canvas(bitmap);
            svg.renderToCanvas(canvas);
            return new Result(bitmap, originalSize);

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

}
