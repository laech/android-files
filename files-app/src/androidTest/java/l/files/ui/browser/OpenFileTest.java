package l.files.ui.browser;

import android.content.ContextWrapper;
import android.content.Intent;

import org.apache.tika.io.IOUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;

import l.files.fs.File;
import l.files.testing.fs.FileBaseTest;

import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class OpenFileTest extends FileBaseTest {

    public void testSendsCorrectIntentForApkFile() throws Exception {
        String testFile = "open_file_test.apk";
        File file = dir1().resolve(testFile);

        try (InputStream in = getTestContext().getAssets().open(testFile);
             OutputStream out = file.output()) {
            IOUtils.copy(in, out);
        }

        final CountDownLatch latch = new CountDownLatch(1);
        final String[] type = {null};
        ContextWrapper context = new ContextWrapper(getTestContext()) {
            @Override
            public void startActivity(Intent intent) {
                type[0] = intent.getType();
                latch.countDown();
            }
        };

        new OpenFile(context, file, file.stat(NOFOLLOW)).execute();
        assertTrue(latch.await(10, SECONDS));
        assertEquals("application/vnd.android.package-archive", type[0]);
    }

}