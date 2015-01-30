package l.files.fs;

import java.io.File;

import l.files.fs.local.LocalPath;

public final class Paths {
  private Paths() {}

  public static Path get(File file) {
    return LocalPath.of(file);
  }
}
