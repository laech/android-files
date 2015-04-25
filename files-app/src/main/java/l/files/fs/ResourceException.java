package l.files.fs;

import java.io.IOException;

public class ResourceException extends IOException {

    private static final long serialVersionUID = 776257967400102431L;

    public ResourceException(String message) {
        super(message);
    }

    public ResourceException(String message, Throwable cause) {
        super(message, cause);
    }

}
