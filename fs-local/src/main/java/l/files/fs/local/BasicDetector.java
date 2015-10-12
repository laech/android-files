package l.files.fs.local;

import l.files.fs.File;
import l.files.fs.Stat;

/**
 * Detects content type based on name and file type.
 */
final class BasicDetector extends AbstractDetector {

    static final BasicDetector INSTANCE = new BasicDetector();

    private BasicDetector() {
    }

    @Override
    String detectFile(File file, Stat stat) {
        return TikaHolder.tika.detect(file.name().toString());
    }

}
