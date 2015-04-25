package l.files.fs;

public class IsDirectoryException extends ResourceException {

    private static final long serialVersionUID = -1873816797277812410L;

    public IsDirectoryException(String message) {
        super(message);
    }

    public IsDirectoryException(String message, Throwable cause) {
        super(message, cause);
    }

}
