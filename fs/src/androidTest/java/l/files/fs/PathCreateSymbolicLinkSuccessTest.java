package l.files.fs;

public final class PathCreateSymbolicLinkSuccessTest
        extends PathCreateSuccessTest {

    public PathCreateSymbolicLinkSuccessTest(String subPath) {
        super(subPath);
    }

    @Override
    PathCreation creation() {
        return PathCreation.SYMBOLIC_LINK;
    }
}
