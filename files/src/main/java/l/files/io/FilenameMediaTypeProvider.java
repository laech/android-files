package l.files.io;

import static java.util.Locale.ENGLISH;
import static org.apache.commons.io.FilenameUtils.getExtension;

import java.io.File;

import com.google.common.base.Function;
import com.google.common.base.Optional;

public final class FilenameMediaTypeProvider
    implements Function<File, Optional<String>> {

  @Override public Optional<String> apply(File file) {
    String extension = getExtension(file.getName()).toLowerCase(ENGLISH);
    return Optional.fromNullable(Medias.get(extension));
  }

}
