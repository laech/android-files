package l.files.thumbnail;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.DisplayMetrics;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

import l.files.base.io.Readers;
import l.files.fs.Path;
import l.files.fs.media.MediaTypes;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Bitmap.createBitmap;
import static android.graphics.Color.WHITE;
import static android.graphics.Color.parseColor;
import static android.graphics.Typeface.MONOSPACE;
import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.util.TypedValue.COMPLEX_UNIT_SP;
import static android.util.TypedValue.applyDimension;
import static android.view.View.MeasureSpec.AT_MOST;
import static android.view.View.MeasureSpec.UNSPECIFIED;
import static android.view.View.MeasureSpec.makeMeasureSpec;
import static l.files.base.io.Charsets.UTF_8;
import static java.lang.Math.min;

final class TextThumbnailer implements Thumbnailer<InputStream> {

    private static final int PREVIEW_LIMIT = 256;
    private static final int TEXT_COLOR = parseColor("#616161");

    @Override
    public boolean accepts(Path path, String mediaType) {
        return MediaTypes.generalize(mediaType).startsWith("text/");
    }

    @Override
    public ScaledBitmap create(InputStream input, Rect max, Context context)
            throws IOException {
        // TODO support more charsets
        String text = Readers.readString(input, PREVIEW_LIMIT, UTF_8);
        if (text == null) {
            return null;
        }
        Bitmap bitmap = draw(text, max, context);
        // TODO this returns bitmaps of different sizes and aspect ratio
        // when on portrait and on landscape, this causes problem since
        // the size of the bitmap is used as the originalSize and saved,
        // so when we are in portrait, the saved size is used which maybe
        // of different aspect ratio then the later loaded thumbnail causing
        // view to flicker
        return new ScaledBitmap(bitmap, Rect.of(bitmap));
    }

    private static Bitmap draw(String text, Rect max, Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int size = min(max.width(), max.height());
        int padding = (int) applyDimension(COMPLEX_UNIT_DIP, 8, metrics);
        TextView view = new TextView(context);
        view.setMaxLines(10);
        view.setLineSpacing(0, 1.1F);
        view.setBackgroundColor(WHITE);
        view.setTextColor(TEXT_COLOR);
        view.setTypeface(MONOSPACE);
        view.setTextSize(COMPLEX_UNIT_SP, 11);
        view.setPadding(padding, padding, padding, padding);
        view.setText(text.length() == PREVIEW_LIMIT ? text + "..." : text);
        view.measure(
                makeMeasureSpec(size, UNSPECIFIED),
                makeMeasureSpec(size, AT_MOST)
        );
        view.layout(0, 0, size, size);

        Bitmap bitmap = createBitmap(
                size,
                view.getMeasuredHeight(),
                ARGB_8888);
        view.draw(new Canvas(bitmap));
        return bitmap;
    }

}
