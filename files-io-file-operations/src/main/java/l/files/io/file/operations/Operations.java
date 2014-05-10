package l.files.io.file.operations;

import java.io.File;
import java.util.List;

import static java.util.Arrays.asList;

final class Operations {
  private Operations() {}

  public static List<File> listDirectoryChildren(File dir) throws NoReadException {
    File[] children = dir.listFiles();
    if (children == null) {
      throw new NoReadException(dir);
    }
    return asList(children);
  }
}
