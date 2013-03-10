package com.example.files.media;

import static com.example.files.util.Files.getFileExtension;
import static java.util.Locale.ENGLISH;

import java.io.File;

import com.example.files.R;

public final class DefaultImageMap extends ImageMap {

  @Override public int get(File file) {
    if (file.isDirectory()) {
      return R.drawable.ic_folder;
    }

    String ext = getFileExtension(file);
    if (ext == null) {
      return R.drawable.ic_file;
    }
    ext = ext.toLowerCase(ENGLISH);

    if (IMAGES.containsKey(ext)) {
      return R.drawable.ic_image;
    }

    return R.drawable.ic_file;
  }
}
