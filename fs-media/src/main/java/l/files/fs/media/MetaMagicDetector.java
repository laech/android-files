package l.files.fs.media;

import org.apache.tika.metadata.Metadata;

import java.io.IOException;
import java.io.InputStream;

import l.files.base.io.Closer;
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
    String detectFile(Path path, Closer closer) throws IOException {
        Metadata meta = new Metadata();
        meta.add(RESOURCE_NAME_KEY, path.name().toString());
        InputStream in = closer.register(newInputStream(path));
        return TikaHolder.tika.detect(in, meta);
    }

}
