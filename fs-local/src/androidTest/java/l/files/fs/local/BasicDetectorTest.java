package l.files.fs.local;

public final class BasicDetectorTest extends AbstractDetectorTest {

    @Override
    AbstractDetector detector() {
        return BasicDetector.INSTANCE;
    }

}
