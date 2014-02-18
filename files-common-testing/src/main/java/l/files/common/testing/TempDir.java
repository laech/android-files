package l.files.common.testing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.io.File.createTempFile;
import static java.lang.System.nanoTime;
import static junit.framework.Assert.assertTrue;

public final class TempDir {

  public static TempDir create() {
    try {
      File file = createTempFile("test", null);
      assertTrue(file.delete());
      assertTrue(file.mkdir());
      return new TempDir(file);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static TempDir use(File directory) {
    assertTrue(directory.isDirectory());
    return new TempDir(directory);
  }

  private final File dir;

  private TempDir(File dir) {
    this.dir = dir;
  }

  public void delete() {
    delete(dir);
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  private void delete(File file) {
    if (file.isDirectory()) {
      file.setExecutable(true, true);
    }
    file.setReadable(true, true);
    File[] children = file.listFiles();
    if (children != null) {
      for (File child : children) {
        delete(child);
      }
    }
    file.delete();
  }

  @Deprecated
  public File get() {
    return dir;
  }

  /**
   * Gets the roo directory of this instance.
   */
  public File root(){
    return dir;
  }

  /**
   * Gets the file at the given path relative to {@link #root()}. The returned
   * file may or may not exists.
   */
  public File get(String path) {
    return new File(get(), path);
  }

  public List<File> newFiles(String... names) {
    List<File> files = new ArrayList<>(names.length);
    for (String name : names) {
      files.add(newFile(name));
    }
    return files;
  }

  public File newFile() {
    return newFile(String.valueOf(nanoTime()));
  }

  public File newFile(String name) {
    final File file = new File(dir, name);
    final File parent = file.getParentFile();
    assertTrue(parent.exists() || parent.mkdirs());
    try {
      assertTrue(file.createNewFile() || file.isFile());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    assertTrue(file.isFile());
    return file;
  }

  public File newDirectory() {
    return newDirectory(String.valueOf(nanoTime()));
  }

  public File newDirectory(String name) {
    final File file = new File(dir, name);
    assertTrue(file.mkdirs() || file.isDirectory());
    return file;
  }
}
