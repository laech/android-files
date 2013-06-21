package l.files.trash;

import android.test.AndroidTestCase;

import java.io.File;
import java.io.IOException;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.write;
import static java.lang.System.nanoTime;
import static org.apache.commons.io.FileUtils.deleteDirectory;

public final class TrashHelperTest extends AndroidTestCase {

  private File internalTrashDir;
  private File externalTrashDir;

  private TrashHelper helper;

  @Override protected void setUp() throws Exception {
    super.setUp();
    internalTrashDir = new File(getCacheDir(), randomName());
    externalTrashDir = new File(getExternalCacheDir(), randomName());
    helper = new TrashHelper(internalTrashDir, externalTrashDir);
  }

  private String randomName() {
    return String.valueOf(nanoTime());
  }

  private File getExternalCacheDir() {
    return getContext().getExternalCacheDir();
  }

  private File getCacheDir() {
    return getContext().getCacheDir();
  }

  @Override protected void tearDown() throws Exception {
    super.tearDown();
    deleteDirectory(internalTrashDir);
    deleteDirectory(externalTrashDir);
  }

  public void testMovesExternalFileToExternalTrash() throws Exception {
    testMovesFileToTrash(getExternalCacheDir(), externalTrashDir);
  }

  public void testMovesInternalFileToInternalTrash() throws Exception {
    testMovesFileToTrash(getCacheDir(), internalTrashDir);
  }

  private void testMovesFileToTrash(File fileDir, File trashDir) throws IOException {
    File file = createTestFile(fileDir);
    testMovesToTrash(file, trashDir);
  }

  private File createTestFile(File parentDir) throws IOException {
    File file = new File(parentDir, randomName());
    assertTrue(file.createNewFile());
    return file;
  }

  public void testMovesExternalDirectoryToExternalTrash() throws Exception {
    testMovesDirectoryToTrash(getExternalCacheDir(), externalTrashDir);
  }

  public void testMovesInternalDirectoryToInternalTrash() throws Exception {
    testMovesDirectoryToTrash(getCacheDir(), internalTrashDir);
  }

  private void testMovesDirectoryToTrash(File fileDir, File trashDir) throws IOException {
    File dir = createTestDirectory(fileDir);
    testMovesToTrash(dir, trashDir);
  }

  private File createTestDirectory(File parentDir) throws IOException {
    File dir = new File(parentDir, randomName());
    assertTrue(dir.mkdir());
    write("blah", new File(dir, "0"), UTF_8);
    assertTrue(new File(dir, "1").createNewFile());
    assertTrue(new File(dir, "2").mkdir());
    return dir;
  }

  private void testMovesToTrash(File fileToDelete, File trashDir) throws IOException {
    assertTrue(fileToDelete.exists());
    helper.moveToTrash(fileToDelete);

    assertFalse(fileToDelete.exists());
    assertTrue(new File(trashDir, fileToDelete.getName()).exists());
  }

}
