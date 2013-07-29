package l.files.io;

import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.io.UserDirs.DIR_HOME;
import static l.files.io.UserDirs.DIR_ROOT;

import java.io.File;

import l.files.R;
import android.content.res.Resources;
import android.os.Build;

import com.google.common.base.Function;

final class FileLabelFunction implements Function<File, String> {

  private final Resources res;

  FileLabelFunction(Resources res) {
    this.res = checkNotNull(res, "res");
  }

  @Override public String apply(File file) {
    if (DIR_HOME.equals(file)) return res.getString(R.string.home);
    if (DIR_ROOT.equals(file)) return Build.MODEL;
    return file.getName();
  }

}
