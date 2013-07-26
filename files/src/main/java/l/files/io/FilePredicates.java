package l.files.io;

import com.google.common.base.Predicate;

import java.io.File;

public final class FilePredicates {

  public static Predicate<File> canRead() {
    return FilePredicate.CAN_READ;
  }

  public static Predicate<File> exists() {
    return FilePredicate.EXISTS;
  }

  private FilePredicates() {}

}
