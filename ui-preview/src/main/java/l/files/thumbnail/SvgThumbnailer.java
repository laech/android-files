package l.files.thumbnail;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import androidx.annotation.Nullable;
import android.util.DisplayMetrics;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import java.io.InputStream;

import l.files.fs.Path;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Color.WHITE;

final class SvgThumbnailer implements Thumbnailer<InputStream> {

    @Override
    public boolean accepts(Path path, String mediaType) {
        return mediaType.equals("image/svg+xml");
    }

    @Override
    public ScaledBitmap create(InputStream input, Rect max, Context context)
            throws Exception {
        SVG svg = parseSvg(input);
        if (svg == null) {
            return null;
        }
        Rect originalSize = getSize(svg);
        Rect scaledSize = originalSize.scaleDown(max);
        return new ScaledBitmap(
                render(svg, scaledSize, context),
                originalSize
        );
    }

    @Nullable
    private static SVG parseSvg(InputStream input) throws SVGParseException {
        SVG svg = SVG.getFromInputStream(input);
        if (svg == null ||
                svg.getDocumentWidth() < 1 ||
                svg.getDocumentHeight() < 1) {
            return null;
        }
        return svg;
    }

    private static Rect getSize(SVG svg) {
        return Rect.of(
                (int) svg.getDocumentWidth(),
                (int) svg.getDocumentHeight()
        );
    }

    private static Bitmap render(SVG svg, Rect size, Context context) {
        svg.setDocumentWidth(size.width());
        svg.setDocumentHeight(size.height());
        Bitmap bitmap = createBitmap(size, context);
        return render(svg, bitmap);
    }

    private static Bitmap render(SVG source, Bitmap destination) {
        Canvas canvas = new Canvas(destination);
        source.renderToCanvas(canvas);
        return destination;
    }

    private static Bitmap createBitmap(Rect size, Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        Bitmap bitmap = Bitmap.createBitmap(
                size.width(),
                size.height(),
                ARGB_8888
        );
        bitmap.setDensity(metrics.densityDpi);
        bitmap.eraseColor(WHITE);
        return bitmap;
    }
}
