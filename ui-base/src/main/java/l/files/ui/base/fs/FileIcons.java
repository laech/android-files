package l.files.ui.base.fs;

import android.content.res.AssetManager;
import android.graphics.Typeface;

import java.util.HashMap;
import java.util.Map;

import l.files.fs.File;
import l.files.ui.base.R;

import static android.graphics.Typeface.createFromAsset;
import static java.util.Collections.unmodifiableMap;
import static l.files.ui.base.fs.UserDirs.DIR_DCIM;
import static l.files.ui.base.fs.UserDirs.DIR_DOWNLOADS;
import static l.files.ui.base.fs.UserDirs.DIR_HOME;
import static l.files.ui.base.fs.UserDirs.DIR_MOVIES;
import static l.files.ui.base.fs.UserDirs.DIR_MUSIC;
import static l.files.ui.base.fs.UserDirs.DIR_PICTURES;
import static l.files.ui.base.fs.UserDirs.DIR_ROOT;
import static l.files.ui.base.fs.UserDirs.DIR_SDCARD2;

public final class FileIcons {
    private static Typeface font;

    private static final Map<File, Integer> iconByDirectoryUri =
            buildIconByDirectoryUri();

    private static Map<File, Integer> buildIconByDirectoryUri() {
        Map<File, Integer> icons = new HashMap<>();
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

    private static boolean isArchive(String media) {
        return media.endsWith("/zip")
                || media.endsWith("/x-gzip")
                || media.endsWith("/x-tar")
                || media.endsWith("/x-rar-compressed")
                || media.endsWith("/x-ace-compressed")
                || media.endsWith("/x-bzip2")
                || media.endsWith("/x-compress")
                || media.endsWith("/x-7z-compressed");
    }

    public static Typeface font(AssetManager assets) {
        if (font == null) {
            font = createFromAsset(assets, "MaterialIcons-Regular.ttf");
        }
        return font;
    }

    public static int directoryIconStringId(File dir) {
        Integer id = iconByDirectoryUri.get(dir);
        return id != null ? id : defaultDirectoryIconStringId();
    }

    public static int defaultDirectoryIconStringId() {
        return R.string.ic_dir;
    }

    public static int fileIconStringId(String media) {
        if (media.equals("application/pdf")) return R.string.ic_file_pdf;
        if (media.startsWith("audio/")) return R.string.ic_file_music;
        if (media.startsWith("video/")) return R.string.ic_file_video;
        if (media.startsWith("image/")) return R.string.ic_file_image;
        if (media.startsWith("text/")) return R.string.ic_file_text;
        if (isArchive(media)) return R.string.ic_file_archive;
        return defaultFileIconStringId();
    }

    public static int defaultFileIconStringId() {
        return R.string.ic_file;
    }

    public static int unknownIconStringId() {
        return R.string.ic_unknown;
    }

    private FileIcons() {
    }

}
