package l.files.fs;

public final class PathCreateDirectoryFailureAlreadyExistsTest
        extends PathCreateFailureAlreadyExistsTest {

    public PathCreateDirectoryFailureAlreadyExistsTest(String subPath) {
        super(subPath);
    }

    @Override
    PathCreation creation() {
        return PathCreation.DIRECTORY;
    }
}
