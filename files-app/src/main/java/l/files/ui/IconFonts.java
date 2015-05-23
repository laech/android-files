package l.files.ui;

import android.content.res.AssetManager;
import android.graphics.Typeface;

import com.google.common.collect.ImmutableMap;
import com.google.common.net.MediaType;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import l.files.fs.Resource;

import static android.graphics.Typeface.createFromAsset;
import static l.files.ui.UserDirs.DIR_ALARMS;
import static l.files.ui.UserDirs.DIR_ANDROID;
import static l.files.ui.UserDirs.DIR_DCIM;
import static l.files.ui.UserDirs.DIR_DOWNLOADS;
import static l.files.ui.UserDirs.DIR_HOME;
import static l.files.ui.UserDirs.DIR_MOVIES;
import static l.files.ui.UserDirs.DIR_MUSIC;
import static l.files.ui.UserDirs.DIR_NOTIFICATIONS;
import static l.files.ui.UserDirs.DIR_PICTURES;
import static l.files.ui.UserDirs.DIR_PODCASTS;
import static l.files.ui.UserDirs.DIR_RINGTONES;
import static l.files.ui.UserDirs.DIR_ROOT;

public final class IconFonts
{

    private static Typeface iconDirectory;
    private static Map<Resource, Typeface> iconByDirectoryUri;

    private static Typeface iconFile;
    private static Typeface iconAudio;
    private static Typeface iconArchive;
    private static Typeface iconVideo;
    private static Typeface iconImage;
    private static Typeface iconText;
    private static Typeface iconPdf;
    private static Set<String> archiveSubtypes;

    public static Typeface getDirectoryIcon(
            final AssetManager assets,
            final Resource resource)
    {
        init(assets);
        final Typeface icon = iconByDirectoryUri.get(resource);
        return (icon != null) ? icon : iconDirectory;
    }

    public static Typeface getIconForFileMediaType(
            final AssetManager assets,
            final MediaType mime)
    {
        init(assets);
        if (mime == null) return iconFile;
        if (mime.subtype().equals("pdf")) return iconPdf;
        if (mime.type().equals("audio")) return iconAudio;
        if (mime.type().equals("video")) return iconVideo;
        if (mime.type().equals("image")) return iconImage;
        if (mime.type().equals("text")) return iconText;
        if (archiveSubtypes.contains(mime.subtype())) return iconArchive;
        return iconFile;
    }

    public static Typeface getDefaultFileIcon(final AssetManager assets)
    {
        init(assets);
        return iconFile;
    }

    private static void init(final AssetManager assets)
    {
        if (iconByDirectoryUri != null)
        {
            return;
        }

        iconArchive = createFromAsset(assets, "ic_file_archive.ttf");
        iconAudio = createFromAsset(assets, "ic_file_audio.ttf");
        iconVideo = createFromAsset(assets, "ic_file_video.ttf");
        iconImage = createFromAsset(assets, "ic_file_image.ttf");
        iconText = createFromAsset(assets, "ic_file_text.ttf");
        iconPdf = createFromAsset(assets, "ic_file_pdf.ttf");

        archiveSubtypes = new HashSet<>();
        archiveSubtypes.add("zip");
        archiveSubtypes.add("x-gzip");
        archiveSubtypes.add("x-tar");
        archiveSubtypes.add("x-rar-compressed");
        archiveSubtypes.add("x-ace-compressed");
        archiveSubtypes.add("x-bzip2");
        archiveSubtypes.add("x-compress");
        archiveSubtypes.add("x-7z-compressed");

        iconDirectory = createFromAsset(assets, "ic_dir.ttf");
        iconFile = createFromAsset(assets, "ic_file.ttf");
        iconByDirectoryUri = ImmutableMap.<Resource, Typeface>builder()
                .put(DIR_ROOT, createFromAsset(assets, "ic_dir_device.ttf"))
                .put(DIR_HOME, createFromAsset(assets, "ic_dir_home.ttf"))
                .put(DIR_DCIM, createFromAsset(assets, "ic_dir_dcim.ttf"))
                .put(DIR_MUSIC, createFromAsset(assets, "ic_dir_music.ttf"))
                .put(DIR_ALARMS, createFromAsset(assets, "ic_dir_alarms.ttf"))
                .put(DIR_MOVIES, createFromAsset(assets, "ic_dir_movies.ttf"))
                .put(DIR_ANDROID, createFromAsset(assets, "ic_dir_android.ttf"))
                .put(DIR_PICTURES, createFromAsset(assets, "ic_dir_pictures.ttf"))
                .put(DIR_PODCASTS, createFromAsset(assets, "ic_dir_podcasts.ttf"))
                .put(DIR_DOWNLOADS, createFromAsset(assets, "ic_dir_download.ttf"))
                .put(DIR_RINGTONES, createFromAsset(assets, "ic_dir_ringtones.ttf"))
                .put(DIR_NOTIFICATIONS, createFromAsset(assets, "ic_dir_notifications.ttf"))
                .build();
    }

    private IconFonts()
    {
    }

}
