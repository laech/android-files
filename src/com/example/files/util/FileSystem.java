package com.example.files.util;

import java.io.File;

public class FileSystem {

  public boolean hasPermissionToRead(File file) {
    return file.canRead() && (file.isDirectory() ? file.canExecute() : true);
  }
}
