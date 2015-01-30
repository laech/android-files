package l.files.ui;

import android.os.Environment;

import java.io.File;

import l.files.fs.DefaultPathProvider;
import l.files.fs.Path;

import static android.os.Environment.getExternalStorageDirectory;
import static android.os.Environment.getExternalStoragePublicDirectory;

public final class UserDirs {
  private UserDirs() {}

  public static final Path DIR_ROOT = DefaultPathProvider.INSTANCE.get(new File("/").toURI());
  public static final Path DIR_HOME = DefaultPathProvider.INSTANCE.get(getExternalStorageDirectory().toURI());
  public static final Path DIR_ALARMS = dir(Environment.DIRECTORY_ALARMS);
  public static final Path DIR_ANDROID = dir("Android");
  public static final Path DIR_DCIM = dir(Environment.DIRECTORY_DCIM);
  public static final Path DIR_DOWNLOADS = dir(Environment.DIRECTORY_DOWNLOADS);
  public static final Path DIR_MOVIES = dir(Environment.DIRECTORY_MOVIES);
  public static final Path DIR_MUSIC = dir(Environment.DIRECTORY_MUSIC);
  public static final Path DIR_PICTURES = dir(Environment.DIRECTORY_PICTURES);
  public static final Path DIR_PODCASTS = dir(Environment.DIRECTORY_PODCASTS);
  public static final Path DIR_NOTIFICATIONS = dir(Environment.DIRECTORY_NOTIFICATIONS);
  public static final Path DIR_RINGTONES = dir(Environment.DIRECTORY_RINGTONES);

  private static Path dir(String type) {
    return DefaultPathProvider.INSTANCE.get(getExternalStoragePublicDirectory(type).toURI());
  }
}
