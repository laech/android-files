package l.files.fs;

import java.io.IOException;

public class UncheckedIOException extends RuntimeException {

    private static final long serialVersionUID = -3456962996367917124L;

    public UncheckedIOException(IOException throwable) {
        super(throwable);
    }

    @Override
    public IOException getCause() {
        return (IOException) super.getCause();
    }

}
