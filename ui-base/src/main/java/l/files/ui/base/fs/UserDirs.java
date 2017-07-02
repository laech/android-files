package l.files.ui.base.fs;

import android.os.Environment;

import l.files.fs.Path;

import static android.os.Environment.getExternalStorageDirectory;
import static android.os.Environment.getExternalStoragePublicDirectory;

public final class UserDirs {
    private UserDirs() {
    }

    static final Path DIR_SDCARD2 = Path.of("/sdcard2");
    static final Path DIR_ROOT = Path.of("/");
    public static final Path DIR_HOME = Path.of(getExternalStorageDirectory());
    static final Path DIR_DCIM = dir(Environment.DIRECTORY_DCIM);
    static final Path DIR_DOWNLOADS = dir(Environment.DIRECTORY_DOWNLOADS);
    static final Path DIR_MOVIES = dir(Environment.DIRECTORY_MOVIES);
    static final Path DIR_MUSIC = dir(Environment.DIRECTORY_MUSIC);
    static final Path DIR_PICTURES = dir(Environment.DIRECTORY_PICTURES);

    private static Path dir(String type) {
        return Path.of(getExternalStoragePublicDirectory(type));
    }

}
