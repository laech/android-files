package com.example.files.util;

import java.io.File;

public final class Files {

  public static String getFileExtension(File file) {
    String name = file.getName();
    int dotIndex = name.lastIndexOf('.');
    return dotIndex == -1 ? null : name.substring(dotIndex + 1);
  }

  private Files() {}
}
