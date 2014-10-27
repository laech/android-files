package l.files.fs.local;

import android.webkit.MimeTypeMap;

import com.google.common.net.MediaType;

import l.files.fs.FileStatus;

import static com.google.common.net.MediaType.OCTET_STREAM;
import static org.apache.commons.io.FilenameUtils.getExtension;

/**
 * Detects by looking at the file's extension.
 */
final class BasicFileTypeDetector extends LocalFileTypeDetector {

  private static final BasicFileTypeDetector INSTANCE =
      new BasicFileTypeDetector(LocalFileSystem.get());

  static BasicFileTypeDetector get() {
    return INSTANCE;
  }

  BasicFileTypeDetector(LocalFileSystem fs) {
    super(fs);
  }

  @Override protected MediaType detectRegularFile(FileStatus stat) {
    MimeTypeMap typeMap = MimeTypeMap.getSingleton();
    String ext = getExtension(stat.name());
    String type = typeMap.getMimeTypeFromExtension(ext);
    if (type == null) {
      return OCTET_STREAM;
    }
    return cache().getUnchecked(type);
  }
}
