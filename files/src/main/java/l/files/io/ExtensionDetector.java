package l.files.io;

import com.google.common.base.Function;
import com.google.common.net.MediaType;

import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.net.MediaType.OCTET_STREAM;
import static java.util.Locale.ENGLISH;
import static org.apache.commons.io.FilenameUtils.getExtension;

final class ExtensionDetector implements Function<File, MediaType> {

  @Override public MediaType apply(File file) {
    checkNotNull(file, "file");
    String extension = getExtension(file.getName()).toLowerCase(ENGLISH);
    String mediaType = Medias.get(extension);
    if (mediaType != null) {
      return MediaType.parse(mediaType);
    }
    return OCTET_STREAM;
  }

}
