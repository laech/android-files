package l.files.ui;

import android.content.res.Resources;
import android.os.Build;

import l.files.R;
import l.files.fs.Resource;

import static l.files.ui.UserDirs.DIR_HOME;
import static l.files.ui.UserDirs.DIR_ROOT;

public final class FileLabels {

    private FileLabels() {
    }

    public static String get(Resources res, Resource resource) {
        if (DIR_HOME.equals(resource)) return res.getString(R.string.home);
        if (DIR_ROOT.equals(resource)) return Build.MODEL;
        return resource.name();
    }

}
