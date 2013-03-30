package com.example.files.util;

import java.io.File;
import java.io.FilenameFilter;

public final class FileFilters {

  public static final FilenameFilter HIDE_HIDDEN_FILES = new FilenameFilter() {
    @Override public boolean accept(File dir, String filename) {
      return !filename.startsWith(".");
    }
  };

  private FileFilters() {
  }
}
