package l.files.fs.local;

import com.google.common.net.MediaType;

import org.apache.tika.Tika;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import l.files.fs.FileStatus;
import l.files.fs.FileSystemException;
import l.files.fs.NoSuchFileException;

/**
 * Detects by looking at the file's magic number.
 * Therefore this detector will need to read the file's header.
 */
final class MagicFileTypeDetector extends LocalFileTypeDetector {

  private static final MagicFileTypeDetector INSTANCE =
      new MagicFileTypeDetector(LocalFileSystem.get());

  static MagicFileTypeDetector get() {
    return INSTANCE;
  }

  MagicFileTypeDetector(LocalFileSystem fs) {
    super(fs);
  }

  @Override protected MediaType detectRegularFile(FileStatus stat) {
    try {
      URL url = stat.id().toUri().toURL();
      return cache().getUnchecked(LazyTika.TIKA.detect(url));
    } catch (FileNotFoundException e) {
      throw new NoSuchFileException(e);
    } catch (IOException e) {
      throw new FileSystemException(e);
    }
  }

  /**
   * A {@link Tika} instance is expensive to create, this class defers
   * initialization until one is really needed.
   *
   * @see <a href="http://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom">
   * Initialization-on-demand holder idiom</a>
   */
  private static class LazyTika {
    static final Tika TIKA = new Tika();
  }
}
