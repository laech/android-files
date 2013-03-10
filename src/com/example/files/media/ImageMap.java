package com.example.files.media;

import com.example.files.R;

import java.io.File;

import static com.example.files.util.Files.getFileExtension;

public class ImageMap {

  public int get(File file) {
    return (file.isDirectory())
        ? R.drawable.ic_folder :
        Images.get(getFileExtension(file));
  }
}
