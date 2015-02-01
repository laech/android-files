package l.files.ui;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

import static android.os.Environment.getExternalStorageDirectory;

public final class IconTest extends TestCase {

  public void test() throws Exception {
    File dir = new File(getExternalStorageDirectory(), "test");
    assertTrue(dir.exists() || dir.mkdir());
    createFile(new File(dir, "html.html"));
    createFile(new File(dir, "mp3.mp3"));
    createFile(new File(dir, "mp4.mp4"));
    createFile(new File(dir, "pdf.pdf"));
    createFile(new File(dir, "png.png"));
    createFile(new File(dir, "zip.zip"));
  }

  private static void createFile(File file) throws IOException {
    assertTrue(file.isFile() || file.createNewFile());
  }
}
