package l.files.fs;

public class PathTooLongException extends ResourceException {

    private static final long serialVersionUID = 4183570287831804317L;

    public PathTooLongException(String message, Throwable cause) {
        super(message, cause);
    }

}
