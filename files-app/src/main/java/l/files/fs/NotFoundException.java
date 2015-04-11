package l.files.fs;

public class NotFoundException extends ResourceException {

    private static final long serialVersionUID = 2305706569214380508L;

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
