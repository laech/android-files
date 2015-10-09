package l.files.fs.local;

import org.apache.tika.io.TaggedIOException;

import java.io.IOException;

import l.files.fs.File;

/**
 * Detects the media type of the underlying file using
 * its properties and its content.
 */
final class MetaMagicDetector extends AbstractDetector {

    static final MetaMagicDetector INSTANCE = new MetaMagicDetector();

    private MetaMagicDetector() {
    }

    @Override
    String detectFile(File file, l.files.fs.Stat stat) throws IOException {

        try {

            return TikaHolder.tika.detect(file.uri().toURL());

        } catch (TaggedIOException e) {
            if (e.getCause() != null) {
                throw e.getCause();
            } else {
                throw e;
            }
        }
    }

}
