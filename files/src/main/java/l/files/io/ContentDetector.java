package l.files.io;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.net.MediaType.OCTET_STREAM;

import java.io.File;
import java.io.IOException;

import org.apache.tika.Tika;

import com.google.common.net.MediaType;

final class ContentDetector implements MediaTypeDetector {

  private static final class LazyHolder {
    static final Tika TIKA = new Tika();
  }

  @Override public MediaType apply(File file) {
    checkNotNull(file, "file");
    try {
      return MediaType.parse(LazyHolder.TIKA.detect(file));
    } catch (IOException e) {
      return OCTET_STREAM;
    }
  }

}
