package l.files.assumptions;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

import static com.google.common.io.Files.createTempDir;
import static java.lang.System.nanoTime;
import static org.apache.commons.io.FileUtils.forceDelete;

public final class DirectoryPermissionTest extends TestCase {

  private File dir;

  @Override protected void setUp() throws Exception {
    super.setUp();
    dir = createTempDir();
  }

  @Override protected void tearDown() throws Exception {
    if (dir.exists()) {
      setReadable(dir, true);
      setExecutable(dir, true);
      setWritable(dir, true);
      forceDelete(dir);
    }
    super.tearDown();
  }

  public void testAssumption_canSeeDirContentWithOnlyReadPermission() {
    generateTempFile(dir);
    setReadable(dir, true);
    setExecutable(dir, false);
    setWritable(dir, false);
    assertNotNull(dir.list());
  }

  public void testAssumption_canNotSeeDirContentWithOnlyExecutePermission() {
    generateTempFile(dir);
    setReadable(dir, false);
    setExecutable(dir, true);
    setWritable(dir, false);
    assertNull(dir.list());
  }

  private void generateTempFile(File dir) {
    try {
      assertTrue(new File(dir, String.valueOf(nanoTime())).createNewFile());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void setExecutable(File file, boolean executable) {
    assertTrue(file.setExecutable(executable, executable));
  }

  private void setReadable(File file, boolean readable) {
    assertTrue(file.setReadable(readable, readable));
  }

  private void setWritable(File file, boolean writable) {
    assertTrue(file.setWritable(writable, writable));
  }

}
