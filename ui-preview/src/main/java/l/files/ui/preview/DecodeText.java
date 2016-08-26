package l.files.ui.preview;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;

import l.files.base.graphics.Rect;
import l.files.base.io.Closer;
import l.files.fs.Files;
import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.fs.media.MediaTypes;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Color.WHITE;
import static android.graphics.Color.parseColor;
import static android.graphics.Typeface.MONOSPACE;
import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.util.TypedValue.COMPLEX_UNIT_SP;
import static android.util.TypedValue.applyDimension;
import static android.view.View.MeasureSpec.AT_MOST;
import static android.view.View.MeasureSpec.UNSPECIFIED;
import static android.view.View.MeasureSpec.makeMeasureSpec;
import static l.files.fs.Files.UTF_8;
import static l.files.fs.media.MediaTypes.detectByFileExtension;

final class DecodeText extends DecodeThumbnail {

    static final Previewer PREVIEWER = new Previewer() {

        @Override
        public boolean acceptsFileExtension(Path path, String extensionInLowercase) {
            return acceptsMediaType(path, detectByFileExtension(extensionInLowercase));
        }

        @Override
        public boolean acceptsMediaType(Path path, String mediaTypeInLowercase) {
            return MediaTypes.generalize(mediaTypeInLowercase).startsWith("text/");
        }

        @Override
        public Decode create(
                Path path,
                Stat stat,
                Rect constraint,
                Preview.Callback callback,
                Preview.Using using,
                Preview context) {
            return new DecodeText(path, stat, constraint, callback, using, context);
        }

    };

    private static final int PREVIEW_LIMIT = 256;
    private static final int TEXT_COLOR = parseColor("#616161");

    private final int padding;
    private final int size;

    DecodeText(
            Path path,
            Stat stat,
            Rect constraint,
            Preview.Callback callback,
            Preview.Using using,
            Preview context) {
        super(path, stat, constraint, callback, using, context);

        padding = (int) applyDimension(COMPLEX_UNIT_DIP, 8, context.displayMetrics);
        size = Math.min(constraint.width(), constraint.height());
    }

    @Override
    Result decode() throws IOException {
        String text = readDetectingCharset(path, PREVIEW_LIMIT);
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

    static String readDetectingCharset(Path path, int limit) throws IOException {
        Closer closer = Closer.create();
        try {

            // TODO support more charsets
            InputStream in = closer.register(Files.newBufferedInputStream(path));
            CharsetDecoder decoder = UTF_8.newDecoder()
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .onMalformedInput(CodingErrorAction.REPORT);
            Reader reader = new InputStreamReader(in, decoder);
            char[] buffer = new char[limit];
            int count = reader.read(buffer);
            if (count != -1) {
                return new String(buffer, 0, count);
            }

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
        throw new UnknownCharsetException();
    }

    private static class UnknownCharsetException extends IOException {

    }
}
