package l.files.fs.media;

import java.io.IOException;
import java.io.InputStream;

import l.files.base.io.Closer;
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
    String detectFile(Path path, Closer closer) throws IOException {
        InputStream in = closer.register(newInputStream(path));
        return TikaHolder.tika.detect(in);
    }

}
