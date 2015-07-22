package l.files.fs;

public final class BasicDetectorTest extends AbstractDetectorTest {

  @Override AbstractDetector detector() {
    return BasicDetector.INSTANCE;
  }

}
