package l.files.ui.preview;

import android.graphics.Bitmap;

import java.io.IOException;

/**
 * Access to this class must be synchronized globally,
 * as the underlying lib is not thread safe.
 */
public final class Pdf {

    static {
        System.loadLibrary("pdfium");
        System.loadLibrary("previewpdf");
        init();
    }

    private static native void init();

    public static synchronized native long open(byte[] path) throws IOException;

    public static synchronized native long openPage(long doc, int i) throws IOException;

    public static synchronized native void close(long doc) throws IOException;

    public static synchronized native void closePage(long page) throws IOException;

    public static synchronized native double getPageWidthInPoints(long page) throws IOException;

    public static synchronized native double getPageHeightInPoints(long page) throws IOException;

    public static synchronized native void render(long page, Bitmap bitmap) throws IOException;

}
