package l.files.app;

import android.content.res.Resources;
import android.os.Build;

import l.files.R;

import static l.files.app.UserDirs.DIR_HOME;
import static l.files.app.UserDirs.DIR_ROOT;
import static l.files.provider.FilesContract.getFileLocation;

final class FileLabels {

  private static final String HOME = getFileLocation(DIR_HOME);
  private static final String ROOT = getFileLocation(DIR_ROOT);

  private FileLabels() {}

  static String get(Resources res, String fileLocation, String name) {
    if (HOME.equals(fileLocation)) return res.getString(R.string.home);
    if (ROOT.equals(fileLocation)) return Build.MODEL;
    return name;
  }
}
