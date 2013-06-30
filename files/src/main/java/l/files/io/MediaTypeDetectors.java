package l.files.io;

public final class MediaTypeDetectors {

  /**
   * Gets a new media detector with default configurations.
   * <p/>
   * Do not use the returned detector on the main application thread.
   */
  public static MediaTypeDetector getDefault() {
    return new CompositeDetector(new ExtensionDetector(), new ContentDetector());
  }

  private MediaTypeDetectors() {}

}
