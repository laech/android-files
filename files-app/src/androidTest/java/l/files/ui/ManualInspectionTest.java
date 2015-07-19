package l.files.ui;

import android.test.InstrumentationTestCase;

import com.google.common.io.ByteStreams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static android.os.Environment.getExternalStorageDirectory;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.SECONDS;

public final class ManualInspectionTest extends InstrumentationTestCase
{

    public void test() throws Exception
    {
        final File dir = new File(getExternalStorageDirectory(), "test");
        assertTrue(dir.exists() || dir.mkdir());
        createFile(new File(dir, ".nomedia"));
        createFile(new File(dir, "html.html"));
        createFile(new File(dir, "zip.zip"));

        createFutureFiles(dir);

        final List<String> resources = asList(
                "will_scale_up.jpg",
                "will_scale_down.jpg",
                "test.pdf",
                "test.mp4",
                "test.m4a");

        for (final String res : resources)
        {
            final File file = new File(dir, res);
            if (file.exists())
            {
                continue;
            }
            try (InputStream in = getInstrumentation().getContext().getAssets().open(res);
                 OutputStream out = new FileOutputStream(file))
            {
                ByteStreams.copy(in, out);
            }
        }

    }

    private void createFutureFiles(final File dir) throws IOException
    {
        assertTrue(createFile(new File(dir, "future"))
                .setLastModified(currentTimeMillis() + DAYS.toMillis(365)));

        assertTrue(createFile(new File(dir, "future3"))
                .setLastModified(currentTimeMillis() + DAYS.toMillis(2)));

        assertTrue(createFile(new File(dir, "future5"))
                .setLastModified(currentTimeMillis() + SECONDS.toMillis(5)));
    }

    private static File createFile(final File file) throws IOException
    {
        assertTrue(file.isFile() || file.createNewFile());
        return file;
    }
}