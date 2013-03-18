package com.example.files.media;

public class MediaMap {

  public static final MediaMap INSTANCE = new MediaMap();

  MediaMap() {
  }

  /**
   * Gets the media type for the given file extension.
   *
   * @param extension the file extension without the ".", in any case
   */
  public String get(String extension) {
    return Medias.get(extension);
  }

}
