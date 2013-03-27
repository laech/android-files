package com.example.files.event;

import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;

public final class FileSelectedEvent {

  private final File file;

  public FileSelectedEvent(File file) {
    this.file = checkNotNull(file, "file");
  }

  public File file() {
    return file;
  }

  @Override public int hashCode() {
    return file().hashCode();
  }

  @Override public String toString() {
    return toStringHelper(this).addValue(file()).toString();
  }

  @Override public boolean equals(Object o) {
    if (o instanceof FileSelectedEvent) {
      FileSelectedEvent that = (FileSelectedEvent) o;
      return file().equals(that.file());
    }
    return false;
  }
}