package l.files.fs.local;

import org.apache.tika.Tika;
import org.apache.tika.io.TaggedIOException;

import java.io.IOException;
import java.io.InputStream;

import l.files.fs.File;

/**
 * Detects the media type of the underlying file by reading it's header.
 */
final class MagicDetector extends AbstractDetector {

  private static final class TikaHolder {
    static final Tika tika = new Tika();
  }

  static final MagicDetector INSTANCE = new MagicDetector();

  private MagicDetector() {
  }

  @Override
  String detectFile(File file, l.files.fs.Stat stat) throws IOException {

    try (InputStream in = file.input()) {
      return TikaHolder.tika.detect(in);

    } catch (TaggedIOException e) {
      if (e.getCause() != null) {
        throw e.getCause();
      } else {
        throw e;
      }
    }
  }

}
