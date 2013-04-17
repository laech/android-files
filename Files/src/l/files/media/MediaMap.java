package l.files.media;

import static java.util.Locale.ENGLISH;

public class MediaMap {

  public static final MediaMap INSTANCE = new MediaMap();

  MediaMap() {
  }

  public String get(String extension) {
    return Medias.get(extension.toLowerCase(ENGLISH));
  }
}
