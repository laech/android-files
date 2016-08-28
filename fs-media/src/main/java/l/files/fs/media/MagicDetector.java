package l.files.fs.media;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MimeTypes;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import l.files.fs.Path;

import static l.files.fs.Files.newInputStream;

/**
 * Detects the media type of the underlying file by reading it's header.
 */
final class MagicDetector extends TikaDetector {

    static final MagicDetector INSTANCE = new MagicDetector();

    private MagicDetector() {
    }

    @Override
    String detectFile(MimeTypes types, Path path) throws IOException {
        InputStream in = new BufferedInputStream(newInputStream(path));
        try {
            return types.detect(in, new Metadata()).getBaseType().toString();
        } finally {
            in.close();
        }
    }

}
