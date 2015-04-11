package l.files.fs;

public class LoopException extends ResourceException {

    private static final long serialVersionUID = -7897928470280447656L;

    public LoopException(String message, Throwable cause) {
        super(message, cause);
    }

}
