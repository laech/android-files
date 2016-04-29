package linux;

public final class ErrnoException extends Exception {

    public final int errno;

    private String strerror;

    ErrnoException(int errno) {
        super();
        this.errno = errno;
    }

    @Override
    public String getMessage() {
        if (strerror == null) {
            strerror = Str.strerror(errno);
        }
        return strerror;
    }

}
