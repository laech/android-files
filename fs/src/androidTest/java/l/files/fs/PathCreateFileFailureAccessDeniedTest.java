package l.files.fs;

public final class PathCreateFileFailureAccessDeniedTest
        extends PathCreateFailureAccessDeniedTest {

    public PathCreateFileFailureAccessDeniedTest(String subPath) {
        super(subPath);
    }

    @Override
    PathCreation creation() {
        return PathCreation.FILE;
    }

}
