package com.example.files.media;

import static java.util.Locale.ENGLISH;

public class MediaMap {

  public String get(String extension) {
    return Medias.get(extension.toLowerCase(ENGLISH));
  }
}
