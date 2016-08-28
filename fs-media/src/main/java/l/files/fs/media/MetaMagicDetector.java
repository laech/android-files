package l.files.fs.media;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MimeTypes;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import l.files.fs.Path;

import static l.files.fs.Files.newInputStream;
import static org.apache.tika.metadata.TikaMetadataKeys.RESOURCE_NAME_KEY;

/**
 * Detects the media type of the underlying file using
 * its properties and its content.
 */
final class MetaMagicDetector extends TikaDetector {

    static final MetaMagicDetector INSTANCE = new MetaMagicDetector();

    private MetaMagicDetector() {
    }

    @Override
    String detectFile(MimeTypes types, Path path) throws IOException {
        Metadata meta = new Metadata();
        meta.add(RESOURCE_NAME_KEY, path.name().toString());
        InputStream in = new BufferedInputStream(newInputStream(path));
        try {
            return types.detect(in, meta).getBaseType().toString();
        } finally {
            in.close();
        }
    }

}
