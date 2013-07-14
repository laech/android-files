package l.files.io;

import com.google.common.base.Function;

import java.io.File;

public final class FileFunctions {

  /**
   * Function to return the name of the file.
   */
  public static Function<File, String> name() {
    return FileNameFunction.INSTANCE;
  }

  private FileFunctions() {}

}
