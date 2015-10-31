package l.files.fs;

import org.apache.tika.io.TaggedIOException;

import java.io.IOException;
import java.io.InputStream;

import l.files.base.io.Closer;

/**
 * Detects the media type of the underlying file by reading it's header.
 */
final class MagicDetector extends AbstractDetector {

    static final MagicDetector INSTANCE = new MagicDetector();

    private MagicDetector() {
    }

    @Override
    String detectFile(File file, Stat stat) throws IOException {

        Closer closer = Closer.create();
        try {

            InputStream in = closer.register(file.newInputStream());
            return TikaHolder.tika.detect(in);

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
