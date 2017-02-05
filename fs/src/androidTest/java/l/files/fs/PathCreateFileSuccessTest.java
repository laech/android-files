package l.files.fs;

public final class PathCreateFileSuccessTest extends PathCreateSuccessTest {

    public PathCreateFileSuccessTest(String subPath) {
        super(subPath);
    }

    @Override
    PathCreation creation() {
        return PathCreation.FILE;
    }
}
