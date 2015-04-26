package l.files.fs;

import com.google.common.net.MediaType;

import org.apache.tika.Tika;
import org.apache.tika.io.TaggedIOException;

import java.io.IOException;
import java.io.InputStream;

import static com.google.common.net.MediaType.OCTET_STREAM;
import static l.files.fs.LinkOption.FOLLOW;

/**
 * Detects the media type of the underlying file by reading it's header.
 */
public final class MagicDetector extends AbstractDetector {

    private static final class TikaHolder {
        static final Tika tika = new Tika();
    }

    public static final MagicDetector INSTANCE = new MagicDetector();

    private MagicDetector() {
    }

    @Override
    protected MediaType detectFile(
            Resource resource, ResourceStatus status) throws IOException {

        try {

            try (InputStream in = resource.openInputStream(FOLLOW)) {
                String result = TikaHolder.tika.detect(in, resource.getName());
                return MediaType.parse(result);
            }

        } catch (TaggedIOException e) {
            if (e.getCause() != null) {
                throw e.getCause();
            } else {
                throw e;
            }
        } catch (IllegalArgumentException e) {
            return OCTET_STREAM;
        }
    }

}
