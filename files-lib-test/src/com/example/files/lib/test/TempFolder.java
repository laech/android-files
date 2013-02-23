package com.example.files.lib.test;

import static com.google.common.io.Files.createTempDir;
import static com.google.common.io.Files.touch;
import static java.lang.System.nanoTime;
import static org.apache.commons.io.FileUtils.deleteDirectory;

import java.io.File;
import java.io.IOException;

public final class TempFolder {

  public static TempFolder newTempFolder() {
    return new TempFolder(createTempDir());
  }

  private final File folder;

  private TempFolder(File folder) {
    this.folder = folder;
  }

  public void delete() throws IOException {
    deleteDirectory(folder);
  }

  public File get() {
    return folder;
  }

  public File newFile() throws IOException {
    return newFile(String.valueOf(nanoTime()));
  }

  public File newFile(String name) throws IOException {
    File file = new File(folder, name);
    touch(file);
    assert file.isFile();
    return file;
  }

  public File newFolder() {
    return newFolder(String.valueOf(nanoTime()));
  }

  public File newFolder(String name) {
    File file = new File(folder, name);
    file.mkdirs();
    assert file.isDirectory();
    return file;
  }
}
