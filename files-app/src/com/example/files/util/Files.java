package com.example.files.util;

import java.io.File;

public final class Files {

  /**
   * Gets the file extension of the file, or "" if no extension.
   */
  public static String getFileExtension(File file) {
    String name = file.getName();
    int dotIndex = name.lastIndexOf('.');
    return dotIndex == -1 ? "" : name.substring(dotIndex + 1);
  }

  private Files() {
  }
}
