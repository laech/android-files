package l.files.fs.media;

import static java.util.Locale.ENGLISH;
import static l.files.fs.media.MediaTypes.MEDIA_TYPE_OCTET_STREAM;

/**
 * Detects content type based on name and file type.
 */
final class ExtensionDetector {

    static final ExtensionDetector INSTANCE = new ExtensionDetector();

    private ExtensionDetector() {
    }

    String detect(String ext) {
        String type = MimeUtils.guessMimeTypeFromExtension(ext.toLowerCase(ENGLISH));
        return type != null ? type : MEDIA_TYPE_OCTET_STREAM;
    }

}
