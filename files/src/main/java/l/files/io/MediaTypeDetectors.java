package l.files.io;

public final class MediaTypeDetectors {

  /**
   * Gets a media detector with default configurations.
   * <p/>
   * Do not use the returned detector on the main application thread.
   */
  public static MediaTypeDetector get() {
    return new CompositeDetector(new ExtensionDetector(), new ContentDetector());
  }

  private MediaTypeDetectors() {}

}
