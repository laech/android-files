package l.files.ui;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.util.Map;
import l.files.R;

import static com.google.common.base.Preconditions.checkNotNull;
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
import static org.apache.commons.io.FilenameUtils.getExtension;

final class FileDrawableProvider implements Function<File, Drawable> {

  private static final Map<File, Integer> DIR_IMGS = ImmutableMap.<File, Integer>builder()
      .put(DIR_ROOT, R.drawable.ic_directory_device)
      .put(DIR_HOME, R.drawable.ic_directory_home)
      .put(DIR_ALARMS, R.drawable.ic_directory_alarms)
      .put(DIR_ANDROID, R.drawable.ic_directory_android)
      .put(DIR_DCIM, R.drawable.ic_directory_dcim)
      .put(DIR_DOWNLOADS, R.drawable.ic_directory_download)
      .put(DIR_MOVIES, R.drawable.ic_directory_movies)
      .put(DIR_MUSIC, R.drawable.ic_directory_music)
      .put(DIR_NOTIFICATIONS, R.drawable.ic_directory_notifications)
      .put(DIR_PICTURES, R.drawable.ic_directory_pictures)
      .put(DIR_PODCASTS, R.drawable.ic_directory_podcasts)
      .put(DIR_RINGTONES, R.drawable.ic_directory_ringtones)
      .build();

  private final Resources res;

  FileDrawableProvider(Resources res) {
    this.res = checkNotNull(res, "res");
  }

  @Override public Drawable apply(File file) {
    if (file.isDirectory()) {
      Integer id = DIR_IMGS.get(file);
      return res.getDrawable(id != null ? id : R.drawable.ic_directory);
    }
    return res.getDrawable(Images.get(getExtension(file.getName())));
  }
}
