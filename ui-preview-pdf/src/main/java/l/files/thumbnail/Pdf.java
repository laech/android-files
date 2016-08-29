package l.files.thumbnail;

import android.graphics.Bitmap;

import java.io.IOException;

/**
 * Access to this class must be synchronized globally,
 * as the underlying lib is not thread safe.
 */
final class Pdf {

    static {
        System.loadLibrary("pdfium");
        System.loadLibrary("previewpdf");
        init();
    }

    private static native void init();

    static native long open(byte[] path) throws IOException;

    static native long openPage(long doc, int i) throws IOException;

    static native void close(long doc) throws IOException;

    static native void closePage(long page) throws IOException;

    static native double getPageWidthInPoints(long page) throws IOException;

    static native double getPageHeightInPoints(long page) throws IOException;

    static native void render(long page, Bitmap bitmap) throws IOException;

}
