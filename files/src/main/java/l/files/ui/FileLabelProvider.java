package l.files.ui;

import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.ui.UserDirs.DIR_HOME;
import static l.files.ui.UserDirs.DIR_ROOT;

import java.io.File;

import l.files.R;
import android.content.res.Resources;
import android.os.Build;

import com.google.common.base.Function;

public final class FileLabelProvider implements Function<File, String> {

  private final Resources res;

  public FileLabelProvider(Resources res) {
    this.res = checkNotNull(res, "res");
  }

  @Override public String apply(File file) {
    if (DIR_HOME.equals(file)) return res.getString(R.string.home);
    if (DIR_ROOT.equals(file)) return Build.MODEL;
    return file.getName();
  }

}
