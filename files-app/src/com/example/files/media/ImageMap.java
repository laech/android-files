package com.example.files.media;

import com.example.files.R;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.example.files.util.FileSystem.DIRECTORY_ALARMS;
import static com.example.files.util.FileSystem.DIRECTORY_ANDROID;
import static com.example.files.util.FileSystem.DIRECTORY_DCIM;
import static com.example.files.util.FileSystem.DIRECTORY_DOWNLOADS;
import static com.example.files.util.FileSystem.DIRECTORY_MOVIES;
import static com.example.files.util.FileSystem.DIRECTORY_MUSIC;
import static com.example.files.util.FileSystem.DIRECTORY_NOTIFICATIONS;
import static com.example.files.util.FileSystem.DIRECTORY_PICTURES;
import static com.example.files.util.FileSystem.DIRECTORY_PODCASTS;
import static com.example.files.util.FileSystem.DIRECTORY_RINGTONES;
import static com.example.files.util.Files.getFileExtension;

public class ImageMap {

  private static final Map<File, Integer> DIRECTORY_IMAGES;

  static {
    DIRECTORY_IMAGES = new HashMap<File, Integer>();
    DIRECTORY_IMAGES.put(DIRECTORY_ALARMS, R.drawable.ic_directory_alarms);
    DIRECTORY_IMAGES.put(DIRECTORY_ANDROID, R.drawable.ic_directory_android);
    DIRECTORY_IMAGES.put(DIRECTORY_DCIM, R.drawable.ic_directory_dcim);
    DIRECTORY_IMAGES.put(DIRECTORY_DOWNLOADS, R.drawable.ic_directory_download);
    DIRECTORY_IMAGES.put(DIRECTORY_MOVIES, R.drawable.ic_directory_movies);
    DIRECTORY_IMAGES.put(DIRECTORY_MUSIC, R.drawable.ic_directory_music);
    DIRECTORY_IMAGES.put(DIRECTORY_NOTIFICATIONS, R.drawable.ic_directory_notifications);
    DIRECTORY_IMAGES.put(DIRECTORY_PICTURES, R.drawable.ic_directory_pictures);
    DIRECTORY_IMAGES.put(DIRECTORY_PODCASTS, R.drawable.ic_directory_podcasts);
    DIRECTORY_IMAGES.put(DIRECTORY_RINGTONES, R.drawable.ic_directory_ringtones);
  }

  public int get(File file) {
    if (file.isDirectory()) {
      Integer id = DIRECTORY_IMAGES.get(file);
      return id != null ? id : R.drawable.ic_directory;
    }
    return Images.get(getFileExtension(file));
  }
}
