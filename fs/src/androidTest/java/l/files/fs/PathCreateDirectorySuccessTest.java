package l.files.fs;

public final class PathCreateDirectorySuccessTest extends PathCreateSuccessTest {

    public PathCreateDirectorySuccessTest(String subPath) {
        super(subPath);
    }

    @Override
    PathCreation creation() {
        return PathCreation.DIRECTORY;
    }
}
