package l.files.common.io;

import org.apache.tika.Tika;

import java.io.IOException;
import java.io.InputStream;

enum ContentDetector implements Detector {

  INSTANCE;

  private static final class LazyHolder {
    static final Tika TIKA = new Tika();
  }

  @Override public String detect(InputStream stream) throws IOException {
    return LazyHolder.TIKA.detect(stream);
  }
}
