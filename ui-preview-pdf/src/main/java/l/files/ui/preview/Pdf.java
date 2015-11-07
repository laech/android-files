package l.files.ui.preview;

import android.graphics.Bitmap;

import java.io.IOException;

/**
 * Access to this class must be synchronized globally,
 * as the underlying lib is not thread safe.
 */
final class Pdf {

    static {
        System.loadLibrary("previewpdf");
        init();
    }

    private static native void init();

    static synchronized native long open(String path) throws IOException;

    static synchronized native long openPage(long doc, int i) throws IOException;

    static synchronized native void close(long doc) throws IOException;

    static synchronized native void closePage(long page) throws IOException;

    static synchronized native double getPageWidthInPoints(long page) throws IOException;

    static synchronized native double getPageHeightInPoints(long page) throws IOException;

    static synchronized native void render(long page, Bitmap bitmap) throws IOException;

}
