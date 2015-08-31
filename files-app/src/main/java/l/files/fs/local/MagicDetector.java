package l.files.fs.local;

import org.apache.tika.Tika;
import org.apache.tika.io.TaggedIOException;

import java.io.IOException;
import java.io.InputStream;

import l.files.fs.Resource;

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
  String detectFile(Resource resource, l.files.fs.Stat stat) throws IOException {

    try (InputStream in = resource.input()) {
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
