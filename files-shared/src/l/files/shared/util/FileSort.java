package l.files.shared.util;

import java.io.File;
import java.util.Comparator;

public enum FileSort implements Comparator<File> {

  BY_NAME {
    @Override public int compare(File x, File y) {
      return x.getName().compareToIgnoreCase(y.getName());
    }
  };

}
