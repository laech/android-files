package l.files.app.format;

import android.content.res.Resources;
import android.os.Build;

import l.files.R;

import static l.files.app.UserDirs.DIR_HOME;
import static l.files.app.UserDirs.DIR_ROOT;
import static l.files.provider.FilesContract.getFileId;

public final class FileNames {

  private static final String DIR_HOME_ID = getFileId(DIR_HOME);
  private static final String DIR_ROOT_ID = getFileId(DIR_ROOT);

  private FileNames() {}

  public static String get(Resources res, String fileId, String defaultName) {
    if (DIR_HOME_ID.equals(fileId)) return res.getString(R.string.home);
    if (DIR_ROOT_ID.equals(fileId)) return Build.MODEL;
    return defaultName;
  }
}
