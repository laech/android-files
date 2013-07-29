package l.files.io;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import com.google.common.base.Function;
import com.google.common.base.Predicate;

import java.io.File;
import java.io.FilenameFilter;

public final class Files {

  private static final FilenameFilter HIDE_HIDDEN_FILES = new FilenameFilter() {
    @Override public boolean accept(File dir, String filename) {
      return !filename.startsWith(".");
    }
  };

  private Files() {}

  public static Function<File, String> name() {
    return FileNameFunction.INSTANCE;
  }

  public static Predicate<File> canRead() {
    return FilePredicate.CAN_READ;
  }

  public static Predicate<File> exists() {
    return FilePredicate.EXISTS;
  }

  /**
   * @see File#listFiles()
   */
  public static File[] listFiles(File dir, boolean showHiddenFiles) {
    return dir.listFiles(showHiddenFiles ? null : HIDE_HIDDEN_FILES);
  }

  /**
   * Function to return the label of the file - usually the name of the file,
   * but some special directories will have different labels than their name
   * when displayed in special places like the sidebar or activity title.
   */
  public static Function<File, String> label(Resources res) {
    return new FileLabelFunction(res);
  }

  /**
   * Function to return a short summary about the file.
   *
   * @param dateFormat function to format {@link java.io.File#lastModified()}
   * @param sizeFormat function to format {@link java.io.File#length()}
   */
  public static Function<File, String> summary(
      Resources res,
      Function<? super Long, ? extends CharSequence> dateFormat,
      Function<? super Long, ? extends CharSequence> sizeFormat) {
    return new FileSummaryFunction(res, dateFormat, sizeFormat);
  }

  /**
   * Function to return a drawable icon for the file.
   */
  public static Function<File, Drawable> drawable(Resources res) {
    return new FileDrawableFunction(res);
  }
}
