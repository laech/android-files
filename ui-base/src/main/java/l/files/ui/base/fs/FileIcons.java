package l.files.ui.base.fs;

import android.content.res.AssetManager;
import android.graphics.Typeface;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import l.files.fs.Path;
import l.files.ui.base.R;

import static android.graphics.Typeface.createFromAsset;
import static java.util.Collections.unmodifiableMap;
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

    @Nullable
    private static Typeface font;

    private static final Map<Path, Integer> iconByDirectoryUri =
            buildIconByDirectoryUri();

    private static Map<Path, Integer> buildIconByDirectoryUri() {
        Map<Path, Integer> icons = new HashMap<>();
        icons.put(DIR_ROOT, R.string.ic_dir_device);
        icons.put(DIR_HOME, R.string.ic_dir_home);
        icons.put(DIR_DCIM, R.string.ic_dir_image);
        icons.put(DIR_MUSIC, R.string.ic_dir_music);
        icons.put(DIR_MOVIES, R.string.ic_dir_video);
        icons.put(DIR_PICTURES, R.string.ic_dir_image);
        icons.put(DIR_DOWNLOADS, R.string.ic_dir_download);
        icons.put(DIR_SDCARD2, R.string.ic_dir_sdcard2);
        return unmodifiableMap(icons);
    }

    public static Typeface font(AssetManager assets) {
        if (font == null) {
            font = createFromAsset(assets, "MaterialIcons-Regular.ttf");
        }
        return font;
    }

    public static int directoryIconStringId(Path dir) {
        Integer id = iconByDirectoryUri.get(dir);
        return id != null ? id : defaultDirectoryIconStringId();
    }

    public static int defaultDirectoryIconStringId() {
        return R.string.ic_dir;
    }

    private FileIcons() {
    }

    public static int getDirectoryIconDrawableResourceId(Path p) {
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

    public static int getFileIconDrawableResourceId() {
        return ic_insert_drive_file_black_24dp;
    }
}
