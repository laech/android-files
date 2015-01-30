package l.files.ui;

import android.content.res.Resources;
import android.os.Build;

import l.files.R;
import l.files.fs.Path;

import static l.files.ui.UserDirs.DIR_HOME;
import static l.files.ui.UserDirs.DIR_ROOT;

final class FileLabels {

  private FileLabels() {}

  static String get(Resources res, Path path) {
    if (DIR_HOME.equals(path)) return res.getString(R.string.home);
    if (DIR_ROOT.equals(path)) return Build.MODEL;
    return path.getName();
  }

}
