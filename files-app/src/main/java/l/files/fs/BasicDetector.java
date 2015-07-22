package l.files.fs;

import android.webkit.MimeTypeMap;

/**
 * Detects content type based on name and resource type.
 */
public final class BasicDetector extends AbstractDetector {

  public static final BasicDetector INSTANCE = new BasicDetector();

  private BasicDetector() {
  }

  @Override String detectFile(Resource resource, Stat stat) {
    MimeTypeMap typeMap = MimeTypeMap.getSingleton();
    String ext = resource.name().ext();
    String type = typeMap.getMimeTypeFromExtension(ext);
    return type != null ? type : OCTET_STREAM;
  }

}
