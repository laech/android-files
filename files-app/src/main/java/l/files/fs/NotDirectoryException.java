package l.files.fs;

public class NotDirectoryException extends ResourceException {

    private static final long serialVersionUID = 586035440168429882L;

    public NotDirectoryException(String message, Throwable cause) {
        super(message, cause);
    }

}
