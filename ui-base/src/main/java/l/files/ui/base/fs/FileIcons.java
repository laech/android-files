package l.files.ui.base.fs;

import androidx.annotation.DrawableRes;

import l.files.fs.Path;

import static l.files.ui.base.R.drawable.ic_file_download_black_24dp;
import static l.files.ui.base.R.drawable.ic_folder_black_24dp;
import static l.files.ui.base.R.drawable.ic_home_black_24dp;
import static l.files.ui.base.R.drawable.ic_insert_drive_file_black_24dp;
import static l.files.ui.base.R.drawable.ic_library_music_black_24dp;
import static l.files.ui.base.R.drawable.ic_phone_android_black_24dp;
import static l.files.ui.base.R.drawable.ic_photo_library_black_24dp;
import static l.files.ui.base.R.drawable.ic_sd_storage_black_24dp;
import static l.files.ui.base.R.drawable.ic_video_library_black_24dp;
import static l.files.ui.base.fs.UserDirs.DIR_DCIM;
import static l.files.ui.base.fs.UserDirs.DIR_DOWNLOADS;
import static l.files.ui.base.fs.UserDirs.DIR_HOME;
import static l.files.ui.base.fs.UserDirs.DIR_MOVIES;
import static l.files.ui.base.fs.UserDirs.DIR_MUSIC;
import static l.files.ui.base.fs.UserDirs.DIR_PICTURES;
import static l.files.ui.base.fs.UserDirs.DIR_ROOT;
import static l.files.ui.base.fs.UserDirs.DIR_SDCARD2;

public final class FileIcons {

    private FileIcons() {
    }

    @DrawableRes
    public static int getDirectory(Path p) {
        if (p.equals(DIR_ROOT)) return ic_phone_android_black_24dp;
        if (p.equals(DIR_HOME)) return ic_home_black_24dp;
        if (p.equals(DIR_DCIM)) return ic_photo_library_black_24dp;
        if (p.equals(DIR_MUSIC)) return ic_library_music_black_24dp;
        if (p.equals(DIR_MOVIES)) return ic_video_library_black_24dp;
        if (p.equals(DIR_PICTURES)) return ic_photo_library_black_24dp;
        if (p.equals(DIR_DOWNLOADS)) return ic_file_download_black_24dp;
        if (p.equals(DIR_SDCARD2)) return ic_sd_storage_black_24dp;
        return ic_folder_black_24dp;
    }

    @DrawableRes
    public static int getFile() {
        return ic_insert_drive_file_black_24dp;
    }
}
