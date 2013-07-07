package l.files.io;

import com.google.common.base.Function;
import com.google.common.net.MediaType;
import org.apache.tika.Tika;

import java.io.File;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.net.MediaType.OCTET_STREAM;

final class ContentDetector implements Function<File, MediaType> {

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
