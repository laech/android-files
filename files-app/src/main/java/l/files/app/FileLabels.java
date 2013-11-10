package l.files.app;

import android.content.res.Resources;
import android.os.Build;

import java.io.File;

import l.files.R;

import static android.os.Environment.getExternalStorageDirectory;
import static l.files.provider.FilesContract.getFileId;

final class FileLabels {

  private static final String HOME = getFileId(getExternalStorageDirectory());
  private static final String ROOT = getFileId(new File("/"));

  private FileLabels() {}

  static String get(Resources res, String fileId, String name) {
    if (HOME.equals(fileId)) return res.getString(R.string.home);
    if (ROOT.equals(fileId)) return Build.MODEL;
    return name;
  }
}
