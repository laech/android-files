package l.files.fs;

import org.apache.tika.Tika;
import org.apache.tika.io.TaggedIOException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Detects the media type of the underlying file by reading it's header.
 */
public final class MagicDetector extends AbstractDetector {

  private static final class TikaHolder {
    static final Tika tika = new Tika();
  }

  public static final MagicDetector INSTANCE = new MagicDetector();

  private MagicDetector() {
  }

  @Override String detectFile(Resource resource, Stat stat) throws IOException {

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
