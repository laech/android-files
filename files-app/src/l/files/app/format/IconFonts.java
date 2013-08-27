package l.files.app.format;

import static android.graphics.Typeface.createFromAsset;
import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static java.util.Locale.ENGLISH;
import static l.files.app.UserDirs.*;
import static l.files.app.format.FileExtensions.*;
import static org.apache.commons.io.FilenameUtils.getExtension;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.util.Map;
import java.util.Set;

final class IconFonts {

  private static Map<File, Typeface> ic_dirs;
  private static Map<String, Typeface> ic_files;
  private static Typeface ic_dir;
  private static Typeface ic_file;

  static Typeface dir(AssetManager assets, File dir) {
    init(assets);
    Typeface typeface = ic_dirs.get(dir);
    return typeface != null ? typeface : ic_dir;
  }

  static Typeface file(AssetManager assets, File file) {
    init(assets);
    String ext = getExtension(file.getName()).toLowerCase(ENGLISH);
    Typeface typeface = ic_files.get(ext);
    return typeface != null ? typeface : ic_file;
  }

  private static void init(AssetManager assets) {
    if (null != ic_dirs) {
      return;
    }
    ic_dir = createFromAsset(assets, "ic_dir.ttf");
    ic_file = createFromAsset(assets, "ic_file.ttf");
    ic_dirs = ImmutableMap.<File, Typeface>builder()
        .put(DIR_ROOT, createFromAsset(assets, "ic_dir_device.ttf"))
        .put(DIR_HOME, createFromAsset(assets, "ic_dir_home.ttf"))
        .put(DIR_ALARMS, createFromAsset(assets, "ic_dir_alarms.ttf"))
        .put(DIR_ANDROID, createFromAsset(assets, "ic_dir_android.ttf"))
        .put(DIR_DCIM, createFromAsset(assets, "ic_dir_dcim.ttf"))
        .put(DIR_DOWNLOADS, createFromAsset(assets, "ic_dir_download.ttf"))
        .put(DIR_MOVIES, createFromAsset(assets, "ic_dir_movies.ttf"))
        .put(DIR_MUSIC, createFromAsset(assets, "ic_dir_music.ttf"))
        .put(DIR_NOTIFICATIONS, createFromAsset(assets, "ic_dir_notifications.ttf"))
        .put(DIR_PICTURES, createFromAsset(assets, "ic_dir_pictures.ttf"))
        .put(DIR_PODCASTS, createFromAsset(assets, "ic_dir_podcasts.ttf"))
        .put(DIR_RINGTONES, createFromAsset(assets, "ic_dir_ringtones.ttf"))
        .build();
    ic_files = ImmutableMap.<String, Typeface>builder()
        .putAll(map(PDFS, createFromAsset(assets, "ic_file_pdf.ttf")))
        .putAll(map(ARCHIVES, createFromAsset(assets, "ic_file_archive.ttf")))
        .putAll(map(AUDIOS, createFromAsset(assets, "ic_file_audio.ttf")))
        .putAll(map(VIDEOS, createFromAsset(assets, "ic_file_video.ttf")))
        .putAll(map(IMAGES, createFromAsset(assets, "ic_file_image.ttf")))
        .putAll(map(TEXTS, createFromAsset(assets, "ic_file_text.ttf")))
        .build();
  }

  private static Map<String, Typeface> map(Set<String> exts, Typeface typeface) {
    Map<String, Typeface> map = newHashMapWithExpectedSize(exts.size());
    for (String ext : exts) {
      map.put(ext, typeface);
    }
    return map;
  }

  private IconFonts() {}
}
