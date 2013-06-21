package l.files.test;

import java.io.File;
import java.io.IOException;

import static com.google.common.io.Files.createTempDir;
import static com.google.common.io.Files.touch;
import static java.lang.System.nanoTime;
import static junit.framework.Assert.assertTrue;

public final class TempDirectory {

  public static TempDirectory newTempDirectory() {
    return new TempDirectory(createTempDir());
  }

  private final File directory;

  private TempDirectory(File directory) {
    this.directory = directory;
  }

  public void delete() {
    delete(directory);
  }

  private void delete(File file) {
    if (file.isDirectory()) file.setExecutable(true, true);
    file.setReadable(true, true);
    File[] children = file.listFiles();
    if (children != null) {
      for (File child : children) {
        delete(child);
      }
    }
    file.delete();
  }

  public File get() {
    return directory;
  }

  public File newFile() {
    return newFile(String.valueOf(nanoTime()));
  }

  public File newFile(String name) {
    File file = new File(directory, name);
    try {
      touch(file);
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
    File file = new File(directory, name);
    assertTrue(file.mkdirs() || file.isDirectory());
    return file;
  }
}
