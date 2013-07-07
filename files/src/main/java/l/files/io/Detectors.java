package l.files.io;

import com.google.common.base.Function;
import com.google.common.net.MediaType;

import java.io.File;

public final class Detectors {

  /**
   * Gets a media detector with default configurations.
   * <p/>
   * Do not use the returned detector on the main application thread.
   */
  public static Function<File, MediaType> newDetector() {
    return new CompositeDetector(new ExtensionDetector(), new ContentDetector());
  }

  private Detectors() {}

}
