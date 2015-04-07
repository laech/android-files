package l.files.fs.local;

public final class BasicFileTypeDetectorTest extends LocalFileTypeDetectorTest {

    @Override
    protected LocalFileTypeDetector detector() {
        return BasicFileTypeDetector.INSTANCE;
    }

}
