package l.files.fs.media;

import l.files.fs.Path;
import l.files.fs.Stat;

/**
 * Detects content type based on name and file type.
 */
final class PropertyDetector extends BasePropertyDetector {

    static final PropertyDetector INSTANCE = new PropertyDetector();

    private PropertyDetector() {
    }

    @Override
    String detectFile(Path path, Stat stat) {
        return ExtensionDetector.INSTANCE.detect(path.name().ext());
    }

}
