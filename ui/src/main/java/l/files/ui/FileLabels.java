package l.files.ui;

import android.content.res.Resources;
import android.os.Build;

import l.files.ui.R;
import l.files.fs.File;

import static l.files.ui.UserDirs.DIR_HOME;
import static l.files.ui.UserDirs.DIR_ROOT;

public final class FileLabels {

    private FileLabels() {
    }

    public static String get(final Resources res, final File file) {
        if (DIR_HOME.equals(file)) return res.getString(R.string.home);
        if (DIR_ROOT.equals(file)) return Build.MODEL;
        return file.name().toString();
    }

}
