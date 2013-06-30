package l.files.ui;

import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.util.FileSystem.DIRECTORY_ALARMS;
import static l.files.util.FileSystem.DIRECTORY_ANDROID;
import static l.files.util.FileSystem.DIRECTORY_DCIM;
import static l.files.util.FileSystem.DIRECTORY_DOWNLOADS;
import static l.files.util.FileSystem.DIRECTORY_HOME;
import static l.files.util.FileSystem.DIRECTORY_MOVIES;
import static l.files.util.FileSystem.DIRECTORY_MUSIC;
import static l.files.util.FileSystem.DIRECTORY_NOTIFICATIONS;
import static l.files.util.FileSystem.DIRECTORY_PICTURES;
import static l.files.util.FileSystem.DIRECTORY_PODCASTS;
import static l.files.util.FileSystem.DIRECTORY_RINGTONES;
import static l.files.util.FileSystem.DIRECTORY_ROOT;
import static l.files.util.Files.getFileExtension;

import java.io.File;
import java.util.Map;

import l.files.R;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

public final class FileDrawableProvider implements Function<File, Drawable> {

  private static final Map<File, Integer> DIR_IMGS = ImmutableMap
      .<File, Integer> builder()
      .put(DIRECTORY_ROOT, R.drawable.ic_directory_device)
      .put(DIRECTORY_HOME, R.drawable.ic_directory_home)
      .put(DIRECTORY_ALARMS, R.drawable.ic_directory_alarms)
      .put(DIRECTORY_ANDROID, R.drawable.ic_directory_android)
      .put(DIRECTORY_DCIM, R.drawable.ic_directory_dcim)
      .put(DIRECTORY_DOWNLOADS, R.drawable.ic_directory_download)
      .put(DIRECTORY_MOVIES, R.drawable.ic_directory_movies)
      .put(DIRECTORY_MUSIC, R.drawable.ic_directory_music)
      .put(DIRECTORY_NOTIFICATIONS, R.drawable.ic_directory_notifications)
      .put(DIRECTORY_PICTURES, R.drawable.ic_directory_pictures)
      .put(DIRECTORY_PODCASTS, R.drawable.ic_directory_podcasts)
      .put(DIRECTORY_RINGTONES, R.drawable.ic_directory_ringtones)
      .build();

  private final Resources res;

  public FileDrawableProvider(Resources res) {
    this.res = checkNotNull(res, "res");
  }

  @Override public Drawable apply(File file) {
    if (file.isDirectory()) {
      Integer id = DIR_IMGS.get(file);
      return res.getDrawable(id != null ? id : R.drawable.ic_directory);
    }
    return res.getDrawable(Images.get(getFileExtension(file)));
  }

}
