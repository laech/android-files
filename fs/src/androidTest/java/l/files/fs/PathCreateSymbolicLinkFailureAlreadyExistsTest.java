package l.files.fs;

public final class PathCreateSymbolicLinkFailureAlreadyExistsTest
        extends PathCreateFailureAlreadyExistsTest {

    public PathCreateSymbolicLinkFailureAlreadyExistsTest(String subPath) {
        super(subPath);
    }

    @Override
    PathCreation creation() {
        return PathCreation.SYMBOLIC_LINK;
    }
}
