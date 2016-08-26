package l.files.base.content.pm;

import android.content.pm.PackageManager;
import android.test.AndroidTestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static java.io.File.createTempFile;
import static l.files.base.content.pm.Packages.getApkIconBitmap;
import static l.files.base.content.pm.Packages.getApkIconDrawable;

public final class PackagesTest extends AndroidTestCase {

    public void test_getApkIconDrawable() throws Exception {
        String path = copyApkFile().getPath();
        assertNotNull(getApkIconDrawable(path, getPackageManager()));
    }

    public void test_getApkIconBitmap() throws Exception {
        String path = copyApkFile().getPath();
        assertNotNull(getApkIconBitmap(path, getPackageManager()));
    }

    private PackageManager getPackageManager() {
        return getContext().getPackageManager();
    }

    private File copyApkFile() throws IOException {
        File tmpFile = createTempFile("PackagesTest", null);
        InputStream in = null;
        OutputStream out = null;
        try {
            out = new FileOutputStream(tmpFile);
            in = getContext().getAssets().open("test.apk");
            byte[] buffer = new byte[1024];
            int count;
            while ((count = in.read(buffer)) != -1) {
                out.write(buffer, 0, count);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
        return tmpFile;
    }
}
