package l.files.ui.base.fs;

import android.content.res.Resources;
import android.os.Build;

import l.files.fs.File;
import l.files.ui.base.R;

import static l.files.ui.base.fs.UserDirs.DIR_HOME;
import static l.files.ui.base.fs.UserDirs.DIR_ROOT;

public final class FileLabels {

    private FileLabels() {
    }

    public static String get(Resources res, File file) {
        if (DIR_HOME.equals(file)) return res.getString(R.string.home);
        if (DIR_ROOT.equals(file)) return Build.MODEL;
        return file.name().toString();
    }

}
