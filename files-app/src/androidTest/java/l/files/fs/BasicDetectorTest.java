package l.files.fs;

public final class BasicDetectorTest extends AbstractDetectorTest
{
    @Override
    protected AbstractDetector detector()
    {
        return BasicDetector.INSTANCE;
    }
}
