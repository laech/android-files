package l.files.ui.browser;

import android.annotation.TargetApi;
import android.graphics.Bitmap;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static android.graphics.Bitmap.CompressFormat.JPEG;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Environment.getExternalStorageDirectory;
import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertTrue;
import static l.files.base.Throwables.addSuppressed;

public final class TakeScreenShotOnFailure extends TestWatcher {

    @Override
    protected void failed(Throwable e, Description desc) {
        super.failed(e, desc);

        if (SDK_INT >= JELLY_BEAN_MR2) {
            takeScreenshot(e, desc);
        }
    }

    @TargetApi(JELLY_BEAN_MR2)
    private void takeScreenshot(Throwable e, Description desc) {

        File file = screenshotFile(desc);
        File parent = file.getParentFile();
        assertTrue(parent.isDirectory() || parent.mkdir());

        Bitmap screenshot = takeScreenshot();
        OutputStream out = null;
        try {

            out = new FileOutputStream(file);
            screenshot.compress(JPEG, 90, out);

        } catch (IOException e1) {
            addSuppressed(e, e1);

        } finally {
            screenshot.recycle();
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e1) {
                    addSuppressed(e, e1);
                }
            }
        }
    }

    @TargetApi(JELLY_BEAN_MR2)
    private Bitmap takeScreenshot() {
        return getInstrumentation().getUiAutomation().takeScreenshot();
    }

    private File screenshotFile(Description desc) {
        String name = screenshotFilename(desc);
        return new File(getExternalStorageDirectory(), "test/" + name);
    }

    private String screenshotFilename(Description desc) {
        return desc.getClassName() + "." + desc.getMethodName() + ".jpg";
    }

}
