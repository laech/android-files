package l.files.io;

import com.google.common.base.Predicate;

import java.io.File;

enum FilePredicate implements Predicate<File> {

  CAN_READ {
    @Override public boolean apply(File file) {
      return file.canRead();
    }
  }

}
