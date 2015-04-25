package l.files.fs;

public class ExistsException extends ResourceException {

    private static final long serialVersionUID = 2440242728467658401L;

    public ExistsException(String message) {
        super(message);
    }

    public ExistsException(String message, Throwable cause) {
        super(message, cause);
    }

}
