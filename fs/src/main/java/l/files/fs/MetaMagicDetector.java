package l.files.fs;

import org.apache.tika.io.TaggedIOException;
import org.apache.tika.metadata.Metadata;

import java.io.IOException;
import java.io.InputStream;

import l.files.base.io.Closer;

import static org.apache.tika.metadata.TikaMetadataKeys.RESOURCE_NAME_KEY;

/**
 * Detects the media type of the underlying file using
 * its properties and its content.
 */
final class MetaMagicDetector extends AbstractDetector {

    static final MetaMagicDetector INSTANCE = new MetaMagicDetector();

    private MetaMagicDetector() {
    }

    @Override
    String detectFile(File file, Stat stat) throws IOException {

        Metadata meta = new Metadata();
        meta.add(RESOURCE_NAME_KEY, file.name().toString());

        Closer closer = Closer.create();
        try {

            InputStream in = closer.register(file.newInputStream());
            return TikaHolder.tika.detect(in, meta);

        } catch (TaggedIOException e) {
            if (e.getCause() != null) {
                throw e.getCause();
            } else {
                throw e;
            }
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

}
