package l.files.common.io;

import com.google.common.base.Function;

import java.io.File;

enum FileNameFunction implements Function<File, String> {

  INSTANCE;

  @Override public String apply(File file) {
    return file.getName();
  }
}