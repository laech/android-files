package l.files.fs;

public class NotExistException extends ResourceException {

    private static final long serialVersionUID = 2305706569214380508L;

    public NotExistException(String message, Throwable cause) {
        super(message, cause);
    }

}
