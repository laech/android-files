package l.files.app.format;

import static android.graphics.Typeface.createFromAsset;
import static l.files.app.UserDirs.*;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.util.Map;

final class IconFonts {

  private static Map<File, Typeface> typefaces;
  private static Typeface ic_dir;
  private static Typeface ic_file;

  static Typeface dir(AssetManager assets, File dir) {
    init(assets);
    Typeface typeface = typefaces.get(dir);
    return typeface != null ? typeface : ic_dir;
  }

  static Typeface file(AssetManager assets, File file) {
    init(assets);
    return ic_file; // TODO
  }

  private static void init(AssetManager assets) {
    if (null != typefaces) {
      return;
    }
    ic_dir = createFromAsset(assets, "ic_dir.ttf");
    ic_file = createFromAsset(assets, "ic_file.ttf");
    typefaces = ImmutableMap.<File, Typeface>builder()
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
  }

  private IconFonts() {}
}
