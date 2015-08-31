package l.files.fs.local;

import android.webkit.MimeTypeMap;

import l.files.fs.Resource;

import static l.files.fs.Resource.OCTET_STREAM;

/**
 * Detects content type based on name and resource type.
 */
final class BasicDetector extends AbstractDetector {

  static final BasicDetector INSTANCE = new BasicDetector();

  private BasicDetector() {
  }

  @Override String detectFile(Resource resource, l.files.fs.Stat stat) {
    MimeTypeMap typeMap = MimeTypeMap.getSingleton();
    String ext = resource.name().ext();
    String type = typeMap.getMimeTypeFromExtension(ext);
    return type != null ? type : OCTET_STREAM;
  }

}
