package l.files.fs.media;

public final class BasicDetectorTest extends AbstractDetectorTest {

    @Override
    AbstractDetector detector() {
        return BasicDetector.INSTANCE;
    }

}
