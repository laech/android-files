package l.files.fs.media;

import org.apache.tika.Tika;
import org.apache.tika.io.TaggedIOException;

import java.io.IOException;

import l.files.base.io.Closer;
import l.files.fs.Path;
import l.files.fs.Stat;

/**
 * Detects the media type of the underlying file by reading it's header.
 */
abstract class TikaDetector extends BasePropertyDetector {

    @Override
    String detectFile(Path path, Stat stat) throws IOException {

        Closer closer = Closer.create();
        try {

            return detectFile(path, closer);

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

    abstract String detectFile(Path path, Closer closer) throws IOException;

    static final class TikaHolder {

        private TikaHolder() {
        }

        static final Tika tika = new Tika();

    }

}
