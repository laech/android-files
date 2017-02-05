package l.files.fs;

public final class PathCreateDirectoryFailureAccessDeniedTest
        extends PathCreateFailureAccessDeniedTest {

    public PathCreateDirectoryFailureAccessDeniedTest(String subPath) {
        super(subPath);
    }

    @Override
    PathCreation creation() {
        return PathCreation.DIRECTORY;
    }

}
