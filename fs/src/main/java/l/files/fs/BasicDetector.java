package l.files.fs;

import static java.util.Locale.ENGLISH;
import static l.files.fs.Files.MEDIA_TYPE_OCTET_STREAM;

/**
 * Detects content type based on name and file type.
 */
final class BasicDetector extends AbstractDetector {

    static final BasicDetector INSTANCE = new BasicDetector();

    private BasicDetector() {
    }

    @Override
    String detectFile(Path path, Stat stat) {
        String ext = path.name().ext().toLowerCase(ENGLISH);
        String type = MimeUtils.guessMimeTypeFromExtension(ext);
        return type != null ? type : MEDIA_TYPE_OCTET_STREAM;
    }

}
