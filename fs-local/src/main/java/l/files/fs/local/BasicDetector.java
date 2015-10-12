package l.files.fs.local;

import android.webkit.MimeTypeMap;

import l.files.fs.File;
import l.files.fs.Stat;

import static java.util.Locale.ENGLISH;
import static l.files.fs.File.MEDIA_TYPE_OCTET_STREAM;

/**
 * Detects content type based on name and file type.
 */
final class BasicDetector extends AbstractDetector {

    static final BasicDetector INSTANCE = new BasicDetector();

    private BasicDetector() {
    }

    @Override
    String detectFile(File file, Stat stat) {
        MimeTypeMap typeMap = MimeTypeMap.getSingleton();
        String ext = file.name().ext().toLowerCase(ENGLISH);
        String type = typeMap.getMimeTypeFromExtension(ext);
        return type != null ? type : MEDIA_TYPE_OCTET_STREAM;
    }

}
