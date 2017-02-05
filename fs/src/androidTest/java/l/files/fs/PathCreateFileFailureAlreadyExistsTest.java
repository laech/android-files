package l.files.fs;

public final class PathCreateFileFailureAlreadyExistsTest
        extends PathCreateFailureAlreadyExistsTest {

    public PathCreateFileFailureAlreadyExistsTest(String subPath) {
        super(subPath);
    }

    @Override
    PathCreation creation() {
        return PathCreation.FILE;
    }
}
