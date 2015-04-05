package l.files.fs.local;

import android.webkit.MimeTypeMap;

import com.google.common.net.MediaType;

import l.files.fs.ResourceStatus;

import static com.google.common.net.MediaType.OCTET_STREAM;
import static org.apache.commons.io.FilenameUtils.getExtension;

final class BasicFileTypeDetector extends LocalFileTypeDetector {

    public static final BasicFileTypeDetector INSTANCE = new BasicFileTypeDetector();

    private BasicFileTypeDetector() {
    }

    @Override
    protected MediaType detectRegularFile(ResourceStatus status) {
        MimeTypeMap typeMap = MimeTypeMap.getSingleton();
        String ext = getExtension(status.getName());
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
