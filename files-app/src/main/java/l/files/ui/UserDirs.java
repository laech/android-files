package l.files.ui;

import android.os.Environment;

import java.io.File;

import static android.os.Environment.getExternalStorageDirectory;
import static android.os.Environment.getExternalStoragePublicDirectory;

public final class UserDirs {
  private UserDirs() {}

  public static final File DIR_ROOT = new File("/");
  public static final File DIR_HOME = getExternalStorageDirectory();
  public static final File DIR_ALARMS = dir(Environment.DIRECTORY_ALARMS);
  public static final File DIR_ANDROID = dir("Android");
  public static final File DIR_DCIM = dir(Environment.DIRECTORY_DCIM);
  public static final File DIR_DOWNLOADS = dir(Environment.DIRECTORY_DOWNLOADS);
  public static final File DIR_MOVIES = dir(Environment.DIRECTORY_MOVIES);
  public static final File DIR_MUSIC = dir(Environment.DIRECTORY_MUSIC);
  public static final File DIR_PICTURES = dir(Environment.DIRECTORY_PICTURES);
  public static final File DIR_PODCASTS = dir(Environment.DIRECTORY_PODCASTS);
  public static final File DIR_NOTIFICATIONS = dir(Environment.DIRECTORY_NOTIFICATIONS);
  public static final File DIR_RINGTONES = dir(Environment.DIRECTORY_RINGTONES);

  private static File dir(String type) {
    return getExternalStoragePublicDirectory(type);
  }
}
