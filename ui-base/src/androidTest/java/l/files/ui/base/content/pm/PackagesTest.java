package l.files.ui.base.content.pm;

import android.content.pm.PackageManager;

import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import l.files.base.Consumer;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static android.support.test.InstrumentationRegistry.getContext;
import static android.test.MoreAsserts.assertNotEqual;
import static java.io.File.createTempFile;
import static l.files.ui.base.content.pm.Packages.getApkIconBitmap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public final class PackagesTest {

    @Test
    public void getApkIconDrawable() throws Exception {
        testGetApkIcon(new Consumer<File>() {
            @Override
            public void accept(File file) {
                String path = file.getPath();
                assertNotNull(Packages.getApkIconDrawable(path, getPackageManager()));
            }
        });
    }

    @Test
    public void getApkIconBitmap_no_scale_needed() throws Exception {
        testGetApkIcon(new Consumer<File>() {
            @Override
            public void accept(File file) {
                String path = file.getPath();
                Rect max = Rect.of(Integer.MAX_VALUE, Integer.MAX_VALUE);
                assertNotNull(getApkIconBitmap(path, max, getPackageManager()));
            }
        });
    }

    @Test
    public void getApkIconBitmap_scale_to_fit() throws Exception {
        testGetApkIcon(new Consumer<File>() {
            @Override
            public void accept(File file) {
                String path = file.getPath();
                Rect max = Rect.of(1, 1);
                ScaledBitmap result = getApkIconBitmap(path, max, getPackageManager());
                assertNotNull(result);
                assertNotEqual(max, result);
                assertEquals(max, Rect.of(result.bitmap()));
            }
        });
    }

    public void testGetApkIcon(Consumer<File> test) throws IOException {
        File file = createTempFile("PackagesTest", null);
        try {
            copyApkFile(file);
            test.accept(file);
        } finally {
            assertTrue(file.delete() || !file.exists());
        }
    }

    private PackageManager getPackageManager() {
        return getContext().getPackageManager();
    }

    private void copyApkFile(File dst) throws IOException {
        InputStream in = openTestApk();
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                byte[] buffer = new byte[1024];
                int count;
                while ((count = in.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    private InputStream openTestApk() throws IOException {
        return getContext().getAssets().open("test.apk");
    }

}
