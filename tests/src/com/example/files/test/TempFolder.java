package com.example.files.test;

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

  public void delete() {
    try {
      deleteDirectory(folder);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public File get() {
    return folder;
  }

  public File newFile() {
    return newFile(String.valueOf(nanoTime()));
  }

  public File newFile(String name) {
    File file = new File(folder, name);
    try {
      touch(file);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
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
