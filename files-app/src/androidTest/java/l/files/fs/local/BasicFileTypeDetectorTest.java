package l.files.fs.local;

import l.files.fs.FileTypeDetector;

public final class BasicFileTypeDetectorTest
    extends LocalFileTypeDetectorTest {

  @Override protected FileTypeDetector detector(LocalFileSystem fs) {
    return new BasicFileTypeDetector(fs);
  }
}
