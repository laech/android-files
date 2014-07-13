package l.files.common.testing;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.nanoTime;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public final class TempDir {

  public static TempDir create() {
    return create("test");
  }

  public static TempDir create(String prefix) {
    File parent = new File(System.getProperty("java.io.tmpdir"));
    File dir = new File(parent, prefix + nanoTime());
    assertFalse(dir.exists());
    assertTrue(dir.mkdir());
    return new TempDir(dir);
  }

  private final File dir;

  private TempDir(File dir) {
    this.dir = dir;
  }

  /**
   * Deletes the root directory.
   */
  public void delete() {
    delete(dir);
  }

  /**
   * Creates the root directory if it doesn't exists.
   */
  public void createRoot() {
    assertTrue(get().isDirectory() || get().mkdirs());
  }

  private void delete(File file) {
    if (!file.exists()) {
      return;
    }
    if (file.isDirectory() && !file.canExecute()) {
      assertTrue(file.setExecutable(true));
    }
    if (!file.canRead()) {
      assertTrue(file.setReadable(true));
    }
    File[] children = file.listFiles();
    if (children != null) {
      for (File child : children) {
        delete(child);
      }
    }
    if (!file.delete()) {
      if (file.exists()) {
        Log.w("TempDir", "Failed to delete " + file);
        // TODO find out why this fails when symlink is involved
      }
    }
  }

  /**
   * Gets the roo directory of this instance.
   */
  public File get() {
    return dir;
  }

  /**
   * Gets the file at the given path relative to the root. The returned file may
   * or may not exists.
   */
  public File get(String path) {
    return new File(get(), path);
  }

  @Deprecated
  public List<File> newFiles(String... names) {
    List<File> files = new ArrayList<>(names.length);
    for (String name : names) {
      files.add(newFile(name));
    }
    return files;
  }

  @Deprecated
  public File newFile() {
    return newFile(String.valueOf(nanoTime()));
  }

  /**
   * @deprecated use {@link #createDir(String)} instead
   */
  @Deprecated
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

  /**
   * Creates a new file and any of it's parents at the given path relative to
   * the root directory.
   */
  public File createFile(String path) {
    final File file = new File(dir, path);
    final File parent = file.getParentFile();
    assertTrue(parent.exists() || parent.mkdirs());
    try {
      assertTrue(file.createNewFile() || file.isFile());
      return file;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Deprecated
  public File newDirectory() {
    return newDirectory(String.valueOf(nanoTime()));
  }

  /**
   * @deprecated use {@link #createDir(String)}
   */
  @Deprecated
  public File newDirectory(String name) {
    final File file = new File(dir, name);
    assertTrue(file.mkdirs() || file.isDirectory());
    return file;
  }

  /**
   * Creates a new directory at the given path relative to the root directory.
   */
  public File createDir(String path) {
    File file = new File(dir, path);
    assertTrue(file.mkdirs() || file.isDirectory());
    return file;
  }
}
