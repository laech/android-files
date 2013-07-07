package l.files.io;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.net.MediaType;

import java.io.File;
import java.util.List;

import static com.google.common.net.MediaType.OCTET_STREAM;

final class CompositeDetector implements Function<File, MediaType> {

  private List<Function<File, MediaType>> detectors;

  CompositeDetector(Function<File, MediaType>... detectors) {
    this.detectors = ImmutableList.copyOf(detectors);
  }

  @Override public MediaType apply(File file) {
    for (Function<File, MediaType> detector : detectors) {
      MediaType media = detector.apply(file);
      if (media != null && !media.equals(OCTET_STREAM)) {
        return media;
      }
    }
    return OCTET_STREAM;
  }

}
