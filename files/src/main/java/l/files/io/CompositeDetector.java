package l.files.io;

import static com.google.common.net.MediaType.OCTET_STREAM;

import java.io.File;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.net.MediaType;

final class CompositeDetector implements MediaTypeDetector {

  private List<MediaTypeDetector> detectors;

  public CompositeDetector(MediaTypeDetector... detectors) {
    this.detectors = ImmutableList.copyOf(detectors);
  }

  @Override public MediaType apply(File file) {
    for (MediaTypeDetector detector : detectors) {
      MediaType media = detector.apply(file);
      if (!OCTET_STREAM.equals(media)) {
        return media;
      }
    }
    return OCTET_STREAM;
  }

}
