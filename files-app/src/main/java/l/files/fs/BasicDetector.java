package l.files.fs;

import android.webkit.MimeTypeMap;

import com.google.common.net.MediaType;

import java.io.IOException;

import static com.google.common.io.Files.getFileExtension;
import static com.google.common.net.MediaType.OCTET_STREAM;

/**
 * Detects content type based on name and resource type.
 */
public final class BasicDetector extends AbstractDetector {

    public static final BasicDetector INSTANCE = new BasicDetector();

    private BasicDetector() {
    }

    @Override
    protected MediaType detectFile(
            Resource resource, ResourceStatus status) throws IOException {

        MimeTypeMap typeMap = MimeTypeMap.getSingleton();
        String ext = getFileExtension(resource.getName());
        String type = typeMap.getMimeTypeFromExtension(ext);
        if (type == null) {
            return OCTET_STREAM;
        }
        try {
            return MediaType.parse(type);
        } catch (IllegalArgumentException e) {
            return OCTET_STREAM;
        }
    }
}
