package l.files.app.format;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import l.files.R;
import l.files.app.UserDirs;

import java.io.File;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.io.FilenameUtils.getExtension;

final class FileDrawableFunction implements Function<File, Drawable> {

  private static final Map<File, Integer> DIR_IMGS = ImmutableMap.<File, Integer>builder()
      .put(UserDirs.DIR_ROOT, R.drawable.ic_dir_device)
      .put(UserDirs.DIR_HOME, R.drawable.ic_dir_home)
      .put(UserDirs.DIR_ALARMS, R.drawable.ic_dir_alarms)
      .put(UserDirs.DIR_ANDROID, R.drawable.ic_dir_android)
      .put(UserDirs.DIR_DCIM, R.drawable.ic_dir_dcim)
      .put(UserDirs.DIR_DOWNLOADS, R.drawable.ic_dir_download)
      .put(UserDirs.DIR_MOVIES, R.drawable.ic_dir_movies)
      .put(UserDirs.DIR_MUSIC, R.drawable.ic_dir_music)
      .put(UserDirs.DIR_NOTIFICATIONS, R.drawable.ic_dir_notifications)
      .put(UserDirs.DIR_PICTURES, R.drawable.ic_dir_pictures)
      .put(UserDirs.DIR_PODCASTS, R.drawable.ic_dir_podcasts)
      .put(UserDirs.DIR_RINGTONES, R.drawable.ic_dir_ringtones)
      .build();

  private final Resources res;

  FileDrawableFunction(Resources res) {
    this.res = checkNotNull(res, "res");
  }

  @Override public Drawable apply(File file) {
    if (file.isDirectory()) {
      Integer id = DIR_IMGS.get(file);
      return res.getDrawable(id != null ? id : R.drawable.ic_dir);
    }
    return res.getDrawable(Images.get(getExtension(file.getName())));
  }
}
