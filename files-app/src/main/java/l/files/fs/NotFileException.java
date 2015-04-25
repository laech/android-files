package l.files.fs;

public class NotFileException extends ResourceException {

    private static final long serialVersionUID = 9123917915270290896L;

    public NotFileException(String message) {
        super(message);
    }

    public NotFileException(String message, Throwable cause) {
        super(message, cause);
    }

}
