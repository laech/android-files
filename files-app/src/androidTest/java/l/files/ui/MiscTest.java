package l.files.ui;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

import static android.os.Environment.getExternalStorageDirectory;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

public final class MiscTest extends TestCase
{

    public void test() throws Exception
    {
        final File dir = new File(getExternalStorageDirectory(), "test");
        assertTrue(dir.exists() || dir.mkdir());
        createFile(new File(dir, ".nomedia"));
        createFile(new File(dir, "html.html"));
        createFile(new File(dir, "mp3.mp3"));
        createFile(new File(dir, "mp4.mp4"));
        createFile(new File(dir, "pdf.pdf"));
        createFile(new File(dir, "png.png"));
        createFile(new File(dir, "zip.zip"));

        createFile(new File(dir, "future"))
                .setLastModified(currentTimeMillis() + DAYS.toMillis(365));
        createFile(new File(dir, "future1"))
                .setLastModified(currentTimeMillis() + DAYS.toMillis(1));
        createFile(new File(dir, "future2"))
                .setLastModified(currentTimeMillis() + HOURS.toMillis(1));
        createFile(new File(dir, "future3"))
                .setLastModified(currentTimeMillis() + HOURS.toMillis(2));
        createFile(new File(dir, "future4"))
                .setLastModified(currentTimeMillis() + MINUTES.toMillis(2));
        createFile(new File(dir, "future5"))
                .setLastModified(currentTimeMillis() + SECONDS.toMillis(5));
    }

    private static File createFile(final File file) throws IOException
    {
        assertTrue(file.isFile() || file.createNewFile());
        return file;
    }
}
