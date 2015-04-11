package l.files.fs.local;

import com.google.common.net.MediaType;

import org.apache.tika.Tika;
import org.apache.tika.io.TaggedIOException;

import java.io.File;
import java.io.IOException;

import static com.google.common.net.MediaType.OCTET_STREAM;

final class MagicFileTypeDetector extends LocalFileTypeDetector {

    private static final class TikaHolder {
        static final Tika tika = new Tika();
    }

    public static final MagicFileTypeDetector INSTANCE = new MagicFileTypeDetector();

    private MagicFileTypeDetector() {
    }

    @Override
    protected MediaType detectRegularFile(LocalResourceStatus status)
            throws IOException {

        try {

            File file = status.getResource().getFile();
            return MediaType.parse(TikaHolder.tika.detect(file));

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
