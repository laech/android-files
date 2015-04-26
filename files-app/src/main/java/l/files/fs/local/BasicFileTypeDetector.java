package l.files.fs.local;

import android.webkit.MimeTypeMap;

import com.google.common.net.MediaType;

import java.io.IOException;

import static com.google.common.io.Files.getFileExtension;
import static com.google.common.net.MediaType.OCTET_STREAM;

final class BasicFileTypeDetector extends LocalFileTypeDetector {

    public static final BasicFileTypeDetector INSTANCE = new BasicFileTypeDetector();

    private BasicFileTypeDetector() {
    }

    @Override
    protected MediaType detectRegularFile(
            LocalResource resource,
            LocalResourceStatus status) throws IOException {

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
