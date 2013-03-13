package com.example.files.media;

import com.example.files.R;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.example.files.util.FileSystem.*;
import static com.example.files.util.Files.getFileExtension;

public class ImageMap {

  private static final Map<File, Integer> DIR_IMAGES;

  static {
    DIR_IMAGES = new HashMap<>();
    DIR_IMAGES.put(DIRECTORY_ALARMS, R.drawable.ic_folder_alarms);
    DIR_IMAGES.put(DIRECTORY_ANDROID, R.drawable.ic_folder_android);
    DIR_IMAGES.put(DIRECTORY_DCIM, R.drawable.ic_folder_dcim);
    DIR_IMAGES.put(DIRECTORY_DOWNLOADS, R.drawable.ic_folder_download);
    DIR_IMAGES.put(DIRECTORY_MOVIES, R.drawable.ic_folder_movies);
    DIR_IMAGES.put(DIRECTORY_MUSIC, R.drawable.ic_folder_music);
    DIR_IMAGES.put(DIRECTORY_NOTIFICATIONS, R.drawable.ic_folder_notifications);
    DIR_IMAGES.put(DIRECTORY_PICTURES, R.drawable.ic_folder_pictures);
    DIR_IMAGES.put(DIRECTORY_PODCASTS, R.drawable.ic_folder_podcasts);
    DIR_IMAGES.put(DIRECTORY_RINGTONES, R.drawable.ic_folder_ringtones);
  }

  public int get(File file) {
    if (file.isDirectory()) {
      Integer id = DIR_IMAGES.get(file);
      return id != null ? id : R.drawable.ic_folder;
    }
    return Images.get(getFileExtension(file));
  }
}
