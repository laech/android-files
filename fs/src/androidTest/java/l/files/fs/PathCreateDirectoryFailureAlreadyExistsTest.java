package l.files.fs;

public final class PathCreateDirectoryFailureAlreadyExistsTest
        extends PathCreateSuccessTest {

    public PathCreateDirectoryFailureAlreadyExistsTest(String subPath) {
        super(subPath);
    }

    @Override
    PathCreation creation() {
        return PathCreation.DIRECTORY;
    }
}
