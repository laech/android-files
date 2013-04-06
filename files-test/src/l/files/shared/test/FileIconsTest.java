package l.files.shared.test;

import static android.os.Environment.getExternalStorageDirectory;

import java.io.File;

import android.test.AndroidTestCase;

public class FileIconsTest extends AndroidTestCase {

  public void testCreateFiles() throws Exception {
    File dir = new File(getExternalStorageDirectory(), "test");
    dir.mkdirs();
    new File(dir, "audio.mp3").createNewFile();
    new File(dir, "archive.zip").createNewFile();
    new File(dir, "image.png").createNewFile();
    new File(dir, "text.txt").createNewFile();
    new File(dir, "video.mp4").createNewFile();
    new File(dir, "pdf.pdf").createNewFile();
  }
}
