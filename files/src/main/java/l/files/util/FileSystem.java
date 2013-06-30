package l.files.util;

import static android.os.Environment.getExternalStorageDirectory;
import static android.os.Environment.getExternalStoragePublicDirectory;

import java.io.File;

import android.os.Environment;

public class FileSystem {

  public static final FileSystem INSTANCE = new FileSystem();

  public static final File DIRECTORY_ROOT = new File("/");
  public static final File DIRECTORY_HOME = getExternalStorageDirectory();
  public static final File DIRECTORY_ALARMS = dir(Environment.DIRECTORY_ALARMS);
  public static final File DIRECTORY_ANDROID = dir("Android");
  public static final File DIRECTORY_DCIM = dir(Environment.DIRECTORY_DCIM);
  public static final File DIRECTORY_DOWNLOADS = dir(Environment.DIRECTORY_DOWNLOADS);
  public static final File DIRECTORY_MOVIES = dir(Environment.DIRECTORY_MOVIES);
  public static final File DIRECTORY_MUSIC = dir(Environment.DIRECTORY_MUSIC);
  public static final File DIRECTORY_PICTURES = dir(Environment.DIRECTORY_PICTURES);
  public static final File DIRECTORY_PODCASTS = dir(Environment.DIRECTORY_PODCASTS);
  public static final File DIRECTORY_NOTIFICATIONS = dir(Environment.DIRECTORY_NOTIFICATIONS);
  public static final File DIRECTORY_RINGTONES = dir(Environment.DIRECTORY_RINGTONES);

  FileSystem() {
  }

  private static File dir(String type) {
    return getExternalStoragePublicDirectory(type);
  }

}
