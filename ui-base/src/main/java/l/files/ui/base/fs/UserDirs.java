package l.files.ui.base.fs;

import android.os.Environment;

import l.files.fs.Path;
import l.files.fs.Paths;

import static android.os.Environment.getExternalStorageDirectory;
import static android.os.Environment.getExternalStoragePublicDirectory;

public final class UserDirs {
    private UserDirs() {
    }

    public static final Path DIR_SDCARD2 = Paths.get("/sdcard2");
    public static final Path DIR_ROOT = Paths.get("/");
    public static final Path DIR_HOME = Paths.get(getExternalStorageDirectory());
    public static final Path DIR_DCIM = dir(Environment.DIRECTORY_DCIM);
    public static final Path DIR_DOWNLOADS = dir(Environment.DIRECTORY_DOWNLOADS);
    public static final Path DIR_MOVIES = dir(Environment.DIRECTORY_MOVIES);
    public static final Path DIR_MUSIC = dir(Environment.DIRECTORY_MUSIC);
    public static final Path DIR_PICTURES = dir(Environment.DIRECTORY_PICTURES);

    private static Path dir(String type) {
        return Paths.get(getExternalStoragePublicDirectory(type));
    }

}
