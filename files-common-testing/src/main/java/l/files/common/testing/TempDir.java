package l.files.common.testing;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.io.Files.createTempDir;
import static com.google.common.io.Files.touch;
import static java.lang.System.nanoTime;
import static junit.framework.Assert.assertTrue;

public final class TempDir {

  public static TempDir create() {
    return new TempDir(createTempDir());
  }

  private final File mDir;

  private TempDir(File dir) {
    this.mDir = dir;
  }

  public void delete() {
    delete(mDir);
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

  public File get() {
    return mDir;
  }

  public List<File> newFiles(String... names) {
    List<File> files = newArrayListWithCapacity(names.length);
    for (String name : names) {
      files.add(newFile(name));
    }
    return files;
  }

  public File newFile() {
    return newFile(String.valueOf(nanoTime()));
  }

  public File newFile(String name) {
    final File file = new File(mDir, name);
    final File parent = file.getParentFile();
    assertTrue(parent.exists() || parent.mkdirs());
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
    final File file = new File(mDir, name);
    assertTrue(file.mkdirs() || file.isDirectory());
    return file;
  }
}
