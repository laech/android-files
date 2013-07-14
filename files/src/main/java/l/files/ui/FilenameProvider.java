package l.files.ui;

import com.google.common.base.Function;

import java.io.File;

final class FileNameProvider implements Function<File, String> {

  @Override public String apply(File file) {
    return file.getName();
  }

}
