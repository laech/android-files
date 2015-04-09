package l.files.ui;

import android.content.res.AssetManager;
import android.graphics.Typeface;

import com.google.common.collect.ImmutableMap;
import com.google.common.net.MediaType;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import l.files.R;
import l.files.fs.Path;
import l.files.fs.Resource;

import static android.graphics.Typeface.createFromAsset;

public final class IconFonts {

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

    private enum Icon {
        PDF(R.drawable.file_circle_pdf),
        AUDIO(R.drawable.file_circle_audio),
        VIDEO(R.drawable.file_circle_video),
        IMAGE(R.drawable.file_circle_image),
        ARCHIVE(R.drawable.file_circle_archive),
        DOCUMENT(R.drawable.file_circle_document),
        DEFAULT(R.drawable.file_circle);

        final int backgroundResourceId;

        Icon(int backgroundResourceId) {
            this.backgroundResourceId = backgroundResourceId;
        }

        static Icon get(MediaType mime) {
            if (mime.subtype().equals("pdf")) return Icon.PDF;
            if (mime.type().equals("audio")) return Icon.AUDIO;
            if (mime.type().equals("video")) return Icon.VIDEO;
            if (mime.type().equals("image")) return Icon.IMAGE;
            if (mime.type().equals("text")) return Icon.DOCUMENT;
            if (archiveSubtypes.contains(mime.subtype())) return Icon.ARCHIVE;
            return Icon.DEFAULT;
        }
    }

    public static int getDefaultBackgroundResource() {
        return Icon.DEFAULT.backgroundResourceId;
    }

    public static int getBackgroundResourceForFileMediaType(MediaType mime) {
        return Icon.get(mime).backgroundResourceId;
    }

    public static Typeface getDirectoryIcon(AssetManager assets, Path path) {
        init(assets);
        Typeface icon = iconByDirectoryUri.get(path);
        if (icon != null) return icon;
        return iconDirectory;
    }

    public static Typeface getIconForFileMediaType(AssetManager assets, MediaType mime) {
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

    public static Typeface getDefaultFileIcon(AssetManager assets) {
        init(assets);
        return iconFile;
    }

    private static void init(AssetManager assets) {
        if (iconByDirectoryUri != null) {
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
                .put(UserDirs.DIR_ROOT, createFromAsset(assets, "ic_dir_device.ttf"))
                .put(UserDirs.DIR_HOME, createFromAsset(assets, "ic_dir_home.ttf"))
                .put(UserDirs.DIR_DCIM, createFromAsset(assets, "ic_dir_dcim.ttf"))
                .put(UserDirs.DIR_MUSIC, createFromAsset(assets, "ic_dir_music.ttf"))
                .put(UserDirs.DIR_ALARMS, createFromAsset(assets, "ic_dir_alarms.ttf"))
                .put(UserDirs.DIR_MOVIES, createFromAsset(assets, "ic_dir_movies.ttf"))
                .put(UserDirs.DIR_ANDROID, createFromAsset(assets, "ic_dir_android.ttf"))
                .put(UserDirs.DIR_PICTURES, createFromAsset(assets, "ic_dir_pictures.ttf"))
                .put(UserDirs.DIR_PODCASTS, createFromAsset(assets, "ic_dir_podcasts.ttf"))
                .put(UserDirs.DIR_DOWNLOADS, createFromAsset(assets, "ic_dir_download.ttf"))
                .put(UserDirs.DIR_RINGTONES, createFromAsset(assets, "ic_dir_ringtones.ttf"))
                .put(UserDirs.DIR_NOTIFICATIONS, createFromAsset(assets, "ic_dir_notifications.ttf"))
                .build();
    }

    private IconFonts() {
    }

}
