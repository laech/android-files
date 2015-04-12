package l.files.fs;

public class NotEmptyException extends ResourceException {

    private static final long serialVersionUID = -7168482821376611436L;

    public NotEmptyException(String message, Throwable cause) {
        super(message, cause);
    }

}
