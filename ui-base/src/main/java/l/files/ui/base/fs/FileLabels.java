package l.files.ui.base.fs;

import android.content.res.Resources;
import android.os.Build;
import l.files.ui.base.R;

import java.nio.file.Path;

import static l.files.ui.base.fs.UserDirs.DIR_HOME;
import static l.files.ui.base.fs.UserDirs.DIR_ROOT;

public final class FileLabels {

    private FileLabels() {
    }

    public static String get(Resources res, Path file) {
        if (DIR_HOME.equals(file)) return res.getString(R.string.home);
        if (DIR_ROOT.equals(file)) return Build.MODEL;
        Path fileName = file.toAbsolutePath().getFileName();
        return fileName != null ? fileName.toString() : file.toString();
    }

}
