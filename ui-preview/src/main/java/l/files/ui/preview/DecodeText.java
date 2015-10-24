package l.files.ui.preview;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.widget.TextView;

import java.io.IOException;

import l.files.fs.File;
import l.files.fs.MediaTypes;
import l.files.fs.Stat;

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

final class DecodeText extends DecodeThumbnail {

    static final Previewer PREVIEWER = new Previewer() {

        @Override
        public boolean accept(File file, String mediaType) {
            return MediaTypes.generalize(mediaType).startsWith("text/");
        }

        @Override
        public Decode create(
                File res,
                Stat stat,
                Rect constraint,
                PreviewCallback callback,
                Preview context) {
            return new DecodeText(res, stat, constraint, callback, context);
        }

    };

    private static final int PREVIEW_LIMIT = 256;
    private static final int TEXT_COLOR = parseColor("#616161");

    private final int padding;
    private final int size;

    DecodeText(
            File res,
            Stat stat,
            Rect constraint,
            PreviewCallback callback,
            Preview context) {
        super(res, stat, constraint, callback, context);

        padding = (int) applyDimension(COMPLEX_UNIT_DIP, 8, context.displayMetrics);
        size = Math.min(constraint.width(), constraint.height());
    }

    @Override
    AsyncTask<Object, Object, Object> executeOnPreferredExecutor() {
        return execute(THREAD_POOL_EXECUTOR);
    }

    @Override
    Result decode() throws IOException {
        String text = file.readDetectingCharset(PREVIEW_LIMIT);
        Bitmap bitmap = draw(text);
        return new Result(bitmap, Rect.of(
                bitmap.getWidth(),
                bitmap.getHeight()
        ));
    }

    private Bitmap draw(String text) {
        TextView view = new TextView(context.context);
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
                context.displayMetrics,
                size,
                view.getMeasuredHeight(),
                ARGB_8888
        );
        view.draw(new Canvas(bitmap));
        return bitmap;
    }

}
