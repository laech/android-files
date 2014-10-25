package l.files.ui;

import android.content.res.AssetManager;
import android.graphics.Typeface;

import com.google.common.collect.ImmutableMap;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static android.graphics.Typeface.createFromAsset;
import static l.files.provider.FilesContract.getFileId;

public final class IconFonts {

  private static Typeface iconDirectory;
  private static Map<String, Typeface> iconByDirectoryUri;

  private static Typeface iconFile;
  private static Typeface iconAudio;
  private static Typeface iconArchive;
  private static Typeface iconVideo;
  private static Typeface iconImage;
  private static Typeface iconText;
  private static Typeface iconPdf;
  private static Set<String> mimeArchive;

  public static Typeface forDirectoryLocation(
      AssetManager assets, String fileLocation) {
    init(assets);
    Typeface icon = iconByDirectoryUri.get(fileLocation);
    if (icon != null) return icon;
    return iconDirectory;
  }

  public static Typeface forFileMediaType(AssetManager assets, String mime) {
    init(assets);
    if (mime == null) return iconFile;
    if (mime.equals("application/pdf")) return iconPdf;
    if (mime.startsWith("audio/")) return iconAudio;
    if (mime.startsWith("video/")) return iconVideo;
    if (mime.startsWith("image/")) return iconImage;
    if (mime.startsWith("text/")) return iconText;
    if (mimeArchive.contains(mime)) return iconArchive;
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

    mimeArchive = new HashSet<>();
    mimeArchive.add("application/zip");
    mimeArchive.add("application/x-gzip");
    mimeArchive.add("application/x-tar");
    mimeArchive.add("application/x-rar-compressed");
    mimeArchive.add("application/x-ace-compressed");
    mimeArchive.add("application/x-bzip2");
    mimeArchive.add("application/x-compress");
    mimeArchive.add("application/x-7z-compressed");

    iconDirectory = createFromAsset(assets, "ic_dir.ttf");
    iconFile = createFromAsset(assets, "ic_file.ttf");
    iconByDirectoryUri = ImmutableMap.<String, Typeface>builder()
        .put(getFileId(UserDirs.DIR_ROOT), createFromAsset(assets, "ic_dir_device.ttf"))
        .put(getFileId(UserDirs.DIR_HOME), createFromAsset(assets, "ic_dir_home.ttf"))
        .put(getFileId(UserDirs.DIR_DCIM), createFromAsset(assets, "ic_dir_dcim.ttf"))
        .put(getFileId(UserDirs.DIR_MUSIC), createFromAsset(assets, "ic_dir_music.ttf"))
        .put(getFileId(UserDirs.DIR_ALARMS), createFromAsset(assets, "ic_dir_alarms.ttf"))
        .put(getFileId(UserDirs.DIR_MOVIES), createFromAsset(assets, "ic_dir_movies.ttf"))
        .put(getFileId(UserDirs.DIR_ANDROID), createFromAsset(assets, "ic_dir_android.ttf"))
        .put(getFileId(UserDirs.DIR_PICTURES), createFromAsset(assets, "ic_dir_pictures.ttf"))
        .put(getFileId(UserDirs.DIR_PODCASTS), createFromAsset(assets, "ic_dir_podcasts.ttf"))
        .put(getFileId(UserDirs.DIR_DOWNLOADS), createFromAsset(assets, "ic_dir_download.ttf"))
        .put(getFileId(UserDirs.DIR_RINGTONES), createFromAsset(assets, "ic_dir_ringtones.ttf"))
        .put(getFileId(UserDirs.DIR_NOTIFICATIONS), createFromAsset(assets, "ic_dir_notifications.ttf"))
        .build();
  }

  private IconFonts() {}
}
