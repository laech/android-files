package l.files.ui;

import com.google.common.base.Function;

import java.io.File;

final class FileNameFunction implements Function<File, String> {

  @Override public String apply(File file) {
    return file.getName();
  }

}
