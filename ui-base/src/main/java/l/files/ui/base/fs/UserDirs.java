package l.files.ui.base.fs;

import android.os.Environment;

import l.files.fs.File;
import l.files.fs.local.LocalFile;

import static android.os.Environment.getExternalStorageDirectory;
import static android.os.Environment.getExternalStoragePublicDirectory;

public final class UserDirs {
    private UserDirs() {
    }

    public static final File DIR_ROOT = LocalFile.create(new java.io.File("/"));
    public static final File DIR_HOME = LocalFile.create(getExternalStorageDirectory());
    public static final File DIR_DCIM = dir(Environment.DIRECTORY_DCIM);
    public static final File DIR_DOWNLOADS = dir(Environment.DIRECTORY_DOWNLOADS);
    public static final File DIR_MOVIES = dir(Environment.DIRECTORY_MOVIES);
    public static final File DIR_MUSIC = dir(Environment.DIRECTORY_MUSIC);
    public static final File DIR_PICTURES = dir(Environment.DIRECTORY_PICTURES);

    private static File dir(final String type) {
        return LocalFile.create(getExternalStoragePublicDirectory(type));
    }

}
