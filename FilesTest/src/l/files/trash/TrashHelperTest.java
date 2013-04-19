package l.files.trash;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.createTempDir;
import static com.google.common.io.Files.write;
import static java.io.File.createTempFile;
import static l.files.test.TempDirectory.newTempDirectory;

import java.io.File;
import java.io.IOException;

import android.test.AndroidTestCase;
import l.files.test.TempDirectory;

public final class TrashHelperTest extends AndroidTestCase {

  private TempDirectory trashDir;
  private TrashHelper helper;

  @Override protected void setUp() throws Exception {
    super.setUp();
    trashDir = newTempDirectory();
    helper = new TrashHelper(trashDir.get(), trashDir.get());
  }

  @Override protected void tearDown() throws Exception {
    super.tearDown();
    trashDir.delete();
  }

  public void testMovesFileToTrash() throws Exception {
    testMovesToTrash(createTempFile("temp", "test"));
  }

  public void testMovesDirectoryToTrash() throws Exception {
    File dir = createTempDir();
    write("blah", new File(dir, "0"), UTF_8);
    assertTrue(new File(dir, "1").createNewFile());
    assertTrue(new File(dir, "2").mkdir());

    testMovesToTrash(dir);
  }

  private void testMovesToTrash(File file) throws IOException {
    assertTrue(file.exists());

    helper.moveToTrash(file);

    assertFalse(file.exists());
    assertTrue(new File(trashDir.get(), file.getName()).exists());
  }
}
