package l.files.fs;

public final class PathCreateSymbolicLinkFailureAccessDeniedTest
        extends PathCreateFailureAccessDeniedTest {

    public PathCreateSymbolicLinkFailureAccessDeniedTest(String subPath) {
        super(subPath);
    }

    @Override
    PathCreation creation() {
        return PathCreation.SYMBOLIC_LINK;
    }

}
