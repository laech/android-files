package l.files.base.content.pm;

import android.content.pm.PackageManager;
import android.test.AndroidTestCase;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import l.files.base.io.Closer;

import static l.files.base.content.pm.Packages.getApkIconBitmap;
import static l.files.base.content.pm.Packages.getApkIconDrawable;

public final class PackagesTest extends AndroidTestCase {

    public void test_getApkIconDrawable() throws Exception {
        Closer closer = Closer.create();
        try {

            File file = createTempFile(closer);
            copyApkFile(file);
            String path = file.getPath();
            assertNotNull(getApkIconDrawable(path, getPackageManager()));
            assertNotNull(getApkIconBitmap(path, getPackageManager()));

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    private PackageManager getPackageManager() {
        return getContext().getPackageManager();
    }

    private void copyApkFile(File dst) throws IOException {
        Closer closer = Closer.create();
        try {

            InputStream in = closer.register(openTestApk());
            OutputStream out = closer.register(new FileOutputStream(dst));
            byte[] buffer = new byte[1024];
            int count;
            while ((count = in.read(buffer)) != -1) {
                out.write(buffer, 0, count);
            }

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    private InputStream openTestApk() throws IOException {
        return getContext().getAssets().open("test.apk");
    }

    private File createTempFile(Closer closer) throws IOException {
        final File tmpFile = File.createTempFile("PackagesTest", null);
        closer.register(new Closeable() {
            @Override
            public void close() throws IOException {
                assertTrue(tmpFile.delete() || !tmpFile.exists());
            }
        });
        return tmpFile;
    }

}
