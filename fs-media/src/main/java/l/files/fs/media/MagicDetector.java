package l.files.fs.media;

import org.apache.tika.io.TaggedIOException;

import java.io.IOException;
import java.io.InputStream;

import l.files.base.io.Closer;
import l.files.fs.Path;
import l.files.fs.Stat;

import static l.files.fs.Files.newInputStream;

/**
 * Detects the media type of the underlying file by reading it's header.
 */
final class MagicDetector extends AbstractDetector {

    static final MagicDetector INSTANCE = new MagicDetector();

    private MagicDetector() {
    }

    @Override
    String detectFile(Path path, Stat stat) throws IOException {

        Closer closer = Closer.create();
        try {

            InputStream in = closer.register(newInputStream(path));
            return TikaHolder.tika.detect(in);

        } catch (TaggedIOException e) {
            if (e.getCause() != null) {
                throw closer.rethrow(e.getCause());
            } else {
                throw closer.rethrow(e);
            }
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

}
