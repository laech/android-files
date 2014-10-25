package l.files.ui;

import android.content.res.Resources;
import android.os.Build;

import l.files.R;

import static l.files.provider.FilesContract.getFileId;

final class FileLabels {

  private static final String HOME = getFileId(UserDirs.DIR_HOME);
  private static final String ROOT = getFileId(UserDirs.DIR_ROOT);

  private FileLabels() {}

  static String get(Resources res, String fileId, String name) {
    if (HOME.equals(fileId)) return res.getString(R.string.home);
    if (ROOT.equals(fileId)) return Build.MODEL;
    return name;
  }
}
