package l.files.ui;

import android.content.res.AssetManager;
import android.graphics.Typeface;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.net.MediaType;

import java.util.Map;
import java.util.Set;

import l.files.fs.Resource;

import static android.graphics.Typeface.createFromAsset;
import static l.files.R.string.ic_dir;
import static l.files.R.string.ic_dir_device;
import static l.files.R.string.ic_dir_download;
import static l.files.R.string.ic_dir_home;
import static l.files.R.string.ic_dir_image;
import static l.files.R.string.ic_dir_music;
import static l.files.R.string.ic_dir_video;
import static l.files.R.string.ic_file;
import static l.files.R.string.ic_file_archive;
import static l.files.R.string.ic_file_image;
import static l.files.R.string.ic_file_music;
import static l.files.R.string.ic_file_pdf;
import static l.files.R.string.ic_file_text;
import static l.files.R.string.ic_file_video;
import static l.files.ui.UserDirs.DIR_DCIM;
import static l.files.ui.UserDirs.DIR_DOWNLOADS;
import static l.files.ui.UserDirs.DIR_HOME;
import static l.files.ui.UserDirs.DIR_MOVIES;
import static l.files.ui.UserDirs.DIR_MUSIC;
import static l.files.ui.UserDirs.DIR_PICTURES;
import static l.files.ui.UserDirs.DIR_ROOT;

public final class Icons
{
    private static Typeface font;

    private static final Map<Resource, Integer> iconByDirectoryUri =
            new ImmutableMap.Builder<Resource, Integer>()
                    .put(DIR_ROOT, ic_dir_device)
                    .put(DIR_HOME, ic_dir_home)
                    .put(DIR_DCIM, ic_dir_image)
                    .put(DIR_MUSIC, ic_dir_music)
                    .put(DIR_MOVIES, ic_dir_video)
                    .put(DIR_PICTURES, ic_dir_image)
                    .put(DIR_DOWNLOADS, ic_dir_download)
                    .build();

    private static final Set<String> archiveSubtypes =
            ImmutableSet.of(
                    "zip",
                    "x-gzip",
                    "x-tar",
                    "x-rar-compressed",
                    "x-ace-compressed",
                    "x-bzip2",
                    "x-compress",
                    "x-7z-compressed");

    public static Typeface font(final AssetManager assets)
    {
        if (font == null)
        {
            font = createFromAsset(assets, "icomoon.ttf");
        }
        return font;
    }

    public static int directoryIconStringId(final Resource res)
    {
        final Integer id = iconByDirectoryUri.get(res);
        return id != null ? id : ic_dir;
    }

    public static int fileIconStringId(final MediaType mime)
    {
        if (mime.subtype().equalsIgnoreCase("pdf")) return ic_file_pdf;
        if (mime.type().equalsIgnoreCase("audio")) return ic_file_music;
        if (mime.type().equalsIgnoreCase("video")) return ic_file_video;
        if (mime.type().equalsIgnoreCase("image")) return ic_file_image;
        if (mime.type().equalsIgnoreCase("text")) return ic_file_text;
        if (archiveSubtypes.contains(mime.subtype())) return ic_file_archive;
        return ic_file;
    }

    public static int defaultFileIconStringId()
    {
        return ic_file;
    }

    private Icons()
    {
    }

}
