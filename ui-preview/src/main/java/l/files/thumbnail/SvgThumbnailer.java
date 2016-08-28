package l.files.thumbnail;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.DisplayMetrics;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;

import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Bitmap.createBitmap;
import static android.graphics.Color.WHITE;

public final class SvgThumbnailer implements Thumbnailer<InputStream> {

    @Override
    public ScaledBitmap create(InputStream input, Rect max, Context context) throws Exception {
        SVG svg = parseSvg(input);
        if (svg == null) {
            return null;
        }
        Rect originalSize = getSize(svg);
        Rect scaledSize = originalSize.scaleDown(max);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        Bitmap bitmap = renderSvg(svg, scaledSize, metrics);
        return new ScaledBitmap(bitmap, originalSize);

    }

    @Nullable
    private static SVG parseSvg(InputStream input)
            throws IOException, SVGParseException {

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
                (int) svg.getDocumentHeight());
    }

    private static Bitmap renderSvg(SVG svg, Rect size, DisplayMetrics metrics) {
        svg.setDocumentWidth(size.width());
        svg.setDocumentHeight(size.height());

        Bitmap bitmap = createBitmap(size.width(), size.height(), ARGB_8888);
        bitmap.setDensity(metrics.densityDpi);
        bitmap.eraseColor(WHITE);

        Canvas canvas = new Canvas(bitmap);
        svg.renderToCanvas(canvas);
        return bitmap;
    }
}
