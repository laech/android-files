package l.files.common.io;

import com.google.common.net.MediaType;
import org.apache.tika.Tika;

import java.io.File;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.net.MediaType.OCTET_STREAM;

enum ContentDetector implements Detector {

  INSTANCE;

  private static final class LazyHolder {
    static final Tika TIKA = new Tika();
  }

  @Override public MediaType detect(File file) {
    checkNotNull(file, "file");
    try {
      return MediaType.parse(LazyHolder.TIKA.detect(file));
    } catch (IOException e) {
      return OCTET_STREAM;
    }
  }
}
