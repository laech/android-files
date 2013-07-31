package l.files.common.io;

public final class Detectors {
  private Detectors() {}

  /**
   * Gets a media detector with default configurations.
   * <p/>
   * Do not use the returned detector on the main application thread.
   */
  public static Detector get() {
    return ContentDetector.INSTANCE;
  }
}
