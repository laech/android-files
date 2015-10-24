package l.files.ui.base.fs;

import android.annotation.SuppressLint;
import android.os.Environment;

import l.files.fs.File;
import l.files.fs.local.LocalFile;

import static android.os.Environment.getExternalStorageDirectory;
import static android.os.Environment.getExternalStoragePublicDirectory;

public final class UserDirs {
    private UserDirs() {
    }

    @SuppressLint("SdCardPath")
    public static final File DIR_SDCARD2 = LocalFile.of("/sdcard2");
    public static final File DIR_ROOT = LocalFile.of("/");
    public static final File DIR_HOME = LocalFile.of(getExternalStorageDirectory());
    public static final File DIR_DCIM = dir(Environment.DIRECTORY_DCIM);
    public static final File DIR_DOWNLOADS = dir(Environment.DIRECTORY_DOWNLOADS);
    public static final File DIR_MOVIES = dir(Environment.DIRECTORY_MOVIES);
    public static final File DIR_MUSIC = dir(Environment.DIRECTORY_MUSIC);
    public static final File DIR_PICTURES = dir(Environment.DIRECTORY_PICTURES);

    private static File dir(final String type) {
        return LocalFile.of(getExternalStoragePublicDirectory(type));
    }

}
